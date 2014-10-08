package john.generateSequences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import zhen.version1.framework.Framework;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;

public class GenerateSequences {
	
	private Framework fw;
	private ArrayList<UIState> knownVertices;
	private ArrayList<String> targetMethods;
	private ArrayList<String> unenhancedSequences;
	private ArrayList<String> enchancedSequences;
	
	public boolean debugMode = false;
	//private ArrayList<ArrayList<String>> methodRelationships;
	
	public GenerateSequences(Framework fw, ArrayList<String> targetMethods) {
		this.fw = fw;
		knownVertices = getKnownVertices();
		this.targetMethods = targetMethods;
	}
	
//	public void generateMethodButtonCorrelations()
//	{
//		String[] methods = (String[]) fw.getAttributes().get("methods");
//		ArrayList<String> retList = new ArrayList<String>();
//		
//		for(String method : methods){
//			
//			List<List<Event>>  llevent = fw.rInfo.findPotentialPathForHandler(method);
//			String retMethod = method;
//			for(List<Event> levent: llevent) {
//				Event event = levent.get(0);
//				retMethod = retMethod + "," + event.toString();
//				//System.out.println(method + "," + event.toString());
//			}
//			
//			retList.add(retMethod);
//		}
//	}
	
	public void generateUnenhancedSequences()
	{
		ArrayList<String> sequences = new ArrayList<String>();
		
		for (String target: targetMethods) {
			
			for (UIState vertex: knownVertices) {
				ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(vertex.Launcher, vertex);
				ArrayList<Event> ieel = (ArrayList<Event>) vertex.getIneffectiveEventList();
				
				for (Event event: el) {
					for (String string: event.getMethodHits()) {
						if (string.contains(target.trim())) {
							sequences.add(target + "|" + eventSequenceToString(el) + event.toString().trim().split("in")[0].trim());
							break;
						}
					}
				}
				
				for (Event event: ieel) {
					for (String string: event.getMethodHits()) {
						if (string.contains(target.trim())) {
							sequences.add(target + "|" + eventSequenceToString(el) + event.toString().trim().split("in")[0].trim());
							break;
						}
					}
				}
				
			}
		}
		
		//if (debugMode) {
			for (String sequence: sequences)
				System.out.println(sequence);
		//}
		
	}
	
	public ArrayList<String> generateMethodGroups()
	{
		HashMap<String, String> hasSeen = new HashMap<String, String>();
		ArrayList<String> ret = new ArrayList<String>();
		
		//for (String target: targetMethods) {
			for (UIState vertex: knownVertices) {
				ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(vertex.Launcher, vertex);
				ArrayList<Event> ieel = (ArrayList<Event>) vertex.getIneffectiveEventList();
				
				for (Event event: el) {
					for (String string: event.getMethodHits()) {
						if (hasSeen.containsKey(string.trim())) {
							String newString = hasSeen.get(string.trim()) + "|" + event.toString().trim().split("in")[0].trim()/*.split(" ")[1].trim()*/;
							hasSeen.put(string.trim(), newString);
						}
						else {
							String newString = event.toString().trim().split("in")[0].trim()/*.split(" ")[1].trim()*/;
							hasSeen.put(string.trim(), newString);
						}
					}
				}
				
				for (Event event: ieel) {
					for (String string: event.getMethodHits()) {
						if (hasSeen.containsKey(string.trim())) {
							String newString = hasSeen.get(string.trim()) + "|" + event.toString().trim().split("in")[0].trim()/*.split(" ")[1].trim()*/;
							hasSeen.put(string.trim(), newString);
						}
						else {
							String newString = event.toString().trim().split("in")[0].trim()/*.split(" ")[1].trim()*/;
							hasSeen.put(string.trim(), newString);
						}
					}
				}
			}
		
			for(Entry<String, String> entry: hasSeen.entrySet()) {
				System.out.println(entry.getKey() + "%" +entry.getValue());
				ret.add(entry.getKey() + "%" + entry.getValue());
			}
		
			return ret;
	}
	
	public void printKnownVertices()
	{	
		for (UIState vertex: knownVertices) {
			ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(vertex.Launcher, vertex);
			ArrayList<Event> ieel = (ArrayList<Event>) vertex.getIneffectiveEventList();
			System.out.println(vertex.actName);
			System.out.print("EVENTS{");
			for (Event event: el) {
				System.out.print(event.toString().trim().split("in")[0].trim() + "|");
			}
			System.out.println("}");
			System.out.print("INEFFECTIVE EVENTS{");
			for (Event event: ieel) {
				System.out.print(event.toString().trim().split("in")[0].trim() + "|");
				System.out.print("(");
				for (String string: event.getMethodHits()) {
					System.out.print(string + ",");
				}
				System.out.print(")");
			}
			System.out.println("}");
		}
	}
	
	public void toggleDebugMode(boolean onOrOff) {
		debugMode = onOrOff;
	}
	
	private ArrayList<UIState> getKnownVertices()
	{
		return (ArrayList<UIState>) fw.rInfo.getUIModel().getKnownVertices();
	}
	
	private String eventSequenceToString(ArrayList<Event> el)
	{
		String string = "";
		
		for (Event event: el) {
			string += event.toString().trim().split("in")[0].trim() + "|";
		}
		
		return string;
	}
}
