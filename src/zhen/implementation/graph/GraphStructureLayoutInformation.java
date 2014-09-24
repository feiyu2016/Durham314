package zhen.implementation.graph;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Stack;

import javax.swing.JApplet;
import javax.swing.JFrame;

import main.Paths;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.demo.JGraphAdapterDemo;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import zhen.framework.AbstractDynamicInformation;
import zhen.framework.AbstractExecuter.LogCatFeedBack;
import zhen.framework.Configuration;
import zhen.framework.Framework;
import zhen.packet.Pair;
import zhen.packet.RunTimeLayoutInformation;
import zhen.packet.Utility;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;
import com.jgraph.algebra.JGraphAlgebra;
import com.jgraph.algebra.cost.JGraphCostFunction;


/**
 * Keep record of the current device layout information
 * Treat RunTimeLayout as node while Event as edge. 
 * 
 * Based on the information collected, the class should find 
 * the event sequence from layout source to destination. 
 * 
 * When a new layout which is in the activity list is encountered,
 * an new RunTimeLayout will be created. 
 * 
 * No detailed layout information will be gathered for launcher or
 * activities out of list.
 * 
 * Upon updating, if length of event is larger than 1, graph will not 
 * be updated. 
 * 
 * @author zhenxu
 *
 */
public class GraphStructureLayoutInformation extends AbstractDynamicInformation{

	public static String TAG = "TraversalGraph";
	
	private String appName;
	//the actual name of launcher;
	private String launcherActName;
	
	private RunTimeLayoutInformation layoutInfo;
	
	//for each activity. list all encountered different layout 
	private Map<String, Collection<RunTimeLayout>> actCategoryReference = new HashMap<String, Collection<RunTimeLayout>>();
	private Map<String, Collection<RunTimeLayout>> outOfScopeReference = new HashMap<String, Collection<RunTimeLayout>>();
	//for an activity, there should be a map between id and a list layout
	//TODO well this structure is awkward... may want to change it sometime 
	private Map<String, Map<String,ArrayList<RunTimeLayout>>> idLayoutMap = new HashMap<String, Map<String,ArrayList<RunTimeLayout>>>();
	
	//the class to store the collection of layout
	private Class containerType = ArrayList.class;
	//the graph that organizes the connection between layout
	private ListenableGraph<RunTimeLayout, Event> graph;
	//the layout access stack
	private RunTimeLayout pointerLayout;
	//the root for all connection
	private RunTimeLayout launcher;
	
	public GraphStructureLayoutInformation(Framework frame){
		super(frame);
		graph = new ListenableDirectedMultigraph<RunTimeLayout, Event>(Event.class);
		layoutInfo = new RunTimeLayoutInformation(Paths.adbPath);
	}
	
	@Override
	public void update(Event... events) {
		if(events == null) return;
		List<List<String>>  feedback = this.frame.executer.getInstrumentationFeedBack();
		if(events.length > 1){
			//only update the reference
			String focusedActName = layoutInfo.getFocusedWindow().getTitle();
			boolean isLauncher = focusedActName.equals(this.launcherActName);
			if(isLauncher){
				this.pointerLayout = this.launcher;
				return;
			}
			
			Collection<RunTimeLayout> inScopeCollection = actCategoryReference.get(focusedActName);
			boolean isInScopeActivity = (inScopeCollection != null);
			if(isInScopeActivity){
				System.out.println("isInScopeActivity");
				ViewNode layoutRoot = layoutInfo.loadFocusedWindowData();
				RunTimeLayout currentToBeUsed =  addToCollection(inScopeCollection,focusedActName,layoutRoot);
				this.pointerLayout = currentToBeUsed;
				return;
			}
			
			{
				Collection<RunTimeLayout> outScopeLayoutCollection = getLayoutCollection(outOfScopeReference, focusedActName);
				RunTimeLayout currentToBeUsed =  addToCollection(outScopeLayoutCollection,focusedActName, null);
				this.pointerLayout = currentToBeUsed;
				return;
			}
		}
		
		Event event = events[0];
		if(event == null) return;
		
		if(event.getType().equals(EventType.SETUP)){
			Window win = layoutInfo.getFocusedWindow();
			this.setLauncherActName(win.getTitle());
			return;
		}
		
//		if(!event.needRefresh()) return ;
		if(pointerLayout != null){pointerLayout.setNewLayout(false);}
		
		Window focused = layoutInfo.getFocusedWindow();
		String focusedActName = focused.getTitle();
		boolean isLauncher = focusedActName.equals(this.launcherActName);
		if(isLauncher){
			if(pointerLayout == null){
				throw new AssertionError();
			}
			
			boolean isPreviousLauncher = pointerLayout.getActName().equals(launcherActName);
			if(isPreviousLauncher) return; //no edge needed
			else{//pointerLayout -> launcher
				event.setVertices(pointerLayout, launcher);
				matchViewWithMethod(event,feedback.get(0));
				addDirectedEdge(pointerLayout, launcher,event);
//				this.frame.getLayoutStack().push(launcher);
				pointerLayout = launcher;
				return;
			}
		}
		
		Collection<RunTimeLayout> inScopeCollection = actCategoryReference.get(focusedActName);
		boolean isInScopeActivity = (inScopeCollection != null);
		if(isInScopeActivity){//needs to retrieve information in detail
			ViewNode layoutRoot = layoutInfo.loadFocusedWindowData();
			RunTimeLayout currentToBeUsed =  addToCollection(inScopeCollection,focusedActName,layoutRoot);
			String type = event.getType();
			if(type.equals(EventType.LAUNCH)){
				event.setVertices(launcher, currentToBeUsed);
				matchViewWithMethod(event,feedback.get(0));
				addDirectedEdge(launcher, currentToBeUsed,event);			
			}else if( currentToBeUsed == pointerLayout){
				 //the same layout 
				event.setVertices(pointerLayout, currentToBeUsed);
				matchViewWithMethod(event,feedback.get(0));
				currentToBeUsed.addIneffectiveEvent(event);
			}
			else {
				if(pointerLayout == null) throw new AssertionError();
				event.setVertices(pointerLayout, currentToBeUsed);
				matchViewWithMethod(event,feedback.get(0));
				addDirectedEdge(pointerLayout, currentToBeUsed,event);		
			}
			pointerLayout = currentToBeUsed;
			return;
		}
		
		{	//out scope activity
			String type = event.getType();
			Collection<RunTimeLayout> outScopeLayoutCollection = getLayoutCollection(outOfScopeReference, focusedActName);
			RunTimeLayout currentToBeUsed =  addToCollection(outScopeLayoutCollection,focusedActName, null);
			if(type.equals(EventType.LAUNCH)){
				event.setVertices(launcher, currentToBeUsed);
				matchViewWithMethod(event,feedback.get(0));
				addDirectedEdge(launcher, currentToBeUsed,event);			
			}else if(pointerLayout.equals(currentToBeUsed)){
				pointerLayout = currentToBeUsed;
				pointerLayout.addIneffectiveEvent(event);
				matchViewWithMethod(event,feedback.get(0));
			}else{
				if(pointerLayout == null) throw new AssertionError();
				event.setVertices(pointerLayout, currentToBeUsed);
				matchViewWithMethod(event,feedback.get(0));
				addDirectedEdge(pointerLayout, currentToBeUsed,event);	
			}
			if(pointerLayout.getActName().equals(currentToBeUsed.getActName())){
				//the same as before, no need to do anything
			}else{
//				this.frame.getLayoutStack().push(currentToBeUsed);
				pointerLayout = currentToBeUsed;
			}
		}
	}
	@Override
	public boolean init(Map<String,Object> attributes) {
		layoutInfo.init();
		String[] actList = (String[]) attributes.get("actlist");
		this.defineActivityScope(actList);
		
		appName = (String) attributes.get("package");
		
		this.enableGUI();
		
		
		return true;
	}
	@Override
	public void terminate() {
		layoutInfo.terminate();
		
	}
	
	@Override
	public RunTimeLayout getCurrentLayout() {
		return pointerLayout;
	}

	//set up a list of activity names, usually should be done at the beginning.
	public void defineActivityScope(String[] actName){
		for(String name:actName){
			getLayoutCollection(actCategoryReference,name);
			idLayoutMap.put(name, new HashMap<String,ArrayList<RunTimeLayout>>());
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
		launcher.isLauncher = true;
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
			if(pointer.hasTheSameViewNode(root)) {
				pointer.visitCount += 1;
				return pointer;
			}
		}
		RunTimeLayout layout = new RunTimeLayout(actName,root);
		layout.setNewLayout(true);
		layout.visitCount += 1;
		//build known id to know layout 
		
		if(root != null){ // which means this is inscope
			Map<String, ArrayList<RunTimeLayout>> idMap = idLayoutMap.get(actName);
			for(ViewNode node: layout.getNodeListReference()){
				String id = node.id;
				ArrayList<RunTimeLayout> layoutList  = null;
				if(idMap.containsKey(id)){
					layoutList = idMap.get(id);
				}else{
					layoutList = new ArrayList<RunTimeLayout>();
					idMap.put(id, layoutList);
				}
				layoutList.add(layout);
			}
		}
		
//		System.out.println("graph.addVertex");
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

	public List<Event> getEventSequence(RunTimeLayout src, RunTimeLayout dest){
		List<Event> sequence1 = DijkstraShortestPath.findPathBetween(graph, this.pointerLayout, dest);
		if(sequence1 == null){
			sequence1 = DijkstraShortestPath.findPathBetween(graph, src, dest);
			Event stopCommand = new Event(EventType.ADBCOMMAND,null);
			stopCommand.setAttribute("adbcommand", "adb shell am force-stop "+appName);
			
//			Event startCommand = new Event(EventType.ADBCOMMAND,null);
//			startCommand.setAttribute("adbcommand", "adb shell am start -n "+dest.getActName());
			
			sequence1.add(0,stopCommand);
//			sequence1.add(1,startCommand);
			
		}
		
		if(sequence1 == null){
			System.out.println("get getEventSequence fails");
		}else{
			System.out.println(sequence1);
		}
//		Event goHome = Event.goToLauncherEvent();
//		Event stopEvent = Event.adbCommandEvent(actName, command);
//		sequence1.add(arg0);
//		List<Event> sequence2 = DijkstraShortestPath.findPathBetween(graph, this.pointerLayout, dest);
		return sequence1;
	}
	
	@Override
	public List<Event> getEventSequence(RunTimeLayout dest) {
		List<Event> sequence1 = DijkstraShortestPath.findPathBetween(graph, this.pointerLayout, dest);
		if(sequence1 == null){
			sequence1 = DijkstraShortestPath.findPathBetween(graph, this.launcher, dest);
			Event stopCommand = new Event(EventType.ADBCOMMAND,null);
			stopCommand.setAttribute("adbcommand", "adb shell am force-stop "+appName);
			
//			Event startCommand = new Event(EventType.ADBCOMMAND,null);
//			startCommand.setAttribute("adbcommand", "adb shell am start -n "+dest.getActName());
			
			sequence1.add(0,stopCommand);
//			sequence1.add(1,startCommand);
			
		}
		
		if(sequence1 == null){
			System.out.println("get getEventSequence fails");
		}else{
			System.out.println(sequence1);
		}
//		Event goHome = Event.goToLauncherEvent();
//		Event stopEvent = Event.adbCommandEvent(actName, command);
//		sequence1.add(arg0);
//		List<Event> sequence2 = DijkstraShortestPath.findPathBetween(graph, this.pointerLayout, dest);
		return sequence1;
	}

	public ArrayList<RunTimeLayout> findPotentialLayout(String actName, String xmlName, String id) {
		System.out.println("findPotentialLayout:"+actName+","+id);
		
		Map<String, ArrayList<RunTimeLayout>>  mapping = idLayoutMap.get(actName);
		if(mapping == null){ return null; }
		return mapping.get(id);
	}
	
	
	private void matchViewWithMethod(Event event, List<String> feedback){
		RunTimeLayout source = event.getSource();
//		I(10395:10395) METHOD_STARTING,<com.example.backupHelper.BackupActivity: boolean onMenuItemSelected(int android.view.MenuItem)>
		//to 
		//com.example.backupHelper.BackupActivity: boolean onMenuItemSelected(int android.view.MenuItem)
		if(source != null){
			String tmp = null;
			for(int i=0;i<feedback.size();i++){
				try{
					tmp = feedback.get(i);
					String[] parts = tmp.split("METHOD_STARTING,<");
//					if(parts.length <=1){
//						parts = tmp.split("METHOD_RETURNING,<");
//					}

					String method = parts[1].split(">")[0];
//					System.out.println(method);
					source.addMatchedEventWithHandler(method, event);
				}catch(Exception e){
//					System.out.println("cannot process: "+tmp);
				}
			}
			System.out.println("Total methods triggered:"+feedback.size());
		}else{
			System.out.println("No method association due to missing source vertex");
		}
	}

	public List<List<Event>> findPotentialPathForHandler(String method){
		//Set<Entry<String, Collection<RunTimeLayout>>>
		List<List<Event>> reuslt = new ArrayList<List<Event>>();
		
		for(Entry<String, Collection<RunTimeLayout>> entry:actCategoryReference.entrySet() ){
			Collection<RunTimeLayout> runTimeCollection = entry.getValue();
			Iterator<RunTimeLayout>  iter = runTimeCollection.iterator();
			while(iter.hasNext()){
				RunTimeLayout layuout = iter.next();
				ArrayList<Event> viewTriggerHandler = layuout.getPotentialEventForHandler(method);
				if(viewTriggerHandler!=null && viewTriggerHandler.size() > 0){
					List<Event>  sequence = this.getEventSequence(this.launcher, layuout);
					for(Event trigger : viewTriggerHandler){
						List<Event> sequence_copy = new ArrayList<Event>(sequence);
						sequence_copy.add(trigger);
						reuslt.add(sequence_copy);
					}
					
				}
			}
		}
		
		for(Entry<String, Collection<RunTimeLayout>> entry:this.outOfScopeReference.entrySet() ){
			Collection<RunTimeLayout> runTimeCollection = entry.getValue();
			Iterator<RunTimeLayout>  iter = runTimeCollection.iterator();
			while(iter.hasNext()){
				RunTimeLayout layuout = iter.next();
				ArrayList<Event> viewTriggerHandler = layuout.getPotentialEventForHandler(method);
				if(viewTriggerHandler!=null && viewTriggerHandler.size() > 0){
					List<Event>  sequence = this.getEventSequence(this.launcher, layuout);
					for(Event trigger : viewTriggerHandler){
						List<Event> sequence_copy = new ArrayList<Event>(sequence);
						sequence_copy.add(trigger);
						reuslt.add(sequence_copy);
					}
					
				}
			}
		}
		
		{
			ArrayList<Event> viewTriggerHandler = launcher.getPotentialEventForHandler(method);
			if(viewTriggerHandler!=null && viewTriggerHandler.size() > 0){
				List<Event>  sequence = new ArrayList<Event>();
				for(Event trigger : viewTriggerHandler){
					List<Event> sequence_copy = new ArrayList<Event>(sequence);
					sequence_copy.add(trigger);
					reuslt.add(sequence_copy);
				}
			}
		}
		return reuslt;
	}
	
	public void printAllMethod(){
		System.out.println("-----------------------");
		for(Entry<String, Collection<RunTimeLayout>> entry:actCategoryReference.entrySet() ){
			Collection<RunTimeLayout> runTimeCollection = entry.getValue();
			Iterator<RunTimeLayout>  iter = runTimeCollection.iterator();
			while(iter.hasNext()){
				RunTimeLayout layout = iter.next();
				System.out.println("-----------------------");
				List<Pair<String, Event>> list = layout.getEventHandlerList();
				for(Pair<String, Event> pair : list){
					System.out.println(pair.first+"\t;\t"+pair.second);
				}
			}
		}
		
		for(Entry<String, Collection<RunTimeLayout>> entry:this.outOfScopeReference.entrySet() ){
			Collection<RunTimeLayout> runTimeCollection = entry.getValue();
			Iterator<RunTimeLayout>  iter = runTimeCollection.iterator();
			while(iter.hasNext()){
				RunTimeLayout layout = iter.next();
				System.out.println("-----------------------");
				List<Pair<String, Event>> list = layout.getEventHandlerList();
				for(Pair<String, Event> pair : list){
					System.out.println(pair.first+"\t;\t"+pair.second);
				}
			}
		}
		
		System.out.println("-----------------------");
		List<Pair<String, Event>> list = launcher.getEventHandlerList();
		for(Pair<String, Event> pair : list){
			System.out.println(pair.first+"\t;\t"+pair.second);
		}
		System.out.println("-----------------------");
	}

	public RunTimeLayout getLauncher(){
		return this.launcher;
	}
}
