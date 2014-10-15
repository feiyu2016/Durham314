package john.generateSequences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import zhen.version1.framework.Framework;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;

public class GenerateSequences {
	
	private Framework fw;
	private ArrayList<UIState> knownVertices;
	private String[] targetMethods;
	private ArrayList<String> methodGroups;
	private ArrayList<String> unenhancedSequences;
	private ArrayList<String> enhancedSequences;
	
	public boolean debugMode;
	
	public GenerateSequences(Framework fw, String[] targetMethods, boolean debugMode) {
		this.fw = fw;
		this.targetMethods = targetMethods;
		this.debugMode = debugMode;
		knownVertices = this.getKnownVertices();
		methodGroups = this.generateMethodGroups();
		unenhancedSequences = this.generateUnenhancedSequences();
		enhancedSequences = this.generateEnhancedSequences();
	}
	
	public ArrayList<String> getMethodGroups()
	{
		return methodGroups;
	}
	
	public ArrayList<String> getUnenhancedSequences()
	{
		return unenhancedSequences;
	}
	
	public ArrayList<String> getEnhancedSequences()
	{
		return enhancedSequences;
	}
	
	private ArrayList<String> generateEnhancedSequences()
	{
		ArrayList<String> uS = new ArrayList<String>();
		ArrayList<String> ret = new ArrayList<String>();
		ArrayList<String> mG = methodGroups;
		
		for (String string: unenhancedSequences) {
			uS.add(string.trim().split("\\|")[1].trim());
		}
		
		for (String string: uS) {
			String combo = "";
			for (String group: mG) {
				if (group.contains(string)) {
					ret.add(string);
				}
				else {
					String temp = group.trim().split("\\|")[0] + "|";
					ret.add(temp + string);
					combo += temp;
				}
			}
			ret.add(combo + string);
		}
		
		return ret;
	}
	
	private ArrayList<String> generateUnenhancedSequences()
	{
		ArrayList<String> sequences = new ArrayList<String>();
		for (String target: targetMethods) {
			for (UIState vertex: knownVertices) {
				ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(UIState.Launcher, vertex);
				ArrayList<Event> ieel = (ArrayList<Event>) vertex.getIneffectiveEventList();
				
				for (Event event: el) {
					for (String string: event.getMethodHits()) {
						if (string.contains(target.trim().replace("<","").replace(">", ""))) {
							sequences.add(target + "|" + eventSequenceToString(el) + event.toString().trim().split("in")[0].trim());
							break;
						}
					}
				}
				
				for (Event event: ieel) {
					for (String string: event.getMethodHits()) {
						if (string.contains(target.trim().replace("<","").replace(">", ""))) {
							sequences.add(target + "|" + eventSequenceToString(el) + event.toString().trim().split("in")[0].trim());
							break;
						}
					}
				}
				
			}
		}
		
		ArrayList<String> ret = new ArrayList<String>();
		for (String sequence: sequences) {
			//sequence = sequence.replace("<", "").replace(">", "");
			sequence = sequence.split("\\>")[1];
			String temp = "|";
			for (int i = 2; i < sequence.split("\\|").length; i++) {
				temp += sequence.split("\\|")[i] + "|";
			}
			sequence = temp;
			if (sequence.trim().contains("|android:onClick ")) {
				ret.add(sequence.trim().replace("|android:onClick ", "|"));
			}
			else {
				ret.add(sequence.trim());
			}
		}
		
		if (debugMode) {
			for (String sequence: ret)
				System.out.println(sequence);
		}
			
		return ret;
	}
	
	private ArrayList<String> generateMethodGroups()
	{
		HashMap<String, String> hasSeen = new HashMap<String, String>();
		ArrayList<String> ret = new ArrayList<String>();
		
		//for (String target: targetMethods) {
			for (UIState vertex: knownVertices) {
				ArrayList<Event> el = (ArrayList<Event>) fw.rInfo.getEventSequence(UIState.Launcher, vertex);
				ArrayList<Event> ieel = (ArrayList<Event>) vertex.getIneffectiveEventList();
				
				for (Event event: el) {
					for (String string: event.getMethodHits()) {
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
				
				for (Event event: ieel) {
					for (String string: event.getMethodHits()) {
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
			}
		
			for(Entry<String, String> entry: hasSeen.entrySet()) {
				ret.add(entry.getKey() + "%" + entry.getValue());
			}
			
			return mergeMethodGroups(ret);
	}
	
	public void printKnownVertices()
	{	
		for (UIState vertex: knownVertices) {
			@SuppressWarnings("static-access")
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
	
	private ArrayList<String> mergeMethodGroups(ArrayList<String> in) {
		ArrayList<String> input = new ArrayList<String>();
		
		for (String string: in) {
			String temp = string.split("%")[1].trim();
			if (!temp.contains("."))
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
	
	private String eventSequenceToString(ArrayList<Event> el)
	{
		String string = "";
		
		for (Event event: el) {
			string += event.toString().trim().split("in")[0].trim() + "|";
		}
		
		return string;
	}
}
