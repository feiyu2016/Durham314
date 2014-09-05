package zhen.framework;

import java.util.List;
import java.util.Map;

import com.android.hierarchyviewer.scene.ViewNode;


public abstract class AbstractTraversalTree extends AbstractBaseFrameworkComponent{

	public AbstractTraversalTree(Framework frame) {
		super(frame);
	}
	
	public abstract void extendTree(String actName, ViewNode root, Event... events);
	public abstract void extendTree(TraversalTreeNode node);
	public abstract List<Event> getEventSequenceToCurrentNode();
	public abstract TraversalTreeNode getCurrentNode();
	
	public static interface LayoutMatcher{
		public int relationBetweenLayout(ViewNode layout1, ViewNode layout2);
	}
	 	
	public static class TraversalTreeNode{
		public ViewNode layoutRoot;
		public List<Event> triggerEvents;
		public TraversalTreeNode parent;
		public List<TraversalTreeNode> children;
//		private StaticLayout staticLayout;
		public Map<String,String> additionalInfo;
	}
}
