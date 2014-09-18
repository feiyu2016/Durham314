package zhen.previous.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.hierarchyviewerlib.models.ViewNode;

public abstract class AbstractTraversalTree extends AbstractBaseFrameworkComponent{

	public AbstractTraversalTree(Framework frame) {
		super(frame);
	}
	
	public abstract void createRoot(String actName, ViewNode layout);
	public abstract void extendTree(String actName, ViewNode layout, Event... events);
	public abstract TraversalTreeNode goBack();
	public abstract List<Event> getEventSequenceToCurrentNode();
	public abstract TraversalTreeNode getCurrentNode();
	
	public static interface LayoutMatcher{
		public int relationBetweenLayout(ViewNode layout1, ViewNode layout2);
	}
	
	public static class TraversalTreeNode{
		public TraversalTreeNode(){}
		
		public TraversalTreeNode(String actName, ViewNode layout, Event... events){
			this.actName = actName;
			this.layoutRoot = layout;
			eventSequence = new ArrayList<Event>();
			for(Event e : events){ eventSequence.add(e); }
			additionalInfo = new HashMap<String, String>();
			children = new ArrayList<TraversalTreeNode> ();
		}
		private String actName;
		private ViewNode layoutRoot;
		private List<Event> eventSequence;
		private TraversalTreeNode parent;
		private List<TraversalTreeNode> children;
		public Map<String,String> additionalInfo;
		
		public String getActName(){
			return actName;
		}
		public ViewNode getLayout(){
			return this.layoutRoot;
		}
		public List<Event> getEventSequence(){
			return this.eventSequence;
		}
		public TraversalTreeNode getParent(){
			return this.parent;
		}
		public List<TraversalTreeNode> getChildList(){
			return this.children;
		}
		public void addChild(TraversalTreeNode node){
			this.children.add(node);
		}
		
		
		
	}
}
