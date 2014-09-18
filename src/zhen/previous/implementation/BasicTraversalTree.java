package zhen.previous.implementation;

import java.util.ArrayList;
import java.util.List;

import com.android.hierarchyviewerlib.models.ViewNode;

import zhen.previous.framework.AbstractTraversalTree;
import zhen.previous.framework.Event;
import zhen.previous.framework.Framework;

public class BasicTraversalTree extends AbstractTraversalTree {
	
	private ArrayList<TraversalTreeNode> rootList;
	private ArrayList<TraversalTreeNode> lastAccess;
	private TraversalTreeNode current;
	private int index = -1;
	
	public BasicTraversalTree(Framework frame) {
		super(frame);
		rootList = new ArrayList<TraversalTreeNode>();
		lastAccess=new ArrayList<TraversalTreeNode>();
	}

	@Override
	public void extendTree(String actName, ViewNode rootNode, Event... events) {
		TraversalTreeNode toExtend = lastAccess.get(index);
		TraversalTreeNode node = new TraversalTreeNode(actName, rootNode, events);
		
		
//		if(root == null){
//			root = ew TraversalTreeNode(actName, rootNode, events);
//			current = root;
//		}else{
//			TraversalTreeNode node = new TraversalTreeNode(actName, rootNode, events);
//			current.children.add(node);
//			current = node;
//		}
	}

	@Override
	public void createRoot(String actName, ViewNode layout){
		TraversalTreeNode newRoot = new TraversalTreeNode(actName, layout, Event.getLaunchEvent(actName));
		rootList.add(newRoot);
		lastAccess.add(newRoot);
		index+=1;
	}
	
	@Override
	public List<Event> getEventSequenceToCurrentNode() {
		List<Event> sequence = new ArrayList<Event>();
		TraversalTreeNode tmp = current;
		while(tmp != null){
			int index = 0;
			for(Event e : tmp.eventSequence){
				sequence.add(index, e);
				index += 1;
			}
			tmp = tmp.parent;
		}
		return sequence;
	}

	@Override
	public TraversalTreeNode getCurrentNode() {
		return current;
	}

	@Override public void onLaunchApplication() {}
	@Override public void onLaunchApplicationFinish() {}
	@Override public void onQuitApplication() {}
	@Override public boolean init() { return true; }
	@Override public void terminate() { }

	@Override
	public TraversalTreeNode goBack() {
		lastAccess.set(index, lastAccess.get(index).parent);
		current = current.parent;
		return current;
	}
}
