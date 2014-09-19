package zhen.backup;

import com.android.hierarchyviewerlib.models.Window;

import zhen.implementation.graph.Event;
import zhen.implementation.graph.GraphStructureLayoutInformation;
import zhen.packet.RunTimeLayoutInformation;

public class DecisionMaker {
	private GraphStructureLayoutInformation graphReference;
	private RunTimeLayoutInformation layoutInfoReference;
	private String launcherIdentifier;
	private String[] actList;
	private boolean[] actReached;
	public DecisionMaker(Framework frame){
		graphReference = frame.getTraversalGraph();
		layoutInfoReference = frame.getLayoutInfo();
	}
	public void init(){
		
	}
	
	public void setActivityScope(String[] actList){
		this.actList = actList;
		actReached = new boolean[actList.length - 1];
	}
	public void setLauncherName(String name){
		launcherIdentifier = name;
	}
	
	public Event generateEvent(){
		Window win = layoutInfoReference.getFocusedWindow();
		
		
		
		
		
		return null;
	}
	
	private boolean isLauncher(Window win){
		return launcherIdentifier.equals(win.getTitle());
	}
}
