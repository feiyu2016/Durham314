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

import zhen.framework.Configuration;
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
	private Map<String,List<Event>> methodEventMap = new HashMap<String,List<Event>>();
	
	public RunTimeInformation(Framework frame){
		this.frame = frame;
		deviceLayout = new RunTimeLayoutInformation();
		UIModel = new UIModelGraph();
	}
	
	public void init(Map<String, Object> attributes){
		this.packageName = (String) attributes.get(Common.packageName);
		deviceLayout.init();
		UIModel.init();
	}
	
	public void enableGUI(){
		this.UIModel.enableGUI();
	}
	
	/**
	 * Synchronize with the device
	 * and Update necessary information 
	 * @param lastEvent -- the last event being applied on the device
	 */
	public void update(Event lastEvent){
		if(DEBUG) Utility.log(TAG, "update");
		
		//check if the event needs to be ignored 
		if(lastEvent.isIgnored || lastEvent.getEventType() == Event.iEMPTY){ return; }
		
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
			this.UIModel.addTransition(lastEvent, newState);
		}break;
		}
		if(DEBUG) Utility.log(TAG, "update check point 4");
		lastEvent.addMethodHiets(logcatFeedback);
		for(String hit : logcatFeedback){
			if(methodEventMap.containsKey(hit)){
				List<Event> eventList = methodEventMap.get(hit);
				if(!eventList.contains(lastEvent)) eventList.add(lastEvent);
			}else{
				List<Event> eventList = new ArrayList<Event>();
				eventList.add(lastEvent);
				methodEventMap.put(hit, eventList);
			}
		}
		if(DEBUG) Utility.log(TAG, "update check point 5");
		//check other system info
		//TODO
	}
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
	
	public UIState getCurrentState(){
		return this.UIModel.getPreviousState();
	}

	public List<Event> getEventSequence(UIState source, UIState target){
		List<Event> result = DijkstraShortestPath.findPathBetween(this.UIModel.getGraph(), source, target);
		
		
		return result;
	}
	public void terminate(){
		deviceLayout.terminate();
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
	public boolean needRetrieveLayout(Window target, WindowInformation winInfo){
//		String[] parts = getAppAndActName(target.getTitle());
//		String appName = parts[0];
//		String actName = parts[1];
//		if(DEBUG) Utility.log(TAG, "needRetrieveLayout,"+appName+","+actName);
//		boolean result;
//		if(appName.contains("launcher")){
//			result = false;
//		}if(appName.equals("")){	//empty app name, check the winInfo
//			//only check one level, should be enough at thie point 
//			//TODO 
//			result = winInfo.pkgName.equals(this.packageName);
//		}else result = appName.equals(this.packageName);
		
		boolean result = winInfo!=null;
		if(DEBUG) Utility.log(TAG, "needRetrieveLayout,"+result);
		return result;
	}
	
	public static String[] getAppAndActName(String msg){ 
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

	public Map<String, List<Event>> getMethodEventMap() {
		return methodEventMap;
	}
}
