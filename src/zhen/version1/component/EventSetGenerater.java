package zhen.version1.component;

import java.util.ArrayList;
import java.util.List;

import zhen.version1.Support.BreadthFirstTreeSearch;
import zhen.version1.Support.Utility;
import zhen.version1.framework.Common;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.ViewNode.Property;

public class EventSetGenerater {
	public static boolean DEBUG = false;
	public static String TAG = "EventSetGenerater";
	

	public static void build(UIState ui){
		
		
		//Build view Events
		ViewNode root = ui.root;
		if(root == null) return;
		
		double actualWidth = ui.winInfo.width;
		double actualHeight = ui.winInfo.height;
		final double xScale = actualWidth/root.width;
		final double yScale = actualHeight/root.height;
		final double startx = ui.winInfo.startx;
		final double starty = ui.winInfo.starty;
		
		if(DEBUG) Utility.log(TAG,"Scale:("+xScale+","+yScale+"), Start:("+startx+","+starty+")");
		
		final List<ViewNode> linearRefernce = new ArrayList<ViewNode>();
		final List<Event> relatedEvent = new ArrayList<Event>();
		BreadthFirstTreeSearch.ChildCollecter<ViewNode> collecter= new BreadthFirstTreeSearch.ChildCollecter<ViewNode>(){
			@Override
			public ViewNode[] collecter(ViewNode parent) {
				return parent.children.toArray(new ViewNode[0]); 
			}
		};
		BreadthFirstTreeSearch.CriticalPoint<ViewNode> operations = new BreadthFirstTreeSearch.CriticalPoint<ViewNode>(){
			@Override
			public void onEnQueue(ViewNode node) {
				linearRefernce.add(node);
			} 
		};
		BreadthFirstTreeSearch.search(root, collecter, operations);
		ui.linearReference = linearRefernce;
		
		for(ViewNode node: linearRefernce){
			ViewNode current = node;
			
			int x_toActivity=current.left;
			int y_toActivity=current.top;
			while(current.parent != null){
				current = current.parent;
				if(current.left < 0 || current.top<0) break; 
				x_toActivity += current.left;
				y_toActivity += current.top;
			}
			
			Property node_activityPosition = new Property();
			node_activityPosition.value =  x_toActivity+","+y_toActivity;
			node.namedProperties.put(Common.node_activity_position,node_activityPosition);//just want to store info
			
			int actual_x = (int) (x_toActivity * xScale + startx);
			int actual_y = (int) (y_toActivity * yScale + starty);

			Property node_actualPosition = new Property();
			node_actualPosition.value =  actual_x+","+actual_y;
			node.namedProperties.put(Common.node_actual_position,node_actualPosition);//just want to store info
			
			if(isIgnored(node)) continue;
			Event clickEvent = Event.getOnClickEvent(actual_x+node.width/2, actual_y+node.height/2);
			
			if(relatedEvent.contains(clickEvent)) continue;
			relatedEvent.add(clickEvent);
		}
		
		ui.setPossibleEventList(relatedEvent);
		ui.setEventIndex(0);
		
		for(Event event: relatedEvent){
			if(DEBUG) Utility.log(TAG,event);
		}
	}

	private static boolean isIgnored(ViewNode node){
//		if(DEBUG) Utility.log(TAG, (node.children != null && node.children.size() > 0)+","+(node.height < 3 && node.width <3));
		
		if(node.children != null && node.children.size() > 0) return true;
		if(node.height < 3 && node.width <3) return true;
		return false;
	}
}
