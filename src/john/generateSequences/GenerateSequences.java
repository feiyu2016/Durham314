package john.generateSequences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import zhen.version1.framework.Common;
import zhen.version1.framework.Framework;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;

public class GenerateSequences {
	
	private Framework fw;
	private ArrayList<UIState> knownVertices;
	private String[] targetMethods;
	private ArrayList<ArrayList<Event>> unenhancedSequences;
	private ArrayList<ArrayList<Event>> enhancedSequences;
	
	public boolean debugMode;
	
	public GenerateSequences(Framework fw, String[] targetMethods, boolean debugMode) {
		this.fw = fw;
		this.targetMethods = targetMethods;
		this.debugMode = debugMode;
		knownVertices = this.getKnownVertices();
		unenhancedSequences = this.generateUnenhancedSequences();
		//enhancedSequences = this.generateEnhancedSequences();
	}
	
	public ArrayList<ArrayList<Event>> getUnenhancedSequences()
	{
		return unenhancedSequences;
	}
	
	public ArrayList<ArrayList<Event>> getEnhancedSequences()
	{
		return enhancedSequences;
	}
	
	private ArrayList<ArrayList<Event>> generateEnhancedSequences()
	{
		ArrayList<ArrayList<Event>> uS = new ArrayList<ArrayList<Event>>();
		uS.addAll(this.unenhancedSequences);
	}
	
//	private ArrayList<String> generateEnhancedSequences()
//	{
//		ArrayList<String> uS = new ArrayList<String>();
//		ArrayList<String> ret = new ArrayList<String>();
//		ArrayList<String> mG = methodGroups;
//		
//		for (String string: unenhancedSequences) {
//			String[] split = string.trim().split("\\|");
//			for (int i = 1; i < split.length; i++) {
//				uS.add(split[i]);
//			}
//		}
//		
//		for (String string: uS) {
//			String combo = "";
//			for (String group: mG) {
//				if (group.contains(string)) {
//					ret.add(string);
//				}
//				else {
//					String temp = group.trim().split("\\|")[0] + "|";
//					ret.add(temp + string);
//					combo += temp;
//				}
//			}
//			ret.add(combo + string);
//		}
//		
//		return ret;
//	}
	
	private ArrayList<ArrayList<Event>> generateUnenhancedSequences()
	{
		ArrayList<ArrayList<Event>> sequences = new ArrayList<ArrayList<Event>>();
		int i = 1;
		for (String target: targetMethods) {
			for (UIState vertex: knownVertices) {
				ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(UIState.Launcher, vertex);
				ArrayList<Event> ieel = (ArrayList<Event>) vertex.getIneffectiveEventList();
				System.out.println("UIState " + i++ + " " + vertex.actName);
				if (vertex.isReachable)
					System.out.println(" this UIState is reachable");
				
				for (Event event: ieel) {
					for (String string: event.getMethodHits()) {
						if (string.contains(target.trim().replace("<","").replace(">", ""))) {
							ArrayList<Event> temp = new ArrayList<Event>();
							temp.addAll(el);
							for (Event print : el) {
								try {
									String x = print.getValue(Common.event_att_click_x).toString();
									String y = print.getValue(Common.event_att_click_y).toString();
									System.out.print("(" + x + "," + y + ")");
								} catch (Exception e) {}
							}
							try {
								String x = event.getValue(Common.event_att_click_x).toString();
								String y = event.getValue(Common.event_att_click_y).toString();
								System.out.println("(" + x + "," + y + ")");
							} catch (Exception e) {}
							temp.add(event);
							sequences.add(temp);
							break;
						}
					}
				}
			}
		}
		
		if (debugMode) {
			System.out.println("There are " + sequences.size() + " baseline sequences.");
			for (ArrayList<Event> sequence: sequences) {
				for (Event event: sequence) {
					try {
						String x = event.getValue(Common.event_att_click_x).toString();
						String y = event.getValue(Common.event_att_click_y).toString();
						System.out.print("(" + x + "," + y + ")");
					} catch (Exception e) {};
				}
				System.out.println();
			}
		}
			
		return sequences;
	}
	
	private ArrayList<ArrayList<Event>> generateEventGroups(Event currentEvent)
	{
		UIState uis = currentEvent.getTarget();
		HashMap<String, ArrayList<Event>> hasSeen = new HashMap<String, ArrayList<Event>>();
		ArrayList<ArrayList<Event>> ret = new ArrayList<ArrayList<Event>>();
		ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(UIState.Launcher, uis);
		ArrayList<Event> ieel = (ArrayList<Event>) uis.getIneffectiveEventList();
		
		for (Event event: ieel) {
			for (String string: event.getMethodHits()) {
				System.out.println(string);
				if (hasSeen.containsKey(string.trim())) {
					try {
						ArrayList<Event> newList = new ArrayList<Event>();
						newList.addAll(hasSeen.get(string.trim()));
						newList.add(event);
						hasSeen.put(string.trim(), newList);
					} catch (ArrayIndexOutOfBoundsException e){
						continue;
					}
				}
				else {
					try {
						ArrayList<Event> newList = new ArrayList<Event>();
						newList.add(event);
						hasSeen.put(string.trim(), newList);
					} catch (ArrayIndexOutOfBoundsException e){
						continue;
					}
				}
			}
		}
		
		for(Entry<String, ArrayList<Event>> entry: hasSeen.entrySet()) {
			ret.add(entry.getValue());
		}
		
		return mergeEventGroups(ret);
	}
	
	private ArrayList<ArrayList<Event>> mergeEventGroups(ArrayList<ArrayList<Event>> input) {
		boolean isIncluded[] = new boolean[input.size()];
        
		for (int i = 0; i < isIncluded.length; i++) {
            isIncluded[i] = false;
        }
        
        for ( int i = 0; i < input.size()-1; i++){
            if (isIncluded[i]) continue;
            
            ArrayList<Event> criteria = input.get(i);
            
            for ( int j = 0; j < input.size(); j++) {
                if (i == j || isIncluded[j]) {
                    continue;
                }
                
                isIncluded[j] = containsAll(criteria, input);
            }
        }
        
        ArrayList<ArrayList<Event>> result = new ArrayList<ArrayList<Event>>();
        for (int i = 0; i < input.size(); i++) {
            if (!isIncluded[i]) result.add(input.get(i));
        }
        
        if (debugMode) {
        	System.out.println("----MERGED METHOD GROUPS:");
//	       for (String string: result) {
//	        	System.out.println(string);
//	        }
        }
        
        return result;
    }

    private boolean containsAll(ArrayList<Event> pointer, ArrayList<ArrayList<Event>> list) {
        for (ArrayList<Event> sequence : list) {
            if (!pointer.contains(sequence)) {
                return false;
            }
        }

        return true;
    }
	
	private ArrayList<UIState> getKnownVertices()
	{
		return (ArrayList<UIState>) fw.rInfo.getUIModel().getKnownVertices();
	}
}
