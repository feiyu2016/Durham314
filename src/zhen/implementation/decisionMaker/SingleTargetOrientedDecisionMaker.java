package zhen.implementation.decisionMaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.android.hierarchyviewerlib.models.ViewNode;

import zhen.framework.AbstractDecisionMaker;
import zhen.framework.Framework;
import zhen.implementation.graph.Event;
import zhen.implementation.graph.EventType;
import zhen.implementation.graph.RunTimeLayout;

public class SingleTargetOrientedDecisionMaker extends AbstractDecisionMaker{
	

	public SingleTargetOrientedDecisionMaker(Framework frame) {
		super(frame); 
		layoutAccessStack = new Stack<RunTimeLayout>();
	}
	
	private int targetIndex = 0;
	private String[] targets;//which should be the ID
	private String[] actNames;
	private boolean[] actReached;
	private String packegeName;
	private Stack<RunTimeLayout> layoutAccessStack;
	
	private int state = 0;
	//0 for start
	//1 for has a target
	//2 target reached
	//3 for all finished

//	private boolean readyForNextTarget;
//	private boolean hasSetupDevice;
//	private boolean hasMoreLayoutToTry;
//	private boolean expandKnowledge;
//	private boolean needReposition;
//	private boolean targetInsight;
	
	
	private boolean checkTargetHit;
	private boolean checkExpectenceOnBack;
	private boolean checkExpectenceOnRestart;
	private ArrayList<RunTimeLayout>  potentialLyout;
	private String[] target;
	private boolean actLaunched;
	//[com.example.simplecallgraphtestapp.MainActivity,activity_main,trigger2,android:onClick]
	
	
	
	/**
	 * Assumption: onBack will not lead to a new layout
	 */
	@Override
	public Event[] nextEvent() {
		Event[] result = null;
		switch(state){
		case 0:{	//setup state
			System.out.println("startup");
			result =  new Event[]{new Event(EventType.SETUP, null)};
			state = 1;
			return result;
		}
		case 1:{	//getNextTarget
			System.out.println("getNextTarget");
			target = getNextTarget();
			if(target == null){ // which indicates no more job to do
				this.frame.requestFinish();
				return null;
			}
			
			if(!target[0].startsWith(packegeName)){
				target[0] = packegeName+target[0];
			}
			target[2] = "id/"+target[2];
			potentialLyout = this.frame.dynamicInfo.findPotentialLayout(target[0], target[1], target[2]);
			if(potentialLyout == null || potentialLyout.size() <=0){ 
				state= 3;
			}else{ 
				state= 2;
			}
//			System.out.println(target[0]+"   "+  target[2]);
			return null;
		}
		case 2:{	//check target within knowledge
			System.out.println("checking knowledge");
			if(checkTargetHit){
				//TODO -- assume target will be reached
				
				
				checkTargetHit = false;
				state = 1;
				return null;
			}else if(potentialLyout.size() > 0){
				RunTimeLayout potential = potentialLyout.remove(0);
				List<Event> sequence = this.frame.dynamicInfo.getEventSequence(potential);
				Event event = new Event(target[3],potential.findViewsById(target[2])[0]);
				sequence.add(event);
//				if(sequence.get(0))
				
				
				result = sequence.toArray(new Event[0]);
				checkTargetHit = true;
				return result;
			}else{
				checkTargetHit = false;
				result = null;
				state = 3;
				return result;
			}
		}
		case 3:{
			System.out.println("exploring");
			if(actLaunched){
				System.out.println("inner state: actLaunched");
				RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
				if(current.isLauncher){
					throw new AssertionError();
				}
				this.layoutAccessStack.push(current);
				actLaunched = false;
				return null;
			}else if(checkExpectenceOnRestart){
				System.out.println("inner state: checkExpectenceOnRestart");
				RunTimeLayout expectence = this.layoutAccessStack.peek();
				RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
				if(!expectence.equals(current)){
					//TODO
					throw new AssertionError();
				}
				checkExpectenceOnRestart = false;
				return result;
			}else if(checkExpectenceOnBack){
				System.out.println("inner state: checkExpectenceOnBack");
				RunTimeLayout expectence = this.layoutAccessStack.peek();
				RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
				if(!expectence.equals(current)){
					List<Event>  equence = this.frame.dynamicInfo.getEventSequence(expectence);
					result = equence.toArray(new Event[0]);
					checkExpectenceOnRestart = true;
				}else{
					result = null;
				}
				checkExpectenceOnBack= false;
				return result;
			}else if(checkTargetHit){
				System.out.println("inner state: checkTargetHit");
				//TODO -- assume target will be reached
				checkTargetHit = false;
				state = 1;
				return result;
			}else if(layoutAccessStack.isEmpty()){
				System.out.println("inner state: empty stack");
				String actName = findTheFirstUnReachedActivity();
				if(actName == null){
					this.frame.requestFinish();
					return null;
					
				}
				
				
				Event launching = new Event(EventType.LAUNCH, null);
				launching.setAttribute("actname", actName);
				result = new Event[]{ launching };
				actLaunched = true;
				return result;
			}else{
				RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
				RunTimeLayout previous = this.layoutAccessStack.peek();
				System.out.println("current:"+current.getActName());
				labelActAsReached(current.getActName());
				if(current.isLauncher && previous.isLauncher){
					System.out.println("inner state: launcher again");
					throw new AssertionError();
				}else if(current.isLauncher){
					System.out.println("inner state: launcher present");
					List<Event>  sequence = this.frame.dynamicInfo.getEventSequence(previous);
					result = sequence.toArray(new Event[0]);
					return result;
				}else if(current.getLayoutRoot() == null){
					System.out.println("inner state: out of scope");
					//which means the current layout is out of scope, simply press back should do the work
					//there is chance to fail. on purpose close the original activity
					result = new Event[]{ Event.getBackEvent() };
					return result;
				}else if(current.equals(previous)){	//same layout
					System.out.println("inner state: same layout");
					Event nextEvent = getUnusedEvent(current);
					if(nextEvent == null){//no more event
						layoutAccessStack.pop();
						if(layoutAccessStack.size() > 0){
							result = new Event[]{Event.getBackEvent()};
							checkExpectenceOnBack = true;
						}else{ result = null; }
					}else{
						result = new Event[]{nextEvent};
					}
					return result;
				}else if(current.visitCount > 1){ //encounter a layout which has been visited before
					System.out.println("inner state: known layout ");
					//goes back
					result = new Event[]{Event.getBackEvent()};
					checkExpectenceOnBack = true;
					return result;
				}else if(current.visitCount <= 1){ //encounter a new layout
					System.out.println("inner state: new layout ");
					this.layoutAccessStack.push(current);
					if(current.getActName().endsWith((target[0]))){
						System.out.println("Same act name as target");
						int i = 0;
						ViewNode lookingFor = null;
						for(ViewNode node : current.getNodeListReference()){
							if(node.id.endsWith(target[2])){
								lookingFor = node;
								break;
							}
							i+= 1;
						}
						if(lookingFor != null){
							Event[] eArr = current.getPossibleViewEventList().get(i);
							for(Event event: eArr){
								if(event.getType().equals(target[3])){
									result = new Event[]{event};
								}
							}
							if(result == null) result = new Event[]{new Event(target[3], lookingFor)};
							checkTargetHit = true;
							return result;
						}else{	//not here
							return getNextEventForLayout(current);
						}
					}else{
						return getNextEventForLayout(current);
					}
				}else return null;
			}
		}
		default:{
			throw new AssertionError();
		}
		}
	}
	
	private Event[] getNextEventForLayout(RunTimeLayout current){
		Event[] result;
		Event nextEvent = getUnusedEvent(current);
		if(nextEvent == null){//no more event
			layoutAccessStack.pop();
			if(layoutAccessStack.size() > 0){
				result = new Event[]{Event.getBackEvent()};
				checkExpectenceOnBack = true;
			}else{ result = null; }
		}else{
			result = new Event[]{nextEvent};
		}
		return result;
	}
	
	private static Event getUnusedEvent(RunTimeLayout layout){
		int index = 0;
		String key = "view_index";
		if(layout.hasExtraInfo(key)){
			index = (Integer)layout.getExtraInfo(key);
		}
		Event result = null;
		ArrayList<Event[]> evenetList = layout.getPossibleViewEventList();
		OUTER:for(;index<evenetList.size();index++){
			Event[] eArr = evenetList.get(index);
			if(eArr == null) continue;
			for(Event single : eArr){
				if(single.operationCount <= 0){
					result = single;
					break OUTER;
				}
			}
		}
		layout.putExtraInfo(key, index);
//		if(result!=null)
//		System.out.println(result.getAppliedViewNode().id+":"+result.operationCount);
		return result;
	}
	
	private String[] getNextTarget(){
		String[] result = null;
		if(this.targetIndex >= targets.length){
			return null;
		}
		result = targets[this.targetIndex].split(",");
		if(result.length != 4) throw new AssertionError();
		this.targetIndex += 1;
		System.out.println(Arrays.toString(result));
		return result;
	}

	@Override
	public boolean init(Map<String,Object> attribute) {
		//activity;id
		targets = (String[]) attribute.get("targets");
		actNames = (String[]) attribute.get("actlist");
		packegeName = (String) attribute.get("package");
		if(!packegeName.endsWith("/")) packegeName = packegeName+"/";
		actReached = new boolean[actNames.length];
		layoutAccessStack = new Stack<RunTimeLayout>();
		System.out.println(packegeName);
		System.out.println(Arrays.toString(targets));
		return true;
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
	}
//	
	private String findTheFirstUnReachedActivity(){
		for(int i=0;i<actReached.length;i++){
			if(!actReached[i]) return actNames[i];
		}
		return null;
	}
	
	
	private void labelActAsReached(String actName){
		int i =0;
		for(String act : this.actNames){
			if(act.equals(actName)){
				this.actReached[i] = true;
				break;
			}
			i+=1;
		}
	}

}



