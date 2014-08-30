package zhen.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.hierarchyviewer.scene.ViewNode;

import viewer.ViewPositionData;
import android.view.KeyEvent;
import inputGeneration.EventHandlers;
import inputGeneration.Layout;
import inputGeneration.StaticInfo;

/**
 * Date: August 30th 2014
 * Implementation of traverse in the UIs
 * 
 * Static info:
 * 		stayingWidgets 	-- 	Views that for sure that does not cause UI change
 * 		eventHandler 	-- 	What event a known view could receive
 * 
 * Dynamic info:
 * 		Break points 	-- 	which is setup before testing and provide information on method calls
 * 		HierarchyView 	-- 	provide view trees
 * 
 * RunTime Log:
 * 		Log channel		-- 	Log various information to file
 * 		Event Sequence	-- 	the sequence of events that leads to current UI state
 * 	
 * Known problems and potential solutions for algorithm
 * 		1. Cyclic UI graph could leads to infinite loop, duplicated UI analysis. 
 * 		UI learning which takes the sequence of UIs before and after current UI
 * 		may solve the problem.
 * 
 * Algorithm:
 * For each untested activity, 
 * 		travel through all staying widgets
 * 		for each of untested views
 * 			for each of possible events
 * 				carry out the event
 * 				observe consequence
 * 				record the event
 * 				if UI changes 
 * 					travel the new UI
 * 				sustain layout consistency
 * 
 * Fatal error condition
 * 		When UI cannot be re-reached
 * 
 * @author zhenxu
 *
 */
public class StaticGuidedAlgoirthm extends TraverseAlgorithm{	
	public boolean enableUsePartialSet = true;
	public boolean enableUseFullSet = true;
	public boolean enableReinstall = true;
	public boolean enableStaticHandlerInfo = false;
	public boolean enablePathOptimization = false;
	public static boolean Debug = false;
	public static final String[] layoutKeywords = { // name and address are not included here
		//identification
		"mID",
		"getTag()",
		"getVisibility()",
		
		//drawing information
		"drawing:getX()",
		"drawing:getY()",
		"drawing:mLayerType",
		
		//Padding information
		"padding:mPaddingTop",
		"padding:mPaddingLeft",
		"padding:mPaddingRight",
		"padding:mPaddingBottom",
		
		//layout information
		"layout:mChildCountWithTransientState",
		"layout:getWidth()",
		"layout:getHeight()",
		
		//status information
		"isActivated()",
		"isSelected()",
		"isEnabled() ",
		
		//focus information 
		"focus:hasFocus()",
		
		//Customized information
		"x",
		"y"
	};
	
	private static String PREFIX = "ZLABEL";
	private static String TESTED = PREFIX+"tested";
	private String currentStartingActName;
	private String[] allPossibleEvents = EventHandlers.eventHandlers;
	private long waitDuraion = 100;
	private boolean enableDynamicPrograming = false;
	private boolean innerTravel_failure = false;
	private Map<String, List<String>> stayingWidgtsInfoDepost;
	private Map<String, ArrayList<EventRecord>> eventRecorder;
	private ArrayList<EventRecord> currentPath;
	
	
	/**
	 * @param apkPath	-- 	the path to the target APK file which will be installed on device
	 */
	public StaticGuidedAlgoirthm(String apkPath) {
		super(apkPath);
		eventRecorder = new HashMap<String, ArrayList<EventRecord>>();
		stayingWidgtsInfoDepost = new HashMap<String, List<String>>();
		this.dataFilter = new ViewPositionData.NodeDataFilter(){
			@Override
			public ArrayList<String> process(ArrayList<ViewNode> list) {
				ArrayList<String> result = new ArrayList<String>();
				for(ViewNode node: list){
					StringBuilder sb = new StringBuilder();
					sb.append(node.name+";");
					for(String keyword: layoutKeywords){
						sb.append(node.namedProperties.get(keyword).value+";");
					}
					double x = Double.parseDouble(node.namedProperties.get("drawing:getX()").value) ,
							   y = Double.parseDouble(node.namedProperties.get("drawing:getY()").value);
						
						ViewNode current = node.parent;
						while(current != null){
							x+=Double.parseDouble(current.namedProperties.get("drawing:getX()").value);
							y+=Double.parseDouble(current.namedProperties.get("drawing:getY()").value);
							current = current.parent;
						}
					sb.append(x +";"+y+";");
					result.add(sb.toString());
				}
				return result;
			}
		};
	}

	/**
	 * when all conditions are set, execute the operation!
	 */
	@Override
	public void execute() {
		//pick an activity that has not been tested
		while(true){
			int actIndex = Utility.findFirstFalse(actMark);
			if(actIndex<0) break; // all activities are label as tested -- finished.
			//launch the act --> am start -n yourpackagename/.activityname
			currentStartingActName = this.activityNames.get(actIndex);
			ADBControl.sendSellCommand("am start -n "+this.packageName+"/."+currentStartingActName);
			//TODO -- change waitForTime to wait for UIStalized after sufficient test
			waitForTime(5000);
			
			String currentActName = currentStartingActName;
			labelActivityAsReached(currentActName);
			//the event of launching APP is not recorded
			
			currentPath = new ArrayList<EventRecord>();
			innerTravel(null, currentActName);
			eventRecorder.put(currentStartingActName, currentPath);
		}
	}
	
	/**
	 * Travel through the UIs. Recursive call happens when UI change is detected.
	 * @param currentLayoutInfo -- 	the layout information of a UI. Can be null. 
	 * @param currentActName	--	the name of activity currently activates.
	 */
	private void innerTravel(List<Map<String,String>> currentLayoutInfo, String currentActName){
		while(innerTravel_failure != false){
			//retrieve current layout information
			if(currentLayoutInfo == null){
				List<String> rawLayoutInfo = this.viewData.retrieveFocusedActivityInformation();
				currentLayoutInfo = processLayoutData(rawLayoutInfo);
			}
			//TODO -- Optimization: Could sort the array by mID, this benefits for searching later
			
			//match with existing static information
			Layout matchedLayoutInfo = matchWithStaticLayout(currentLayoutInfo);
			if(matchedLayoutInfo != null){ // there is a match!
				// For the widget which will not trigger any UI change 
				String layoutName = matchedLayoutInfo.getName();
				List<String> stayingWidgetsInfo = null;
				if(enableDynamicPrograming){
					if(stayingWidgtsInfoDepost.containsKey(layoutName)){
						stayingWidgetsInfo = stayingWidgtsInfoDepost.get(currentActName+ layoutName);
					}else{
						stayingWidgetsInfo = StaticInfo.getStayingWidgets(apkFile, currentActName, layoutName);
						stayingWidgtsInfoDepost.put(currentActName+ layoutName, stayingWidgetsInfo);
					}
				}else{
					stayingWidgetsInfo = StaticInfo.getStayingWidgets(apkFile, currentActName, layoutName);
				}
				
				for(String viewId: stayingWidgetsInfo){ //stayingWidgtsInfo stores the id for views
					//find the view in currentLayoutInfo
					InnerLoop: for(int i=0;i<currentLayoutInfo.size();i++){
						Map<String,String> potentialView_attribute = currentLayoutInfo.get(i);
						String retrievedId = potentialView_attribute.get("mID");
						if(retrievedId.equals(viewId)){
							generateEventSetForStayingWidgets(currentActName,potentialView_attribute, matchedLayoutInfo);
							potentialView_attribute.put(TESTED, "true");//mark this view as tested
							break InnerLoop;
						}
					}
				}
			}
			// the UI should not get changed before the above operations
			
			// For the rest of widget -- it does not seem to be necessary to pay 
			// special attention on the leaving widgets 
			for(Map<String,String> viewProfile: currentLayoutInfo){
				if(viewProfile.get(TESTED) == null){ // the view is not yet touched
					String eventType = null;
					while(((eventType = getNextEventForView(viewProfile, matchedLayoutInfo)) != null)){
						
						boolean layoutUnchanged = false;
						EventRecord record = new EventRecord(eventType, currentActName);
						record.recordFromViewProfile(viewProfile);
						carryoutEventOnView(viewProfile, eventType, true);//e.g press, click
						waitForTime(800);
						
						//check consequence
						String focuedActName = this.viewData.getFocusedActivityName();
						if(focuedActName.equals(currentActName)){	//in the same activity ?
							List<String> info = this.viewData.retrieveFocusedActivityInformation();
							List<Map<String,String>> processedLayoutInfo = this.processLayoutData(info);
							//compare the information with known layout to determine any change
							if(isExactlyMatched(processedLayoutInfo,currentLayoutInfo)){
								layoutUnchanged = true; // the same layout -- no change at all --> proceed
							}else{ //layout has changed 
								innerTravel(currentLayoutInfo, currentActName);
							}
						}else if(this.activityNames.contains(focuedActName)){//has jumped to other activity in the APP?	
							labelActivityAsReached(focuedActName);
							innerTravel(null, focuedActName);
						}else{ // launch an activity NOT in the APP e.g. Setting or went back to launcher
							// don't traverse into the other APP
						}
						
						record.destActName = focuedActName;
						record.triggerLayoutChange = layoutUnchanged;
						this.currentPath.add(record);
						if(layoutUnchanged){ continue; }
						
						//make sure the consistency of the UI graph
						//TODO -- refine/re-think this part when a failure occurs
						if(innerTravel_failure) return;
						sustainUIGraphConsisitencyProcedure(currentLayoutInfo);
					}
					viewProfile.put(TESTED, "true");
				}
			}
			
		}
	}

	/**
	 * sustain the UI consistency. Make sure the UI is the same one after an event is launched on 
	 * device. It will be called after an event. 
	 * @param expectedLayout	--	the expected layout 
	 * @return	true if current UI is the same as expected one, false otherwise
	 */
	private boolean sustainUIGraphConsisitencyProcedure(List<Map<String,String>> expectedLayout){
		{
			//press button to see if the original layout could be reached
			this.monkey.interactiveModelPress(KeyEvent.KEYCODE_BACK);
			//TODO -- change waitForTime to waitForUIStablized after sufficient test
			waitForTime(800);
			List<String> info_previous_layoutChange = this.viewData.retrieveFocusedActivityInformation();
			List<Map<String,String>> processedInfo = this.processLayoutData(info_previous_layoutChange);
			if(isExactlyMatched(expectedLayout,processedInfo)){
				//via press back, the UI has been reached. 
				return true;
			}
		}
		
		if(enableUsePartialSet){
			restartAndReachUIWithPartialSet();
			List<String> restartedUIlayout = this.viewData.retrieveFocusedActivityInformation();
			List<Map<String,String>> processedInfo = this.processLayoutData(restartedUIlayout);
			if(isExactlyMatched(expectedLayout,processedInfo)){
				//via the partial set, the UI has been reached. 
				return true;
			}
		}
		
		if(enableUseFullSet){
			restatAndReachUIWithFullSet();
			List<String> restartedUIlayout = this.viewData.retrieveFocusedActivityInformation();
			List<Map<String,String>> processedInfo = this.processLayoutData(restartedUIlayout);
			if(isExactlyMatched(expectedLayout,processedInfo)){
				//via the full set, the UI has been reached. 
				return true;
			}
		}
		
		if(enableReinstall){
			reinstallPackageAndReachUIWithFullSet();
			List<String> restartedUIlayout = this.viewData.retrieveFocusedActivityInformation();
			List<Map<String,String>> processedInfo = this.processLayoutData(restartedUIlayout);
			if(isExactlyMatched(expectedLayout,processedInfo)){
				//via re-installation, the UI has been reached. 
				return true;
			}
		}
		
		failToReachUI();
		return false ;
	}
	
	/**
	 * when UI consistency can not be sustained. Consider this a fatal error.
	 */
	private void failToReachUI(){
		System.out.println("Fail to reach UI");
		dumpLogFile();
		innerTravel_failure = true;
	}
	
	/**
	 * Restart the APP and carry out the recorded event sequence. However, events which do not 
	 * trigger UI change are ignored. Will try to find the shortest path if flag is set.
	 */
	private void restartAndReachUIWithPartialSet(){
		ADBControl.sendSellCommand("am force-stop "+this.packageName);
		this.waitForTime(800);
		ADBControl.sendSellCommand("am start -n "+this.packageName+"/."+currentStartingActName);
		this.waitForTime(800);
		List<EventRecord> path = this.currentPath;
		if(enablePathOptimization){
			path = optimizeEventPath(this.currentPath);
		}
		applyEventSet(path,true);
	}
	
	/**
	 * Restart the APP and carry out the full event sequence
	 */
	private void restatAndReachUIWithFullSet(){
		ADBControl.sendSellCommand("am force-stop "+this.packageName);
		this.waitForTime(800);
		ADBControl.sendSellCommand("am start -n "+this.packageName+"/."+currentStartingActName);
		this.waitForTime(800);
		applyEventSet(this.currentPath, false);
	}
	
	
	/**
	 * TODO -- may want to check ADB output
	 * Wipe out the APP and all data in the its private storage place e.g. database. 
	 * Re-install the APP and launch it. Then carry out the event sequence.
	 */
	private void reinstallPackageAndReachUIWithFullSet(){
		ADBControl.sendSellCommand("pm uninstall -k "+this.packageName);
		this.waitForTime(1500);
		ADBControl.sendADBCommand("adb install "+this.apkPath);
		this.waitForTime(1500);
		ADBControl.sendSellCommand("am start -n "+this.packageName+"/."+currentStartingActName);
		this.waitForTime(1500);
		applyEventSet(this.currentPath, false);
	}
	
	/**
	 * TODO implementation required
	 * @param path -- the event path happened
	 * @return	the optimized/shortest valid event sequence
	 */
	private List<EventRecord> optimizeEventPath(ArrayList<EventRecord> path){
		List<EventRecord> result = new ArrayList<EventRecord>();
		return result;
	}
	
	/**
	 * Used for traveling back to UI after restart
	 * @param path 	-- the sequence about who the program get to the status
	 * @param ignoreIntermediate -- if ignore the widget does not change UI
	 */
	private void applyEventSet(List<EventRecord> path, boolean ignoreIntermediate){
		for(EventRecord event : path){
			if(event.triggerLayoutChange || !ignoreIntermediate){
				this.carryoutEventOnView(event.viewProfile, event.eventType, false);
				this.waitForTime(800);
			}
		}
	}
	
	private void waitForTime(long timeout){
		try { Thread.sleep(timeout);
		} catch (InterruptedException e) { }
	}
	private void waitForUIStablized(long timeout){
		long time1 = System.currentTimeMillis();
		long time2 = -1;
		
		do{
			try { Thread.sleep(waitDuraion);
			} catch (InterruptedException e) { }
			time2 = System.currentTimeMillis();
		}while(!isUIStablized() && time2-time1 > timeout);
	}
	private void waitForUIStablized(){
		while(isUIStablized()){
			try { Thread.sleep(waitDuraion);
			} catch (InterruptedException e) { }
		}
	}
	/**
	 * TODO -- implementation required after acquisition of sufficient knowledge on breakpoint 
	 * hits on various situation. Possible other candidates includes device CPU rate, specific
	 * method which might indicate the finish of UI drawing. 
	 * @return true if 
	 */
	private boolean isUIStablized(){
		return false;
	};
	/**
	 * Label an activity has been reached so that it will not be launched separately.
	 * @param actName -- the name of the activity
	 */
	private void labelActivityAsReached(String actName){
		for(int i=0;i<this.activityNames.size();i++){
			if(this.activityNames.get(i).equals(actName)){
				this.actMark[i] = true;
				break;
			}
		}
	}
	
	/**
	 * NOTE Right now it assume the order of the views will not change
	 * Compare the two input layouts in terms of name, mID, x, y, layout:getWidth(), layout:getHeight()
	 * @param layout1	-- the first layout
	 * @param layout2	-- the second layout
	 * @return true if the order and the target attributes of each elements are the same
	 */
	private boolean isExactlyMatched(List<Map<String,String>> layout1, List<Map<String,String>> layout2){
		if(layout1.size() != layout2.size()) return false;
		for(int i=0;i<layout1.size();i++){
			String msg1 = exactComparisonHelper(layout1.get(i));
			String msg2 = exactComparisonHelper(layout2.get(i));
			if(!msg1.equals(msg2)) return false;
		}
		return true;
	}
	//NOTE potential wrong format e.g. "1.0" does not equal String "1"
	private static String exactComparisonHelper(Map<String,String> info){
		return info.get("name")+info.get("mID")+info.get("x")+info.get("y")+info.get("layout:getWidth()")+info.get("layout:getHeight()");
	}
	
	/**
	 * NOTE Assume: no ID duplication. if the layout set from dynamic run >= the layout set 
	 * 		from static analysis then there is a match. Potential optimization could be done.
	 * @param currentLayoutInfo -- current layout information from device
	 * @return match static layout
	 */
	private Layout matchWithStaticLayout(List<Map<String,String>> currentLayoutInfo){
		Layout result = null;
		for(Layout layout : this.staticLayout){
			ArrayList<inputGeneration.ViewNode> list = layout.getViewNodes();
			int totalElement = list.size();
			
			for(inputGeneration.ViewNode viewNode: list){
				String viewId = viewNode.getID();
				boolean aMatch = false;
				for(Map<String,String> profile: currentLayoutInfo){
					String potentialId = profile.get("mID");
					if(potentialId.equals(viewId)){
						aMatch = true;
						totalElement -= 1;
						break;
					}
				}
				if(aMatch == false) break;
			}
			
			if(totalElement == 0){
				result = layout;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Extract the information from a set of Strings into 
	 * a format where data are more accessible
	 * @param list -- a list of layout information from PositionData
	 * @return the formated information
	 */
	private List<Map<String,String>> processLayoutData(List<String> list){
		List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		for(String infoPiece: list){
			Map<String,String> extracted = extractLayoutInfo(infoPiece);
			result.add(extracted);
		}
		return result;
	}
	
	/**
	 * extract layout information from a single string into 
	 * a more accessible format
	 * @param layoutInfoPiece
	 * @return a map between keyword and attribute
	 */
	private Map<String,String> extractLayoutInfo(String layoutInfoPiece){
		String[] parts = layoutInfoPiece.split(";");
		Map<String,String> result = new HashMap<String,String>();
		String name = parts[0].split("@")[0];
		String address = parts[0].split("@")[1];
		
		result.put("name", name);
		int index = 1;
		for(String key: layoutKeywords){
			result.put(key, parts[index]);
			index+=1;
		}
		result.put("x", parts[index]);index+=1;
		result.put("y", parts[index]);index+=1;
		result.put("address", address);
		return result;
	}
	
	/**
	 * NOTE Possible unnecessary searching
	 * Find a possible event given static analysis information and the view profile.
	 * @param viewInfo	-- the view profile including id 
	 * @param staticLayoutInfo	--	static layout information 
	 * @return an event string or null
	 */
	private String getNextEventForView(Map<String,String> viewInfo, Layout staticLayoutInfo){
		if(enableStaticHandlerInfo){
			//only consider event handler listed in staticLayoutInfo
			String viewId = viewInfo.get("mID");
			ArrayList<inputGeneration.ViewNode> staticList = staticLayoutInfo.getViewNodes();
			for(inputGeneration.ViewNode viewNode :staticList ){
				if(viewId.equals(viewNode.getID())){
					//found
					for(String event: this.allPossibleEvents){
						boolean condition1 = viewNode.hasEventHandler(event);
						boolean condition2 = !viewInfo.containsKey(PREFIX+ event);
						if(condition1 && condition2){ // has handler but not tested
							return event;
						}
					}
					
				}
			}
			//no found in static layout, thereby test on All possible event 
			for(String event: this.allPossibleEvents){
				boolean condition2 = !viewInfo.containsKey(PREFIX+ event);
				if(condition2){ // has handler but not tested
					return event;
				}
			}
		}else{
			for(String event: this.allPossibleEvents){
				boolean condition2 = !viewInfo.containsKey(PREFIX+ event);
				if(condition2){ // has handler but not tested
					return event;
				}
			}
		}
		return null;
	}
	
	/**
	 * carry out the event on device
	 * TODO Require improvement over time
	 * @param viewInfo	--	view profile
	 * @param event		-- the event which needs to be carried out
	 * @param enableLabel	-- if needs to label the event as tested. 
	 */
	private void carryoutEventOnView(Map<String,String> viewInfo, String event, boolean enableLabel){
		String x = viewInfo.get("x");
		String y = viewInfo.get("y");
		if(event.equals("android:onClick")){
			this.monkey.inteactiveModelClick(x, y);
		}
		if(enableLabel) viewInfo.put(PREFIX+event,"tested"); // label this event as tested;
	}
	
	/**
	 * This function is used when a set of views are known for sure will not change layout
	 * @param actName -- the current activity
	 * @param viewInfo -- a map between key and attributes for a certain view for the staying widgets
	 * @param staticLayoutInfo
	 */
	private void generateEventSetForStayingWidgets(String actName, Map<String,String> viewInfo, Layout staticLayoutInfo){
		String viewId = viewInfo.get("mID");
		ArrayList<inputGeneration.ViewNode> staticList = staticLayoutInfo.getViewNodes();
		for(inputGeneration.ViewNode viewNode :staticList ){
			ArrayList<String> eventSet = new ArrayList<String>();
			if(viewId.equals(viewNode.getID())){//found
				for(String eventAction: this.allPossibleEvents){
					boolean condition1 = viewNode.hasEventHandler(eventAction);
					if(condition1){
						eventSet.add(eventAction);
					}
				}
				for(String eventType : eventSet){
					EventRecord record = new EventRecord(eventType, actName);
					record.recordFromViewProfile(viewInfo);
					carryoutEventOnView(viewInfo, eventType, true);
					this.waitForTime(300);
					record.destActName = actName;
					currentPath.add(record);
				}
			}else{//This should be logically impossible to reach due to previous if condition
				for(String eventType : this.allPossibleEvents){
					EventRecord record = new EventRecord(eventType, actName);
					record.recordFromViewProfile(viewInfo);
					carryoutEventOnView(viewInfo, eventType, true);
					this.waitForTime(300);
					record.destActName = actName;
					currentPath.add(record);
				}
			}
		}
	}
	
	private void dumpLogFile(){
		//TODO
	}
	
	/**
	 * Representation of an event e.g. touch, click
	 * On a view which is in an activity, an event occurred, which leads to a destination UI.
	 * @author zhenxu
	 */
	private class EventRecord{
		public EventRecord(String eventType, String sourceAct){
			this.eventType = eventType;
			this.sourceActName = sourceAct;
		}
		public String sourceActName, destActName;
		public Map<String,String> viewProfile;
		public String eventType;
		public boolean triggerLayoutChange;
		public String targetUIInformation;
		public Map<String,String> additionalInfo = new HashMap<String,String>();
	
		public void recordFromViewProfile(Map<String,String> profile){
			viewProfile = profile;
		}
		
		public String toString(){
			return eventType+" occurs on ("+viewProfile.get("x")+","+viewProfile.get("y")+") at " +sourceActName+" leads to "+destActName;
		}
	}
}
