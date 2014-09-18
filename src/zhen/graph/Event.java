package zhen.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

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
	
	public boolean isBroken() {
		return isBroken;
	}
	public void setBroken(boolean isBroken) {
		this.isBroken = isBroken;
	}
	public Event(String type, ViewNode appliedView){
		this.appliedView = appliedView;
		this.type = type;
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
	public void addAttribute(String key,String value){
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
}
