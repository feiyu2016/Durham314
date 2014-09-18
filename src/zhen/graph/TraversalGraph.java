package zhen.graph;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.demo.JGraphAdapterDemo;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import zhen.packet.Utility;

import com.android.hierarchyviewerlib.models.ViewNode;

public class TraversalGraph {

	public static String TAG = "TraversalGraph";
	
	public static int INSCOPE_NEW 			= 0;
	public static int INSCOPE_ENCOUNTERED	= 1;
	public static int INSCOPE_REPEATIVE		= 2;
	
	public static int OUTSCOPE_NEW			= 3;
	public static int OUTSCOPE_ENCOUNTERED	= 4;
	public static int OUTSCOPE_REPEATIVE	= 5;
	
	public static int SPECIAL_LAUNCHER		= 6;
	public static int SPECIAL_LAUNCHER_REPEATIVE	= 7;
	public static int SPECIAL_OTHER			= 8;

//	public static int INSCOPE = 0x0100;
//	public static int OUTSCOPE = 0x0200;
//	public static int SPEICAL = 0x0600;
//	public static int NEW_LAYOUT = 0x0001;
//	public static int KNOWN_LAYOUT = 0x0002;
//	public static int REPEATIVE_LAYOUT = 0x0006;
	
	private String appName;
	//the actual name of launcher;
	private String launcherActName;
	
	//for each activity. list all encountered different layout 
	private Map<String, Collection<RunTimeLayout>> actCategoryReference = new HashMap<String, Collection<RunTimeLayout>>();
	private Map<String, Collection<RunTimeLayout>> outOfScopeReference = new HashMap<String, Collection<RunTimeLayout>>();
	//the class to store the collection of layout
	private Class containerType = ArrayList.class;
	//the graph that organizes the connection between layout
	private ListenableGraph<RunTimeLayout, Event> graph;
	//the layout access stack
	private Stack<RunTimeLayout> accessStack;
	//the root for all connection
	private RunTimeLayout launcher;
	
	public TraversalGraph(){
		accessStack = new Stack<RunTimeLayout>(){
			//TODO might want to modify pop
		};
		graph = new ListenableDirectedMultigraph<RunTimeLayout, Event>(Event.class);
	}
	//set up a list of activity names, usually should be done at the beginning.
	public void defineActivityScope(String[] actName){
		for(String name:actName){
			getLayoutCollection(actCategoryReference,name);
		}
	}
	//set the application name
	public void setApplicationName(String appName){
		this.appName = appName;
	}
	//set the name of the launcher activity
	public void setLauncherActName(String name){
		this.launcherActName = name;
		launcher = new RunTimeLayout(name,null,"launcher");
		this.graph.addVertex(launcher);
	}
	
	public void enableGUI(){
		final LayoutRelationViewer applet = new LayoutRelationViewer(graph);
        applet.init();
        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("LayoutRelationViewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
	}
	
	/**
	 * extend the graph from current layout to another layout if it is new
	 * Assumption: 
	 * 	1. no special activity will be launched by AM
	 * 
	 * @param eventType				the eventType applied on
	 * @param focusedActName		the focused activity name
	 * @param inputRoot					the root of the layout
	 * @return
	 */
	public int extendGraph(Event event, String focusedActName, ViewNode inputRoot){		
		Collection<RunTimeLayout> inScopelayoutCollection  = actCategoryReference.get(focusedActName);
		boolean isLayoutInscope = (inScopelayoutCollection!=null);
		if(isLayoutInscope){
			String type = event.getType();
			RunTimeLayout currentToBeUsed =  addToCollection(inScopelayoutCollection,focusedActName,inputRoot);
			if(type.equals(EventType.LAUNCH)){
				event.setVertices(launcher, currentToBeUsed);
				addDirectedEdge(launcher, currentToBeUsed,event);
				if(currentToBeUsed.visitCount >= 0){
					Utility.log(TAG,"Launch activity;Visited;"+focusedActName);
					accessStack.push(currentToBeUsed);
					return INSCOPE_ENCOUNTERED;
				}else{
					Utility.log(TAG,"Launch activity;unVisited;"+focusedActName);
					currentToBeUsed.visitCount += 1;
					accessStack.push(currentToBeUsed);
					return INSCOPE_NEW;
				}
			}else{ // an normal event from click, swipe, onback
				RunTimeLayout previous = this.accessStack.peek();
				//did not expect this happens
				if(previous == null) throw new AssertionError();
				 
				if(currentToBeUsed.equals(previous)){ // encounter previous layout
					event.setVertices(previous, previous);
					previous.addIneffectiveEvent(event);
					Utility.log(TAG,"Encounter previous layout;"+focusedActName);
					return INSCOPE_REPEATIVE;
				}else if(currentToBeUsed.visitCount >= 0){ //encounter old layout
					event.setVertices(previous, currentToBeUsed);
					addDirectedEdge(previous, currentToBeUsed,event);
					Utility.log(TAG,"Encounter knwon layout;"+focusedActName);
					accessStack.push(currentToBeUsed);
					return INSCOPE_ENCOUNTERED;
				}else{ //encounter new layout 
					event.setVertices(previous, currentToBeUsed);
					addDirectedEdge(previous, currentToBeUsed,event);
					Utility.log(TAG,"Encounter new layout;"+focusedActName);
					currentToBeUsed.visitCount +=1;
					accessStack.push(currentToBeUsed);
					return INSCOPE_NEW;
				}
			}
		}
		
		//the layout is not within scope, it might be a special one
		//only check launcher right now TODO
		if(isSpecialActivity(focusedActName)){
			boolean isCurrentLayoutLauncher = focusedActName.equals(this.launcherActName);
			//did not expect this happens
			if(accessStack.isEmpty()) throw new AssertionError();
			RunTimeLayout previous = accessStack.peek();
			boolean isPreviousLaotLauncher = previous.getActName().equals(launcherActName);
			if(isCurrentLayoutLauncher && isPreviousLaotLauncher){
				Utility.log(TAG,"Encounter launcher repeatively.");
				return SPECIAL_LAUNCHER_REPEATIVE; 
			}else if(isCurrentLayoutLauncher){
				event.setVertices(previous, this.launcher);
				addDirectedEdge(previous, launcher,event);
				Utility.log(TAG,"Encounter launcher.");
				return SPECIAL_LAUNCHER;
			}
		}
		
		//the focused activity must be within another app
		{
			Collection<RunTimeLayout> outScopeLayoutCollection = getLayoutCollection(outOfScopeReference, focusedActName);
			RunTimeLayout currentToBeUsed =  addToCollection(outScopeLayoutCollection,focusedActName, inputRoot);
			String type = event.getType();
			if(type.equals(EventType.LAUNCH)){
				event.setVertices(launcher, currentToBeUsed);
				addDirectedEdge(launcher, currentToBeUsed,event);
				if(currentToBeUsed.visitCount >= 0){
					Utility.log(TAG,"Launch activity;Visited layout;"+focusedActName);
					accessStack.push(currentToBeUsed);
					return OUTSCOPE_ENCOUNTERED;
				}else{
					Utility.log(TAG,"Launch activity;unVisited layout;"+focusedActName);
					currentToBeUsed.visitCount +=1;
					accessStack.push(currentToBeUsed);
					return OUTSCOPE_NEW;
				}
			}else{
				if(this.accessStack.isEmpty()) throw new AssertionError();
				RunTimeLayout previous = accessStack.peek();
				if(previous.equals(currentToBeUsed)){
					event.setVertices(previous, null);
					previous.addIneffectiveEvent(event);
					Utility.log(TAG,"Out of scope; Repeatively;"+focusedActName);
					return OUTSCOPE_REPEATIVE;
				}else if(previous.visitCount >= 0){
					event.setVertices(previous, currentToBeUsed);
					addDirectedEdge(previous, currentToBeUsed,event);
					Utility.log(TAG,"Out of scope; known layout;"+focusedActName);
					accessStack.push(currentToBeUsed);
					return OUTSCOPE_ENCOUNTERED;
				}else{
					event.setVertices(previous, currentToBeUsed);
					addDirectedEdge(previous, currentToBeUsed,event);
					Utility.log(TAG,"Out of scope; unknwon layout;"+focusedActName);
					currentToBeUsed.visitCount +=1;
					accessStack.push(currentToBeUsed);
					return OUTSCOPE_NEW;
				}
			}
		}
	}
	
	
	private boolean isSpecialActivity(String actName){
		return actName.equals(this.launcherActName);
	}
	
	private void addDirectedEdge(RunTimeLayout source, RunTimeLayout target, Event event){
		//TODO might want to switch other way
		//Event bundles
		graph.addEdge(source, target,event);
	}

	private RunTimeLayout addToCollection(Collection<RunTimeLayout> collection,String actName, ViewNode root){
		//try to determine if this is already existed. 
		//the RunTimeLayout be returned must be new or existed RunTimeLayout in graph
		//TODO in the future might want to auto-update if necessary 
		Iterator<RunTimeLayout> iter = collection.iterator();
		while(iter.hasNext()){
			RunTimeLayout pointer = iter.next();
			if(pointer.equals(root)) return pointer;
		}
		RunTimeLayout layout = new RunTimeLayout(actName,root);
		graph.addVertex(layout);
		collection.add(layout);
		return layout;
	}
	
	@SuppressWarnings("unchecked")
	private Collection<RunTimeLayout> getLayoutCollection(Map<String, Collection<RunTimeLayout>> map, String actName){
		if(map.containsKey(actName)){
			return map.get(actName);
		}else{
			Collection<RunTimeLayout> collection;
			try {
				collection = (Collection<RunTimeLayout>) containerType.newInstance();
				map.put(actName, collection);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new AssertionError("new collection instance creaction failure");
			}
			return collection;
		}
	}


	private static class ListenableDirectedMultigraph<V, E> extends
			DefaultListenableGraph<V, E> implements DirectedGraph<V, E> {
		private static final long serialVersionUID = 1L;

		ListenableDirectedMultigraph(Class<E> edgeClass) {
			super(new DirectedMultigraph<V, E>(edgeClass));
		}
	}
	
	private static class LayoutRelationViewer extends JApplet {
		private static final Color DEFAULT_BG_COLOR = Color
				.decode("#FAFBFF");
		private static final Dimension DEFAULT_SIZE = new Dimension(800,
				600);
		private JGraphModelAdapter<RunTimeLayout, Event> jgAdapter;
		private ListenableGraph g;

		public LayoutRelationViewer(ListenableGraph g) {
			this.g = g;
		}

		public void init() {
			jgAdapter = new JGraphModelAdapter<RunTimeLayout, Event>(g);
			JGraph jgraph = new JGraph(jgAdapter);
			adjustDisplaySettings(jgraph);
			getContentPane().add(jgraph);
			resize(DEFAULT_SIZE);
		}

		private void adjustDisplaySettings(JGraph jg) {
			jg.setPreferredSize(DEFAULT_SIZE);
			Color c = DEFAULT_BG_COLOR;
			String colorStr = null;
			try {
				colorStr = getParameter("bgcolor");
			} catch (Exception e) {
			}
			if (colorStr != null) {
				c = Color.decode(colorStr);
			}
			jg.setBackground(c);
		}
		
	    private void positionVertexAt(Object vertex, int x, int y){
	        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
	        AttributeMap attr = cell.getAttributes();
	        Rectangle2D bounds = GraphConstants.getBounds(attr);

	        Rectangle2D newBounds =
	            new Rectangle2D.Double(
	                x,
	                y,
	                bounds.getWidth(),
	                bounds.getHeight());

	        GraphConstants.setBounds(attr, newBounds);

	        // TODO: Clean up generics once JGraph goes generic
	        AttributeMap cellAttr = new AttributeMap();
	        cellAttr.put(cell, attr);
	        jgAdapter.edit(cellAttr, null, null, null);
	    }
	}
}
