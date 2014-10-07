package john.supplementsequences;

import java.util.ArrayList;
import java.util.List;

import zhen.version1.framework.Framework;
import zhen.version1.component.Event;

public class SupplementExistingSequences {
	
	private Framework thisFW;
	//private ArrayList<ArrayList<String>> methodRelationships;
	
	public SupplementExistingSequences(Framework fw) {
		thisFW = fw;
	}
	
	public void generateMethodButtonCorrelations()
	{
		String[] methods = (String[]) thisFW.getAttributes().get("methods");
		ArrayList<String> retList = new ArrayList<String>();
		
		for(String method : methods){
			
			List<List<Event>>  llevent = thisFW.rInfo.findPotentialPathForHandler(method);
			String retMethod = method;
			for(List<Event> levent: llevent) {
				Event event = levent.get(0);
				retMethod = retMethod + "," + event.toString();
				//System.out.println(method + "," + event.toString());
			}
			
			retList.add(retMethod);
		}
	}
}
