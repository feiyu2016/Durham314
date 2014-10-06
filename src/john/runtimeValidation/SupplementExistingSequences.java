package john.runtimeValidation;

import java.util.ArrayList;
import java.util.List;

import zhen.framework.Framework;
import zhen.implementation.graph.Event;

public class SupplementExistingSequences {
	
	private Framework thisFW;
	//private ArrayList<ArrayList<String>> methodRelationships;
	
	public SupplementExistingSequences(Framework fw) {
		thisFW = fw;
	}
	
	public void generateMethodCorrelations()
	{
		String[] methods = (String[]) thisFW.attribute.get("methods");
		
		for(String method : methods){
			
			List<List<Event>>  llevent = thisFW.dynamicInfo.findPotentialPathForHandler(method);
			
			for(List<Event> levent: llevent) {
				Event event = levent.get(0);
				System.out.println(method + "," + event.toString());
			}	
		}
	}
}
