package zhen.version1.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.view.KeyEvent;
import staticFamily.StaticApp;
import staticFamily.StaticMethod;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;
import zhen.version1.framework.Framework;

public class TaintedEventGeneration {
	//Input: String target_method in Jimple format
	//		 Framework frame
	//output:Event finalEvent
	//
	//return frame.rInfo.getMethodEventMap().get(target_method);
	public TaintedEventGeneration(){
		
	}
	
	/**
	 * Find a list of sequence which should be applied
	 * @param frame			-- the framework object which contains the UI model
	 * @param methodGroupList	-- the method list from tainted analysis
	 * @param finalEvent	-- the final event that triggered the target method
	 * @return
	 */
	public List<Event[]> findSequence(Framework frame,StaticApp testApp, ArrayList<ArrayList<String>>  methodGroupList,Event finalEvent){
		if(methodGroupList.isEmpty()){
			System.out.println("methodList is empty");
			return new ArrayList<Event[]>();
		}
		log("findSequence");
		for(ArrayList<String> group:methodGroupList){
			log("elements: "+group);
		}
		ArrayList<ArrayList<String>> sourceHandler = getSourceHandler(methodGroupList,testApp);
		List<Event[]> relatedEventSequence = generateEventWithTaint(frame, sourceHandler,finalEvent);
		System.out.println("TaintedEventGeneration, relatedEventSequence size:"+relatedEventSequence.size());
		return expandToRealSequence(frame, relatedEventSequence);
	}
	
	
	private List<Event[]> expandToRealSequence(Framework frame, List<Event[]> input){
		List<Event[]> result = new ArrayList<Event[]>();
		for(Event[] el: input){
			ArrayList<Event> list = new ArrayList<Event>();
			UIState previous = UIState.Launcher;
			for(Event e:el){
				UIState source = e.getSource();
				if(source.isLauncher){
					list.add(Event.getPressEvent(KeyEvent.KEYCODE_HOME+""));
				}else if(source.equals(previous)){
					//no need to find path
				}else{
					list.addAll(frame.rInfo.getEventSequence(previous, source));
				}
				list.add(e);
				previous = e.getTarget();
			}
			result.add(list.toArray(new Event[0]));
		}
		log("expandToRealSequence");
		for(Event[] eve : result){
			log(Arrays.toString(eve));
		}
		log("\n");
		
		return result;
	}
	
	private ArrayList<ArrayList<String>> getSourceHandler(ArrayList<ArrayList<String>> methodGroupList, StaticApp testApp){
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		//find the source
		for(ArrayList<String> group:methodGroupList){
			ArrayList<String> arr = new ArrayList<String>();
			for(String method:group){
				StaticMethod methodSig = testApp.findMethodByFullSignature(method);
				ArrayList<String> sequences = testApp.getCallSequenceForMethod(methodSig);
				for(String sequence : sequences){ 
					String[] parts = sequence.split(";");
					String source = parts[parts.length-1];
					if(!arr.contains(source))arr.add(source);
				}
			}
			result.add(arr);
		}
		
//		System.out.println("getSourceHandler:"+methodList.size());
//		for(String msg:methodList){
//			System.out.println("msg:"+msg);
//			StaticMethod method = testApp.findMethodByFullSignature(msg);
//			ArrayList<String> sequences = testApp.getCallSequenceForMethod(method);
//			for(String sequence : sequences){
//				System.out.println("sequence:"+sequence);
//				String[] parts = sequence.split(";");
//				String source = parts[parts.length-1];
//				result.add(source);
//			}
//		}
		
		log("getSourceHandler");
		for(ArrayList<String> group : result){
			log("elements: "+group);
		}
		log("\n");
		log("\n");
		
		return result;
	}
	
	private List<Event[]> generateEventWithTaint(Framework frame, ArrayList<ArrayList<String>> methodGroupList,Event finalEvent){
		log("generateEventWithTaint1");
		ArrayList<Event[]> eventMatrix = new ArrayList<Event[]>(); 
		Map<String, List<Event>>  map = frame.rInfo.getMethodEventMap();
		//find relevant event
		for(ArrayList<String> group : methodGroupList){
			ArrayList<Event> column = new ArrayList<Event>();
			for(String source:group){
				List<Event> events = map.get(source);
				for(Event event : events){
					if(!column.contains(event))column.add(event);
				}
			}
			if(column.isEmpty()) continue;
			Event[] tmp = column.toArray(new Event[0]);
			eventMatrix.add(tmp);
			log(Arrays.toString(tmp));
		}
		
		log("generateEventWithTaint1.1");
		
//		for(int j=0;j<methodList.size();j++){
//			List<Event> events = map.get(methodList.get(j));
//			if(events == null || events.isEmpty()){
//				log("cannot find events for:"+methodList.get(j));
//				continue;
//			}
//			List<Event> copy = new ArrayList<Event>(events);
////			copy.remove(finalEvent);
////			if(copy.isEmpty()){
////				log("empty event set for:"+methodList.get(j));
////				continue;
////			}
//			Event[] set= copy.toArray(new Event[0]);
//			eventMatrix.add(set);
//			log(methodList.get(j)+" has "+Arrays.toString(set));
//		}
		ArrayList<Integer> unsued = new ArrayList<Integer>();
		for(int i=0;i<eventMatrix.size();i++){
			unsued.add(i);
		}
		
		log("eventMatrix"); 
		for(Event[] eve : eventMatrix){
			log(Arrays.toString(eve));
		}
		log("\n");
		
		ArrayList<Event> current = new ArrayList<Event>();
		List<Event[]> deposit = new ArrayList<Event[]>();
		eventgenerateHelper(eventMatrix,unsued,current, deposit,finalEvent);
		
		log("generateEventWithTaint2");
		for(Event[] eve : deposit){
			log(Arrays.toString(eve));
		}
		log("\n");
		
		return deposit;
	}
	
	private static void eventgenerateHelper(ArrayList<Event[]> eventMatrix,ArrayList<Integer> unused,ArrayList<Event> current, List<Event[]> deposit,Event finalEvent){
		if(unused.isEmpty()){
			current.add(finalEvent);
			deposit.add(current.toArray(new Event[0]));
			current.remove(current.size()-1);
			return;
		}
		
		int size = unused.size();
		ArrayList<Integer> nextUnused = null;
		for(int i=0;i<size;i++){
			int beingUsed = unused.get(i);
			nextUnused = new ArrayList<Integer>(unused);
			nextUnused.remove(i);
			
			for(Event e : eventMatrix.get(beingUsed)){
				current.add(e);
				eventgenerateHelper(eventMatrix, nextUnused, current,deposit,finalEvent);
				current.remove(current.size()-1);
			}
//			eventgenerateHelper
		}
		 
	}
	
	public static void main(String[] args){
		ArrayList<Event[]> eventMatrix = new ArrayList<Event[]>();
		eventMatrix.add(new Event[]{Event.getOnClickEvent(100, 101),
									Event.getOnClickEvent(200, 201)});
		
		eventMatrix.add(new Event[]{Event.getOnBackEvent()});
		
		eventMatrix.add(new Event[]{Event.getOnClickEvent(300, 301),
				Event.getOnClickEvent(400, 401)});
		
		Event finalEvent = Event.getOnClickEvent(500, 501);
		
		ArrayList<Event> current = new ArrayList<Event>();
		List<Event[]> deposit = new ArrayList<Event[]>();
		ArrayList<Integer> unsued = new ArrayList<Integer>();
		for(int i=0;i<eventMatrix.size();i++){
			unsued.add(i);
		}
		eventgenerateHelper(eventMatrix,unsued,current, deposit,finalEvent);

		System.out.println(deposit.size());
		for(Event[] eve : deposit){
			System.out.println(Arrays.toString(eve));
		}
		
	}
	PrintWriter pw = null;
	void log(Object o ){
		if(pw == null){
			try {
				pw = new PrintWriter(new File("TaintedEventGeneration_log"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pw.println(o.toString());
		pw.flush();
	}
}
