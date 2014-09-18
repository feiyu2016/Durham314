package zhen.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import inputGeneration.StaticLayout;

import com.android.hierarchyviewerlib.models.ViewNode;

//this is our node for the graph
public class RunTimeLayout {
	static int counter = 0;
	private String name;
	public RunTimeLayout(String actName, ViewNode layoutRoot){
		this.layoutRoot = layoutRoot;
		this.actName = actName;
		ineffectiveEvents = new ArrayList<Event>();
		String[] parts = actName.split("\\.");
		name = parts[parts.length - 1]+"_"+counter;
		counter += 1;
	}
	
	public RunTimeLayout(String actName, ViewNode layoutRoot, String name){
		this.layoutRoot = layoutRoot;
		this.actName = actName;
		ineffectiveEvents = new ArrayList<Event>();
		this.name = name;
		counter += 1;
	}
	
	ViewNode layoutRoot;
	StaticLayout associated;
	String actName;
	List<Event> ineffectiveEvents;
//	boolean isVisited;
	
	int visitCount = 0;

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
			if(r1.width != r2.width) return false;
			if(r1.height != r2.height) return false;
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

	public ViewNode getLayoutRoot() {
		return layoutRoot;
	}

	public void setLayoutRoot(ViewNode layoutRoot) {
		this.layoutRoot = layoutRoot;
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
	
}
