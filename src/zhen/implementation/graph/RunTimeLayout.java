package zhen.implementation.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import zhen.framework.Configuration;
import zhen.packet.ADBControl;
import zhen.packet.Pair;
import inputGeneration.StaticLayout;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.ViewNode.Property;

/**
 * An instance of this class should have a relation of one-to-one correspondence 
 * with the actual layout on the device. 
 * 
 * Upon construction, 
 * 	1.	it should try to find the associated static layout
 * 	2.	build a set Event which could be applied on this layout
 * 
 * @author zhenxu
 *
 */
public class RunTimeLayout {
	
	public static int UNVISITED = 0;
	public static int INPROCESS = 1;
	public static int VISITED = 2;
	
	static int counter = 0;
	private String name;
	private ArrayList<ViewNode> linearReference;
	private ArrayList<Event[]> possibleViewEventList;
	private ArrayList<Event> possibleOtherEventList;
	
	
//	private Map<ViewNode,Map<String,String>> additionalProperty;
	private Map<String,Object> extraInfo = new HashMap<String,Object>();
	
	private boolean isReachable = true;
	private int status = UNVISITED;
	private boolean newLayout; 
	private ViewNode layoutRoot;
	private StaticLayout associated;
	private String actName;
	private List<Event> ineffectiveEvents;
	
	private List<Pair<String,Event>> eventHandlerList = new ArrayList<Pair<String,Event>>();
	
	public int visitCount = 0;
	public boolean isLauncher = false;
	
	public RunTimeLayout(String actName, ViewNode layoutRoot){
//		while(layoutRoot.parent != null)
//			layoutRoot = layoutRoot.parent;
		
		this.layoutRoot = layoutRoot;
		this.actName = actName;
		ineffectiveEvents = new ArrayList<Event>();
		String[] parts = actName.split("\\.");
		name = parts[parts.length - 1]+"_"+counter;
		counter += 1;
		
		if(layoutRoot != null){
			toLinear(layoutRoot); 
			findStaticLayoutAssociation();
			buildEventList();
		}
//		additionalProperty = new HashMap<ViewNode,Map<String,String>>();
	}
	
	public RunTimeLayout(String actName, ViewNode layoutRoot, String name){
		this.layoutRoot = layoutRoot;
		this.actName = actName;
		ineffectiveEvents = new ArrayList<Event>();
		this.name = name;
		
		if(layoutRoot != null){
			toLinear(layoutRoot); 
			findStaticLayoutAssociation();
			buildEventList();
		}
//		additionalProperty = new HashMap<ViewNode,Map<String,String>>();
	}
	

//	boolean isVisited;
	
	

	public void addIneffectiveEvent(Event event){
		ineffectiveEvents.add(event);
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public boolean equals(Object o){
		if( o instanceof RunTimeLayout){
			RunTimeLayout other = (RunTimeLayout)o;
			if(! this.actName.equals(other.actName)) return false;
			return compareViewNode(this.layoutRoot,other.layoutRoot);
		}else if(o instanceof ViewNode){
			ViewNode otherRoot = (ViewNode)o;
			return compareViewNode(this.layoutRoot,otherRoot);
		}else return false;
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
	
	public boolean hasTheSameViewNode(ViewNode other){
		return compareViewNode(this.layoutRoot,other);
	}

	private void toLinear(ViewNode layoutRoot){
		linearReference = new ArrayList<ViewNode>();
		if(layoutRoot == null) return;
		//layout should be a tree. Seem to be a rule to reinforce it. 
		ArrayList<ViewNode> queue = new ArrayList<ViewNode>();
		queue.add(layoutRoot);
		while(!queue.isEmpty()){
			ViewNode node = queue.remove(0);
			linearReference.add(node);
			for(ViewNode child : node.children){
				queue.add(child);
			}
		}
	}
	
	private void findStaticLayoutAssociation(){
		//TODO
	}
	
	private void buildEventList(){ 
		possibleViewEventList = new ArrayList<Event[]>();
		possibleOtherEventList = new ArrayList<Event>();
		ViewNode root = linearReference.get(0);
		while(root.parent!=null){
			root = root.parent;
		}
		
//		root.namedProperties.get(arg0)
		
		//use the stupid way
		for(ViewNode node:this.linearReference){
			ViewNode current = node;
			int x=current.left;
			int y=current.top;
			while(current.parent != null){
				current = current.parent;
				if(current.left < 0 || current.top<0) break; 
				x += current.left;
				y += current.top;
			}
			Property p = new Property();
			p.value =  x+","+y;
			node.namedProperties.put("position",p);
		}
		
//		adb shell dumpsys window 
//		ADBControl.clearStdoutBuffer();
//		ADBControl.sendADBCommand(Configuration.adbPath+" shell dumpsys window | grep mRestrictedOverscanScreen ");
//		String msg = ADBControl.getLatestStdoutMessage();
//		System.out.println(Configuration.adbPath+" shell dumpsys window | grep mRestrictedOverscanScreen :"+msg);
//		int width,height;
//		String parts[] = msg.trim().split(" ")[1].split("x");
//		width = Integer.parseInt(parts[0]);
//		height = Integer.parseInt(parts[1]);
		
		if(this.associated == null){
			for(ViewNode node: linearReference){
				this.possibleViewEventList.add(Event.getAllPossileEventForView(node, 0, 0));
			}
		}else{
			//TODO
		}
		

		//TODO  populate the other event list. e.g. sensor? system?
//		possibleOtherEventList.add(object)
	}
	
	

	
	public ArrayList<Event[]> getPossibleViewEventList() {
		return possibleViewEventList;
	}

	public ViewNode[] findViewsById(String id){
		ArrayList<ViewNode> list = new ArrayList<ViewNode>();
		for(ViewNode node : linearReference){
			if(node.id.equals(id)) list.add(node);
		}
		return list.toArray(new ViewNode[0]);
	}
	
	public ViewNode getLayoutRoot() {
		return layoutRoot;
	}

	public StaticLayout getAssociated() {
		return associated;
	}

	public void setAssociated(StaticLayout associated) {
		this.associated = associated;
	}

	public String getActName() {
		return actName;
	}

	public void setActName(String actName) {
		this.actName = actName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isNewLayout() {
		return newLayout;
	}

	public void setNewLayout(boolean newLayout) {
		this.newLayout = newLayout;
	}
	
	public ArrayList<ViewNode> getNodeListReference(){
		return this.linearReference;
	}
	
	public boolean hasExtraInfo(String key){
		return this.extraInfo.containsKey(key);
	}
	
	public void putExtraInfo(String key, Object value){
		this.extraInfo.put(key, value);
	}
	public Object getExtraInfo(String key){
		return this.extraInfo.get(key);
	}
	
	public void addMatchedEventWithHandler(String handler, Event event){
		this.eventHandlerList.add(new Pair<String,Event>(handler,event));
	}
	
	public ArrayList<Event> getPotentialEventForHandler(String methodName){
		ArrayList<Event> result= new ArrayList<Event>();
		for(int i=0;i<eventHandlerList.size();i++){
			 Pair<String,Event> pair = eventHandlerList.get(i);
			 if(pair.first.equals(methodName)){
				 result.add(pair.second); 
			 }
		}
		return result;
	}

	public List<Pair<String, Event>> getEventHandlerList() {
		return eventHandlerList;
	}

}


//public void setViewNodeExtraProperty(ViewNode node, String key, String value){
//	if(additionalProperty.containsKey(node)){
//		Map<String, String> properties = additionalProperty.get(node);
//		properties.put(key, value);
//	}else{
//		Map<String, String> properties = new HashMap<String, String>();
//		additionalProperty.put(node, properties);
//		properties.put(key, value);
//	}
//}
//
//public String getViewNodeExtraProperty(ViewNode node, String key){
//	if(additionalProperty.containsKey(node)){
//		return additionalProperty.get(node).get(key);
//	}else{
//		return null;
//	}
//}