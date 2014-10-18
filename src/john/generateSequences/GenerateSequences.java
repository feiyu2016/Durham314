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
		enhancedSequences = this.generateEnhancedSequences();
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
		ArrayList<ArrayList<Event>> eG = new ArrayList<ArrayList<Event>>();
		ArrayList<ArrayList<Event>> ret = new ArrayList<ArrayList<Event>>();
		for (ArrayList<Event> s : uS) {
			ArrayList<ArrayList<Event>> allSequences = new ArrayList<ArrayList<Event>>();
			ArrayList<UIState> seen = new ArrayList<UIState>();
			for (int i = 0; i < s.size(); i++) {
				ArrayList<ArrayList<Event>> newAllSequences = new ArrayList<ArrayList<Event>>();
				Event e = s.get(i);
				if (seen.contains(e.getSource())) continue;
				ArrayList<ArrayList<Event>> eg = generateEventGroups(e.getSource());
				ArrayList<Event> candidates = new ArrayList<Event>();
				for (ArrayList<Event> g : eg) {
					candidates.add(g.get(0));
				}
				for (ArrayList<Event> existingS : allSequences) {
					for (Event candidate : candidates) {
						ArrayList<Event> newOne = new ArrayList<Event>();
						newOne.addAll(existingS);
						newOne.add(candidate);
						newOne.add(e);
						newAllSequences.add(newOne);
					}
				}
				if (allSequences.size() < 1) {
					for (Event candidate : candidates) {
						ArrayList<Event> newOne = new ArrayList<Event>();
						newOne.add(candidate);
						newOne.add(e);
						if (!newAllSequences.contains(newOne))
							newAllSequences.add(newOne);
					}
				}
				seen.add(e.getSource());
				allSequences = newAllSequences;
			}
			for (ArrayList<Event> ssss: allSequences)
				if (!ret.contains(ssss))
					ret.add(ssss);
		}
		
		if (debugMode) {
			System.out.println("There are " + ret.size() + " enhanced sequences.");
			for (ArrayList<Event> sequence: ret) {
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
		
		return ret;
	}
	
	private ArrayList<ArrayList<Event>> generateUnenhancedSequences()
	{
		ArrayList<ArrayList<Event>> sequences = new ArrayList<ArrayList<Event>>();
		for (String target: targetMethods) {
			for (UIState vertex: knownVertices) {
				ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(knownVertices.get(1), vertex);
				ArrayList<Event> ieel = (ArrayList<Event>) vertex.getIneffectiveEventList();

				for (Event event: ieel) {
					for (String string: event.getMethodHits()) {
						if (string.contains(target.trim().replace("<","").replace(">", ""))) {
							ArrayList<Event> temp = new ArrayList<Event>();
							temp.addAll(el);
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
	
	private ArrayList<ArrayList<Event>> generateEventGroups(UIState uis)
	{
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
