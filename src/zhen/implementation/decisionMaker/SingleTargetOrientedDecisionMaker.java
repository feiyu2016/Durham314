package zhen.implementation.decisionMaker;

import java.util.ArrayList;
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
	//[com.example.simplecallgraphtestapp.MainActivity,activity_main,trigger2,android:onClick]
	
	
	
	/**
	 * Assumption: onBack will not lead to a new layout
	 */
	@Override
	public Event[] nextEvent() {
		Event[] result = null;
		switch(state){
		case 0:{	//setup state
			result =  new Event[]{new Event(EventType.SETUP, null)};
			state = 1;
		}break;
		case 1:{	//getNextTarget
			target = getNextTarget();
			if(target == null){ // which indicates no more job to do
				this.frame.requestFinish();
				return null;
			}
			potentialLyout = this.frame.dynamicInfo.findPotentialLayout(target[0], target[1], target[2]);
			if(potentialLyout == null || potentialLyout.size() <=0){ 
				state= 3;
			}else{ 
				state= 2;
			}
		}break;
		case 2:{	//check target within knowledge
			if(checkTargetHit){
				//TODO -- assume target will be reached
				checkTargetHit = false;
				state = 1;
			}else if(potentialLyout.size() > 0){
				RunTimeLayout potential = potentialLyout.remove(0);
				List<Event> sequence = this.frame.dynamicInfo.getEventSequence(potential);
				Event event = new Event(target[3],potential.findViewsById(target[2])[0]);
				sequence.add(event);
				result = sequence.toArray(new Event[0]);
				checkTargetHit = true;
			}else{
				checkTargetHit = false;
				result = null;
				state = 3;
			}
		}break;
		case 3:{
			if(checkExpectenceOnRestart){
				RunTimeLayout expectence = this.layoutAccessStack.peek();
				RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
				if(expectence != current){
					//TODO
					throw new AssertionError();
				}
				checkExpectenceOnRestart = false;
			}else if(checkExpectenceOnBack){
				RunTimeLayout expectence = this.layoutAccessStack.peek();
				RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
				if(expectence != current){
					List<Event>  equence = this.frame.dynamicInfo.getEventSequence(expectence);
					result = equence.toArray(new Event[0]);
					checkExpectenceOnRestart = true;
				}else{
					result = null;
				}
				checkExpectenceOnBack= false;
			}else if(checkTargetHit){
				//TODO -- assume target will be reached
				checkTargetHit = false;
				state = 1;
			}else if(layoutAccessStack.isEmpty()){
				String actName = findTheFirstUnReachedActivity();
				Event launching = new Event(EventType.LAUNCH, null);
				launching.setAttribute("actname", actName);
				result = new Event[]{ launching };
			}else{
				RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
				RunTimeLayout previous = this.layoutAccessStack.peek();
				labelActAsReached(current.getActName());
				if(current.isLauncher && previous.isLauncher){
					throw new AssertionError();
				}else if(current.isLauncher){
					List<Event>  sequence = this.frame.dynamicInfo.getEventSequence(previous);
					result = sequence.toArray(new Event[0]);
				}else if(current.getLayoutRoot() == null){
					//which means the current layout is out of scope, simply press back should do the work
					//there is chance to fail. on purpose close the original activity
					result = new Event[]{ Event.getBackEvent() };
				}else if(current == previous){	//same layout
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
				}else if(current.visitCount > 1){ //encounter a layout which has been visited before
					//goes back
					result = new Event[]{Event.getBackEvent()};
					checkExpectenceOnBack = true;
				}else if(current.visitCount <= 1){ //encounter a new layout
					this.layoutAccessStack.push(current);
					if(current.getActName().contains((target[0]))){
						int i = 0;
						ViewNode lookingFor = null;
						for(ViewNode node : current.getNodeListReference()){
							if(node.id.contains(target[2])){
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
						}else{	//not here
							result = getNextEventForLayout(current);
						}
					}else{
						result = getNextEventForLayout(current);
					}
				}
			}
		}break;
		default:{
			throw new AssertionError();
		}
		}
		return result;
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
		return result;
	}

	@Override
	public boolean init(Map<String,Object> attribute) {
		//activity;id
		targets = (String[]) attribute.get("targets");
		actNames = (String[]) attribute.get("actname");
		actReached = new boolean[actNames.length];
		layoutAccessStack = new Stack<RunTimeLayout>();

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
//	//ID oriented
//	private Event getEventForTarget(RunTimeLayout layout){
//		
//		
//		return null;
//	}
}







////check the previous layout has more event to apply.
//String key = "view_index";
//int index = 0;
//if(previous.hasExtraInfo(key)){
//	index = (Integer)previous.getExtraInfo(key);
//}
//Event untouched = null;
//ArrayList<Event[]> viewEventList = previous.getPossibleViewEventList();
//while(untouched == null && viewEventList.size() > index){
//	Event[] elist = viewEventList.get(index);
//	for(Event e : elist){
//		if(e.operationCount <= 0){
//			untouched = e;
//			break;
//		}
//	}
//	index += 1;
//}
//
//if(untouched == null){
//	//cannot find event for view
//	//try to go back
//	
//	
//	
//	
//}else{
//	
//	
//	previous.putExtraInfo(key, index);
//}


























//if(!hasSetupDevice){
//	
//	hasSetupDevice = true;
//}else if(readyForNextTarget){
//	//get the target 
//	target = getNextTarget();
//	if(target == null){ // which indicates no more job to do
//		this.frame.requestFinish();
//		return null;
//	}else{ // check if we know this in our graph
//		potentialLyout = this.frame.dynamicInfo.findPotentialLayout(target[0], target[1], target[2]);
//		if(potentialLyout==null || potentialLyout.size() <=0){
//			//no potential layout found
//			//which indicate traversal needed
//			expandKnowledge = true;
//		}else{ //find some layouts 
//			hasMoreLayoutToTry = true;
//		}
//	}
//	readyForNextTarget= false;
//}else if(targetInsight){
//	
//	
//	
//	
//	
//}else if(checkTargetHit){
//	
//	
//	
//	
//	
//}else if(hasMoreLayoutToTry){
//	if(potentialLyout.size() > 0){
//		RunTimeLayout potential = potentialLyout.remove(0);
//		List<Event> sequence = this.frame.dynamicInfo.getEventSequence(potential);
//		result = sequence.toArray(new Event[0]);
//		checkTargetHit = true;
//	}else{
//		//can find any layout
//		hasMoreLayoutToTry = false;
//		checkTargetHit = false;
//		expandKnowledge = true;
//		result = null;
//	}
//}else if(checkExpectenceOnBack){
//	RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
//	if(current.isNewLayout()){
//		throw new AssertionError();
//	}
//	
////	if(checkExpectence == )
//	
//}else if(expandKnowledge){//expanding knowledge of UI
//	if(layoutAccessStack.isEmpty()){ //and meanwhile the current layout should be launcher
//		//launch an acitivity;
//		String actName = findTheFirstUnReachedActivity();
//		Event launching = new Event(EventType.LAUNCH, null);
//		launching.setAttribute("actname", actName);
//		result = new Event[]{ launching };
//	}else{
//		RunTimeLayout current = this.frame.dynamicInfo.getCurrentLayout();
//		RunTimeLayout previous = this.layoutAccessStack.peek();
//		if(current.isLauncher){
//			//encounter launcher
//			if(previous.isLauncher){
//				//does not expect to be 
//				//maybe bacause of an event fails to actually execute?
//				//press home button or back when layout already launcher?
//				//TODO need checking
//				throw new AssertionError();
//			}else{
//				//when an event has been executed and leads to launcher
//				//tries goes back
//				List<Event>  sequence = this.frame.dynamicInfo.getEventSequence(previous);
//				result = sequence.toArray(new Event[0]);
//			}
//		}else if(current.getLayoutRoot() == null){
//			//which means the current layout is out of scope
//			//simply press back should do the work
//			//there is chance to fail.
//			//on purpose close the original activity
//			result = new Event[]{ Event.getBackEvent() };
//		}else{	//the layout should be within the activity list 
//			//check if current is the same as previous
//			if(current == previous){
//				Event nextEvent = getEventForTarget(current);
//				if(nextEvent == null){//no more event
//					layoutAccessStack.pop();
//					if(layoutAccessStack.size() > 0){
//						result = new Event[]{Event.getBackEvent()};
//						checkExpectenceOnBack = true;
//					}else{
//						result = null;
//						expandKnowledge = true;
//					}
//				}else{
//					result = new Event[]{nextEvent};
//				}
//			}else{//the current layout is different from the previous
//				//check if the layout has been encountered before
//				int visitCount = current.visitCount;
//				if(visitCount <= 1){
//					//the first time layout encountered;
//					Event possibleEvent = getUnusedEvent(current);
//					if(possibleEvent != null){
//						//there is one
//						result = new Event[]{ possibleEvent };
//					}else{
//						//no event can be applied
//						//such a worthless layout ...
//						//go back!
//						
//					}
//				}else{ // the layout has been encountered more than once;
//					
//				}
//			}
//			
//			
//
//
//			
//		}
//	}
//	
//	
//}else if(checkExpectenceOnBack){
//		
//	
//}else{
//	throw new AssertionError("Invalid state");
//}



