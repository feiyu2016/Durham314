package john.supplementsequences;

import java.util.ArrayList;
import java.util.List;

import zhen.version1.framework.Framework;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;

public class SupplementExistingSequences {
	
	private Framework fw;
	private ArrayList<UIState> knownVertices;
	//private ArrayList<ArrayList<String>> methodRelationships;
	
	public SupplementExistingSequences(Framework fw) {
		this.fw = fw;
		knownVertices = getKnownVertices();
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
	
	private void printKnownVertices()
	{	
		int i = 0;
		for (UIState vertex: knownVertices) {
			vertex.
			System.out.println();
		}
	}
	
	private ArrayList<UIState> getKnownVertices()
	{
		return fw.rInfo.getUIModel().getKnownVertices();
	}
	
}
