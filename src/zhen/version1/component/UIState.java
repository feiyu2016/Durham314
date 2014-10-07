package zhen.version1.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zhen.version1.Support.Utility;

import com.android.hierarchyviewerlib.models.ViewNode;

/**
 * The basic block of UI model
 * A UIState represent a unique state during traversal
 * Subcomponents include:
 * 	1.	Layout -- a tree of ViewNode
 * 	2.	Events that have been applied	-- ? to review
 * 	3.	Extra Information
 * 	4.	Activity information
 * 
 * 
 * @author zhenxu
 *
 */
public class UIState {
	public static String TAG = "UIState";
	public static boolean DEBUG = true;
	
	private static int avaliableIndex = 0;
	
	public final int index;
	public List<ViewNode> linearReference;
	private List<Event> possibleEventLsit;
	private int eventIndex = -1; 
	private boolean isOnBackTried = false;
	
	private List<Event> ineffectiveEventList = new ArrayList<Event>();
	
	public final ViewNode root;
	public final String actName, appName;
	public final  WindowInformation winInfo;
	
	public int visitCount = 0;
	public boolean isInScopeUI = true;
	public boolean isReachable = true;
	public boolean isLauncher = false;
	
	public static UIState Launcher = new UIState(); 
	private UIState(){
		this("com.android.launcher","com.android.launcher2.Launcher",null,null); 
		isLauncher = true; 
	}
	public void defineLauncher(String appName, String actName){
		Launcher = new UIState(appName,actName,null,null);
		Launcher.isLauncher = true; 
	}
	
	public UIState(String appName, String actName, ViewNode root, WindowInformation winInfo){
		this.index = avaliableIndex;
		avaliableIndex += 1;
		this.actName = actName;
		this.appName = appName==null?"":appName;
		this.root = root;
		this.winInfo = winInfo;
		if(DEBUG) Utility.log(TAG, this.appName +","+this.actName);
		if(this.winInfo != null) EventSetGenerater.build(this);
	}
	
	public void addIneffectiveEvent(Event event){
		this.ineffectiveEventList.add(event);
	}
	
	@Override
	public String toString(){
		String[] title = this.actName.split("\\.");
		return "#"+this.index+" "+title[title.length-1];
	}
	
	@Override
	public boolean equals(Object input){
		if(input instanceof UIState){
			UIState other = (UIState)input;
			return this.index == other.index;
		}else if(input == null||input instanceof ViewNode){
			return compareViewNode(this.root,(ViewNode) input);
		}
		return false;
	}
	public boolean theSameAs(ViewNode root, WindowInformation winInfo){
		if(!this.equals(root)) return false;
		if(this.winInfo == null && winInfo == null){
			return true;
		}else if(this.winInfo != null){
			return this.winInfo.equals(winInfo);
		}
		return false;
	}
	
	private static boolean compareViewNode(ViewNode r1, ViewNode r2){
		//check the class name, id, x,y
		if(r1 == null && r2 == null) return true;
		if(r1 != null && r2 != null){
			if(!r1.name.equals(r2.name))return false;
			if(!r1.id.equals(r2.id)) return false;
			if(r1.left != r2.left) return false;
			if(r1.top != r2.top) return false;
//			if(r1.width != r2.width) return false;
//			if(r1.height != r2.height) return false;
			if(r1.children.size() != r2.children.size()) return false;
			//check children
			List<ViewNode> list1 = r1.children;
			List<ViewNode> list2 = r2.children;
			
			//TODO assume the order is the same
			for(int i=0;i<list1.size();i++){
				ViewNode v1 = list1.get(i);
				ViewNode v2 = list2.get(i);
				if(compareViewNode(v1,v2) == false){
					return false;
				}
			}
		}
		return true;
	}
	
	public void setPossibleEventList(List<Event> eventList) {
		this.possibleEventLsit = eventList;
	} 
	public void setEventIndex(int eventIndex) {
		this.eventIndex = eventIndex;
	}
	public Event getNextPossibleEvent(){
		Event result;
		if(this.eventIndex >= this.possibleEventLsit.size()){
			result= null;
		}else{
			result= this.possibleEventLsit.get(this.eventIndex);
			eventIndex += 1;
		}
		if(DEBUG) Utility.log(TAG,"getNextPossibleEvent, #"+this.eventIndex+","+result);
		return result;
	}
	
	public void setOnBackUsed(){
		this.isOnBackTried = true;
	}
	public boolean isOnBackUsed(){
		return this.isOnBackTried;
	}
	
	public ArrayList<Event> getIneffectiveEventList(){
		return (ArrayList<Event>) ineffectiveEventList;
	}
	
}
