package zhen.version1.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Paths;

import org.jgrapht.alg.DijkstraShortestPath;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;
 
import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.RunTimeLayoutInformation;
import zhen.version1.component.UIModelGraph;
import zhen.version1.component.UIState;
import zhen.version1.component.WindowInformation;
 
/**
 * Responsibility: Keep track of runtime information: Logcat,layout,JDB,System
 * 
 * @author zhenxu
 *
 */
public class RunTimeInformation{
	public static boolean DEBUG = true;
	public static final String TAG = "RunTimeInformation";
	private static String winIgnoredList = "Toast";
	protected Framework frame;
	/**
	 * the name of the application 
	 */
	private String packageName;
	/**
	 * the ui information for the device
	 */
	private RunTimeLayoutInformation deviceLayout;
	/**
	 * A graph based UIModel
	 */
	private UIModelGraph UIModel;
	/**
	 * Map between method name and event
	 */
	private final Map<String,List<Event>> methodEventMap = new HashMap<String,List<Event>>();
	
	private List<Event> eventDeposit = new ArrayList<Event>();
	
	/**
	 * 
	 * @param frame
	 */
	public RunTimeInformation(Framework frame){
		this.frame = frame;
		deviceLayout = new RunTimeLayoutInformation();
		UIModel = new UIModelGraph();
	}
	/**
	 * initialize components
	 * @param attributes
	 */
	public void init(Map<String, Object> attributes){
		this.packageName = (String) attributes.get(Common.packageName);
		deviceLayout.init();
		UIModel.init();
	}
	/**
	 * close/release/terminate related component
	 */
	public void terminate(){
		deviceLayout.terminate();
	}
	/**
	 * Enable the GUI for the graph 
	 */
	public void enableGUI(){
		this.UIModel.enableGUI();
	}
	
	/**
	 * get the UIModel being used
	 * @return UIModel
	 */
	public UIModelGraph getUIModel() {
		return UIModel;
	}
	/**
	 * Synchronize with the device
	 * and Update necessary information 
	 * including feedback from logcat, layout.
	 * @param lastEvent -- the last event being applied on the device
	 */
	public void update(Event lastEvent){
		if(DEBUG) Utility.log(TAG, "update");
		
		//check if the event needs to be ignored 
		if(lastEvent.getEventType() == Event.iEMPTY){ return; }
		
		List<String> logcatFeedback = Utility.readInstrumentationFeedBack();
		if(DEBUG) Utility.log(TAG, "readInstrumentationFeedBack finished");
//		if(isErrorPresent(logcatFeedback)){	//TODO
//			onApplicationError();
//		}
		
		WindowInformation[] visibleWindows = checkVisibleWindowAndCloseKeyBoard();
		
		WindowInformation targetInfo = null;
		for(WindowInformation info : visibleWindows){
			if(info.pkgName.equals(this.packageName) && !winIgnoredList.contains(info.name)){
				targetInfo = info;
				break;
			}
		}
		if(DEBUG) Utility.log(TAG, "WindowInformation, "+targetInfo);
		Window[] winList = deviceLayout.getWindowList();
		Window topWin = null;
		if(targetInfo == null){	//which means no app window visible
			topWin = deviceLayout.getFocusedWindow();
			
		}else{
			//find the first one that has the same name
			for(int index = 0; index < winList.length; index ++){
				Window win = winList[index]; 
				if(win.encode().equals(targetInfo.encode)){
					topWin = win;
					break;
				}
			}
		}
		if(DEBUG) Utility.log(TAG, "topWin, "+topWin);
		//Maybe want to check which is first drawn, focused or top
		
		UIState previous = this.UIModel.getCurrentState();
		String targetTitle = topWin.getTitle();
		String parts[] = getAppAndActName(targetTitle);
		String appName = parts[0];
		String actName = parts[1];
		
		//get necessary layout 
		int eventType = lastEvent.getEventType();
		switch(eventType){
		case Event.iLAUNCH:
		case Event.iRESTART:
		case Event.iREINSTALL:{
			UIState newState = null;
			if(needRetrieveLayout(topWin,targetInfo)){
				ViewNode root = deviceLayout.loadWindowData(topWin);
				newState = UIModel.getOrBuildState(appName, actName, root,targetInfo);
			}else{ 
				newState = UIModel.getOrBuildState(appName, actName, null,targetInfo); 
				newState.isInScopeUI = false;
			}
			if(lastEvent.operationCount > 0){
				//which means in which target and source should not be null
				//and check if they meet the expectation. only the target matters because it
				//is a launch event
				if(!previous.equals(lastEvent.getTarget())){
					//does not meet the expectation
					lastEvent = new Event(lastEvent);
				}
			}
			this.UIModel.addLaunch(lastEvent, newState);
		}break;
		case Event.iONCLICK:
		case Event.iPRESS:{
			UIState newState = null;
			if(needRetrieveLayout(topWin,targetInfo)){
				ViewNode root = deviceLayout.loadWindowData(topWin);
				newState = UIModel.getOrBuildState(appName, actName, root,targetInfo);
			}else{ 
				newState = UIModel.getOrBuildState(appName, actName, null,targetInfo); 
				newState.isInScopeUI = false;
			}
			if(lastEvent.operationCount > 0){
				//which means in which target and source should not be null
				//and check if they meet the expectation. 
				if(!previous.equals(lastEvent.getSource()) || !previous.equals(lastEvent.getTarget())){
					//does not meet the expectation
					lastEvent = new Event(lastEvent);
				}
			}
			this.UIModel.addTransition(lastEvent, newState);
		}break;
		}
		lastEvent.addMethodHiets(logcatFeedback);
		for(String hit : logcatFeedback){
			if(methodEventMap.containsKey(hit)){
				List<Event> eventList = methodEventMap.get(hit);
				eventList.add(lastEvent);
			}else{
				List<Event> eventList = new ArrayList<Event>();
				eventList.add(lastEvent);
				methodEventMap.put(hit, eventList);
			}
		}
		
		lastEvent.operationCount += 1;
		eventDeposit.add(lastEvent);
		
		
		//check other system info
		//TODO
	}
	/**
	 * get a list of visible windows and if the keyboard is present close it
	 * @return	a list of visible window information
	 */
	public WindowInformation[] checkVisibleWindowAndCloseKeyBoard(){
		WindowInformation[]  visibleWindows = WindowInformation.getVisibleWindowInformation();
		for(WindowInformation vwin : visibleWindows){
			//TODO to improve
			if(vwin.name.toLowerCase().contains("inputmethod")){
				this.frame.executer.onBack();
				break;
			}
		}
		return visibleWindows;
	}
	/**
	 * get the current UI which the program believe the device is on.
	 * @return
	 */
	public UIState getCurrentState(){
		return this.UIModel.getCurrentState();
	}
	/**
	 * get an event sequence from source UI to target UI
	 * Both of the UIState must be known in the graph
	 * @param source	
	 * @param target
	 * @return	a list of event 
	 */
	public List<Event> getEventSequence(UIState source, UIState target){
		List<Event> path = DijkstraShortestPath.findPathBetween(this.UIModel.getGraph(), source, target);
		return path;
	}
	/**
	 * get the map which tells a method can be triggered by which events. 
	 * @return
	 */
	public Map<String, List<Event>> getMethodEventMap() {
		return methodEventMap;
	}
	/**
	 * get the sequence of events that applied on the device by far
	 * Note: OnBack due to keyboard is not included
	 * @return
	 */
	public List<Event> getAppliedEventSequence(){
		return this.eventDeposit;
	}
	
	private boolean isErrorPresent(List<String> logcatFeedback){
		//TODO
		return false;
	}
	private void onApplicationError(){
		//TODO
	}
	/**
	 * 
	 * @param target
	 * @param winInfo	- a newly retrieved information for a window in the app
	 * @return
	 */
	private boolean needRetrieveLayout(Window target, WindowInformation winInfo){
		boolean result = winInfo!=null;
		if(DEBUG) Utility.log(TAG, "needRetrieveLayout,"+result);
		return result;
	}
	
	private static String[] getAppAndActName(String msg){ 
		String parts[] = msg.split("/");
		String appName = "";
		String actName = "";
		if(parts.length > 1){
			appName = parts[0];
			actName = parts[1];
		}else{
			actName = parts[0];
		}
		return new String[]{appName,actName};
	}
}
