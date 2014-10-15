package zhen.version1.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import staticFamily.StaticApp;
import staticFamily.StaticMethod;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;
import zhen.version1.framework.Framework;

public class TaintedEventGeneration {
	
	/**
	 * Find a list of sequence which should be applied
	 * @param frame			-- the framework object which contains the UI model
	 * @param methodList	-- the method list from tainted analysis
	 * @param finalEvent	-- the final event that trigger the target method
	 * @return
	 */
	public static List<Event[]> findSequence(Framework frame,StaticApp testApp, ArrayList<String> methodList,Event finalEvent){
		List<String> sourceHandler = getSourceHandler(methodList,testApp);
		List<Event[]> relatedEventSequence = generateEventWithTaint(frame, sourceHandler,finalEvent);
		System.out.println("TaintedEventGeneration, relatedEventSequence size:"+relatedEventSequence.size());
		return expandToRealSequence(frame, relatedEventSequence);
	}
	
	
	private static List<Event[]> expandToRealSequence(Framework frame, List<Event[]> input){
		List<Event[]> result = new ArrayList<Event[]>();
		for(Event[] el: input){
			ArrayList<Event> list = new ArrayList<Event>();
			UIState previous = UIState.Launcher;
			for(Event e:el){
				UIState source = e.getSource();
				if(source.equals(previous)){
					//no need to find path
					list.add(e);
				}else{
					list.addAll(frame.rInfo.getEventSequence(previous, source));
					list.add(e);
				}
				previous = e.getTarget();
			}
			result.add(list.toArray(new Event[0]));
		}
		return result;
	}
	
	private static ArrayList<String> getSourceHandler(ArrayList<String> methodList, StaticApp testApp){
		ArrayList<String> result = new ArrayList<String>();
		for(String msg:methodList){
			StaticMethod method = testApp.findMethodByFullSignature(msg);
			result.addAll(testApp.getCallSequenceForMethod(method));
		}
		
		return result;
	}
	
	private static List<Event[]> generateEventWithTaint(Framework frame, List<String> methodList,Event finalEvent){
		ArrayList<Event[]> eventMatrix = new ArrayList<Event[]>(); 
		Map<String, List<Event>>  map = frame.rInfo.getMethodEventMap();
		for(int j=0;j<methodList.size();j++){
			List<Event> events = map.get(methodList.get(j));
			if(events == null || events.isEmpty()) continue;
			List<Event> copy = new ArrayList<Event>(events);
			copy.remove(finalEvent);
			if(copy.isEmpty()) continue;
			eventMatrix.add(copy.toArray(new Event[0]));
		}
		ArrayList<Integer> unsued = new ArrayList<Integer>();
		for(int i=0;i<eventMatrix.size();i++){
			unsued.add(i);
		}
		ArrayList<Event> current = new ArrayList<Event>();
		List<Event[]> deposit = new ArrayList<Event[]>();
		eventgenerateHelper(eventMatrix,unsued,current, deposit,finalEvent);
		
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
		for(int i=0;i<size;i++){
			int beingUsed = unused.get(i);
			unused.add(beingUsed);
			
			for(Event e : eventMatrix.get(beingUsed)){
				current.add(e);
				eventgenerateHelper(eventMatrix, unused, current,deposit,finalEvent);
				current.remove(current.size()-1);
			}
//			eventgenerateHelper
			
			unused.remove(unused.size()-1);
		}
		 
	}
}
