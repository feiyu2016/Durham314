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
		for (String target: targetMethods) {
			for (UIState vertex: knownVertices) {
				ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(UIState.Launcher, vertex);
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
	
	private ArrayList<ArrayList<Event>> generateEventGroups(Event event)
	{
				HashMap<String, String> hasSeen = new HashMap<String, String>();
				ArrayList<String> ret = new ArrayList<String>();
				ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(UIState.Launcher, vertex);
				ArrayList<Event> ieel = (ArrayList<Event>) vertex.getIneffectiveEventList();
				
				for (Event event: ieel) {
					for (String string: event.getMethodHits()) {
						System.out.println(string);
						if (hasSeen.containsKey(string.trim())) {
							try {
								String newString = hasSeen.get(string.trim()) + "|" + event.toString().trim().split("in")[0].trim().split(" ")[1].trim();
								hasSeen.put(string.trim(), newString);
							} catch (ArrayIndexOutOfBoundsException e){
								continue;
							}
						}
						else {
							try {
								String newString = event.toString().trim().split("in")[0].trim().split(" ")[1].trim();
								hasSeen.put(string.trim(), newString);
							} catch (ArrayIndexOutOfBoundsException e){
								continue;
							}
						}
					}
				}
				
				for(Entry<String, String> entry: hasSeen.entrySet()) {
					ret.add(entry.getKey() + "%" + entry.getValue());
				}
			}
		
			
			
			return mergeEventGroups(ret);
	}
	
	private ArrayList<String> mergeEventGroups(ArrayList<String> in) {
		ArrayList<String> input = new ArrayList<String>();
		
		for (String string: in) {
			String temp = string.split("%")[1].trim();
			if (!temp.contains(".") && !temp.contains("keycode"))
				input.add(temp);
		}
		
		boolean isIncluded[] = new boolean[input.size()];
        for(int i=0;i<isIncluded.length;i++){
            isIncluded[i] = false;
        }
        
        for(int i=0;i<input.size()-1;i++){
            if(isIncluded[i]) continue;
            String criteria = input.get(i);
            for(int j=0;j<input.size();j++){
                if(i == j || isIncluded[j]){
                    continue;
                }
                String parts[] = input.get(j).split("\\|");
                isIncluded[j] = containsAll(criteria,parts);
            }
        }
        
        ArrayList<String> result = new ArrayList<String>();
        for(int i=0;i<input.size();i++){
            if(!isIncluded[i]) result.add(input.get(i));
        }
        
        if (debugMode) {
        	System.out.println("----MERGED METHOD GROUPS:");
	        for (String string: result) {
	        	System.out.println(string);
	        }
        }
        
        return result;
    }

    private boolean containsAll(String pointer, String[] list) {
        for (String msg : list) {
            if (!pointer.contains(msg)) {
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
