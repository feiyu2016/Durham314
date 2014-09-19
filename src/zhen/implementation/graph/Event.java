package zhen.implementation.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

import android.view.KeyEvent;

import com.android.hierarchyviewerlib.models.ViewNode;


//This is our edge
public class Event extends DefaultEdge{
	
	private String type;
	private ViewNode appliedView;
	private RunTimeLayout source;
	private RunTimeLayout target;
	private List<String> methodHits;
	private Map<String,String> attribute;
	private int pathIndex;
	private boolean isBroken =false;
	private boolean isEffective = true;
	private boolean needRefresh = true;
	
	public int operationCount = 0;
	
	public boolean isBroken() {
		return isBroken;
	}
	public void setBroken(boolean isBroken) {
		this.isBroken = isBroken;
	}
	public Event(String type, ViewNode appliedView){
		System.out.println("Event Created for:"+type);
		if(appliedView!=null)System.out.println(appliedView.id);
		this.appliedView = appliedView;
		this.type = type;
		attribute = new HashMap<String,String>();
	}
	public void setVertices(RunTimeLayout source, RunTimeLayout target){
		this.source = source;
		this.target = target;
	}
	
	public void setSource(RunTimeLayout source) {
		this.source = source;
	}
	public void setTarget(RunTimeLayout target) {
		this.target = target;
	}
	@Override
	public RunTimeLayout getSource(){
		return this.source;
	}
	@Override
	public RunTimeLayout getTarget(){
		return this.target;
	}
	@Override
	public String toString(){
		return type;
	}
	public void setAppliedViewNode(ViewNode view){
		this.appliedView = view;
	}
	public ViewNode getAppliedViewNode(){
		return this.appliedView;
	}
	public void setAttribute(String key,String value){
		this.attribute.put(key, value);
	}
	public String getAttribute(String key){
		return this.attribute.get(key);
	}
	public List<String> getMethodHits() {
		return new ArrayList<String>(methodHits);
	}
	public void addMethodHits(String method) {
		this.methodHits.add(method);
	}
	public int getPathIndex() {
		return pathIndex;
	}
	public void setPathIndex(int pathIndex) {
		this.pathIndex = pathIndex;
	}
	public String getType() {
		return type;
	}
	public boolean needRefresh() {
		return this.needRefresh;
	}
	public void setRefreshFlag(boolean needRefresh) {
		this.needRefresh = needRefresh;
	}
	public void setType(String type) {
		this.type = type;
	}

//	@Override
//	public boolean equals(Object o){
//		if(o instanceof Event){
//			Event e = (Event)o;
//			if(!this.type.equals(e.type)) return false;
//			if(this.appliedView != e.appliedView) return false;
////			if(this.source != e.source) return false;
////			if(this.target != e.target) return false;
//			return true;
//		}
//		return false;
//	}
	
	public static Event getBackEvent(){
		Event event = new Event(EventType.ONBACK,null);
		return event;
	}
	
	public static Event goToLauncherEvent(){
		Event event = new Event(EventType.PRESS,null);
		event.setAttribute("keycode", KeyEvent.KEYCODE_HOME+"");
		return event;
	}
	public static Event adbCommandEvent(String actName, String command){
		Event event = new Event(EventType.ADBCOMMAND,null);
		event.setAttribute("adbcommand", command);
		return event;
	}
	
	static boolean ignoreLayout = true;
	static boolean ignoreNoID = true;
	public static Event[] getAllPossileEventForView(ViewNode node){
		//TODO right now only the onclick event
		//ignore too small one
		if(node.height <= 5 || node.width <= 5){
			return new Event[0];
		}
		
		if(node.willNotDraw){
			return new Event[0];
		}
		if(ignoreLayout){
			String tmp = node.name.toLowerCase();
			if(tmp.contains("layout"))
				return new Event[0];
			if(tmp.contains("container"))
				return new Event[0];
		}
		
		if(node.id.equalsIgnoreCase("no_id")){
			return new Event[0];
		}
		
//		System.out.println(node.namedProperties.get("position") == null);
		String[] xy =node.namedProperties.get("position").value.split(",");
		int x = Integer.parseInt(xy[0]);
		int y = Integer.parseInt(xy[1]);
		Event event = new Event(EventType.ONCLICK,node);
		event.attribute.put("x", (x+node.width/2)+"");
		event.attribute.put("y", (y+node.height/2)+"");
		return new Event[]{event};
	}

}
