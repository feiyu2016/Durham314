package inputGeneration;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import main.Paths;
import viewer.MonkeyWrapper;
import viewer.ViewPositionData;


	public class SimpleTesting {
		
		public static ArrayList<String> currentViewData;
		public static MonkeyWrapper m;
		public static ViewPositionData view;
		public static File processedAPK;
		public static ArrayList<String> visitedActivities;
		private static JDBStuff jdb;

		public static void clickAll(File file) throws Exception{

			RunTimeInfo.startApp(file);	// adb am start -n package/activity
			
			setBreakPointsAtAllLines(file);
			System.out.println("break points have been set.");
			initRuntimeEnv();		// initiate HierarchyViewer and MonkeyRunner
			System.out.println("INITIATION COMPLETE");

			updateViewData();
			sendEvent(file, "activity_main", "ford", "android:onClick");
			updateViewData();
			sendEvent(file, "activity_main", "gtr", "android:onClick");
			updateViewData();
			sendEvent(file, "activity_main", "por", "android:onClick");
			updateViewData();
			sendEvent(file, "activity_main", "bug", "android:onClick");
			
/*			String mainActivityName = StaticInfo.getMainActivityName(file);
			clearClickingRecord(file);
			clickWidgetsOfActivity(file, mainActivityName, mainActivityName, "activity");
			ArrayList<String> allActivities = StaticInfo.getActivityNames(file);
			for (String actvt: allActivities) {
				boolean visited = false;
				for (String visitedActvt: visitedActivities)
					if (visitedActvt.equals(actvt.split(",")[0].split("/")[1]))	visited = true;
				if (!visited) {
					RunTimeInfo.startActivity(file, actvt.split(",")[0]);
					clickWidgetsOfActivity(file, actvt.split(",")[0].split("/")[1], actvt.split(",")[0].split("/")[1], "activity");
				}
			}*/
			
			System.out.println("-Testing Finished");
		}
		
		public static void initRuntimeEnv() {
			// initiate monkeyrunner and hierarchy viewer
			visitedActivities = new ArrayList<String>();
			view = new ViewPositionData();
			view.setDataFilter(new ViewPositionData.StringValueRetriever(
					"mID", " layout:getWidth()",  "layout:getHeight()",
					"layout:mLeft" ,"layout:mTop" ,"layout:mRight", "layout:mBottom"
					));
			ViewPositionData.debug = true;
			
			System.out.println("HierarchyView initiated.");
			m = new MonkeyWrapper();
			m.startInteractiveModel();
			System.out.println("MonkeyRunner initiated.");
		}
		
		public static void setBreakPointsAtAllLines(File file) {
			ArrayList<String> al = new ParseSmali().parseLines(file);
			jdb = new JDBStuff();
			
			try {
				jdb.initJDB(file);
				jdb.setMonitorStatus(true);
				jdb.setBreakPointsAllLines(al);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public static void exitJDB() throws Exception{
			jdb.exitJDB();
			
		}
		
		public static void clickWidgetsOfActivity(File file, String currentActivity, String activityOrLayout, String flag) {
			// this is a recursive method
			try {
				updateViewData();
				StaticLayout currentLayout = StaticInfo.getLayoutObject(activityOrLayout);
				if (flag.equals("activity")) {
					visitedActivities.add(activityOrLayout);
					System.out.println("retrieving default layout for " + activityOrLayout);
					currentLayout = StaticInfo.getDefaultLayout(file, activityOrLayout);
					if (currentLayout==null)	System.out.println("null layout");
					System.out.println("-Activity State: " + activityOrLayout + ", layout: " + currentLayout.getName() + "\n");
				} else {
					System.out.println("-Activity State: " + currentActivity + ", layout: " + currentLayout.getName() + "\n");
				}
				
				ArrayList<StaticViewNode> leavingWidgets = currentLayout.getLeavingViewNodes();
				ArrayList<StaticViewNode> stayingWidgets = currentLayout.getStayingViewNodes();
				System.out.println("staying widgets: ");
				for (StaticViewNode v: stayingWidgets)	System.out.println(v.getID());
				System.out.println("leaving widgets: ");
				for (StaticViewNode v: leavingWidgets)	System.out.println(v.getID() + v.getLeavingTargets(currentActivity, "android:onClick"));
				if (stayingWidgets.size() == 0)
					System.out.println("no staying widgets.");
				
				for (StaticViewNode stayingWidget: stayingWidgets) {
					String widgetID = stayingWidget.getID();
					Map<String, String> allEH = stayingWidget.getAllEventHandlers();
					if (allEH.size() == 0)
						System.out.println("no EH for this staying Widget " + stayingWidget.getID());
					for (String eh: allEH.keySet()) {
						if (alreadyPerformed(file, currentActivity + "," + currentLayout.getName() + "," + widgetID + "," + eh)) {
							System.out.println(widgetID + " has been clicked before, skipping...\n"); 
							continue;
						}
						System.out.println("  clicking " + widgetID + ". Activity/Layout should stay the same\n");
						sendEvent(file, currentLayout.getName(), widgetID, eh);
						//updateViewData();
						//Thread.sleep(1000);
						updateClickedEvents(file, currentActivity + "," + currentLayout.getName() + "," + widgetID);
					}
				}
				System.out.println("finished staying, now starting to leave");
				for (StaticViewNode leavingWidget : leavingWidgets) {
					String widgetID = leavingWidget.getID();
					// 1. perform the staying events
					ArrayList<String> stayingEH = leavingWidget.getStayingEvents(currentActivity);
					if (stayingEH.size() == 0) System.out.println("no staying EH for this widget " + leavingWidget.getID());
					for (String sEH : stayingEH) {
						if (alreadyPerformed(file, currentActivity + "," + currentLayout.getName() + "," + widgetID + "," + sEH)) {
							System.out.println(widgetID + " has been clicked before, skipping...\n"); 
							continue;
						}
						System.out.println("  clicking " + widgetID + ". Activity/Layout should stay the same\n");
						sendEvent(file, currentLayout.getName(), widgetID, sEH);
						//updateViewData();
						//Thread.sleep(1000);
						updateClickedEvents(file, currentActivity + "," + currentLayout.getName() + "," + widgetID);
					}
					System.out.println("starting the leaving EH for widget " + leavingWidget.getID());
					// 2. perform the leaving events
					Set<String> leavingEH = leavingWidget.getLeavingEvents(currentActivity).keySet();
					for (String lEH : leavingEH) {
						System.out.println("performing " + lEH);
						ArrayList<String> targets = leavingWidget.getLeavingTargets(currentActivity, lEH);
						// need to validate target

						if (alreadyPerformed(file, currentActivity + "," + currentLayout.getName() + "," + widgetID + "," + lEH))	{System.out.println(widgetID + " has been clicked before, skipping...\n"); continue;}
/*						if (target.split(",")[0].equals("setContentView"))
							System.out.println("  clicking " + widgetID + ". Layout will change to: " + target + ".xml\n");
						else  if (target.split(",")[0].equals("startActivity"))
							System.out.println("  clicking " + widgetID + ". Activity will change to: " + target + "\n");*/
						sendEvent(file, currentLayout.getName(), widgetID, lEH);
						Thread.sleep(1000);
						updateViewData();
						System.out.println("------\n" + currentViewData + "\n------");
						updateClickedEvents(file, currentActivity + "," + currentLayout.getName() + "," + widgetID);
						String target = "";
						if (targets.size() > 0) target = targets.get(0);
						// if more than 1 possible target, need runtime info confirmation
						if (targets.size()!=1)
							target = RunTimeInfo.getCurrentUIStatus();
						if (target.split(",")[0].equals("setContentView")) {
							clickWidgetsOfActivity(file, currentActivity, target.split(",")[1], "layout");
							System.out.println("-layout of Activity\"" + currentActivity + "\" has changed, restarting App...\n");
							RunTimeInfo.exitApp(file);
							RunTimeInfo.startApp(file);
							clickWidgetsOfActivity(file, StaticInfo.getMainActivityName(file), StaticInfo.getMainActivityName(file), "activity");
						}
						else  if (target.split(",")[0].equals("startActivity")) {
							clickWidgetsOfActivity(file, activityOrLayout, target.split(",")[1], "activity");
							System.out.println("-just finished Clicking all widgets of Activity\"" + target.split(",")[1] + "\", returning to previous Activity\n");
							System.out.println("-Activity State: " + currentActivity + ", layout: " + currentLayout.getName() + "\n");
							clickReturnButton();
						}
					}
				}
			}	catch (Exception e) {e.printStackTrace();}
		}
		
		
		public static void updateViewData() {
			currentViewData = view.retrieveViewInformation();
		}
		
		public static String[] getWidgetLocation(String widgetID) {
			String[] results = new String[4];
			for (String widgetData: currentViewData)
				if (widgetData.contains(";mID=id/" + widgetID + ";")) {
					String left = widgetData.substring(widgetData.indexOf("layout:mLeft=")+"layout:mLeft=".length());
					left = left.substring(0, left.indexOf(";"));
					String right = widgetData.substring(widgetData.indexOf("layout:mRight=")+"layout:mRight=".length());
					right = right.substring(0, right.indexOf(";"));
					String top = widgetData.substring(widgetData.indexOf("layout:mTop=")+"layout:mTop=".length());
					top = top.substring(0, top.indexOf(";"));
					String bottom = widgetData.substring(widgetData.indexOf("layout:mBottom=")+"layout:mBottom=".length());
					bottom = bottom.substring(0, bottom.indexOf(";"));
					results[0] = left;
					results[1] = top;
					results[2] = right;
					results[3] = bottom;
				} 
			return results;
		}
		
		public static void clickReturnButton() {
			m.interactiveModelPress("KEYCODE_BACK");
		}
		
		public static void sendEvent(File file, String layoutName, String widgetID, String eventHandler) throws Exception{
			if (eventHandler.equals("android:onClick")) {
				String[] widgetLocation = getWidgetLocation(widgetID);
				if (widgetLocation == null)
					System.out.println("widget location return null");
				System.out.println("original location string[]: " + widgetLocation[0] + widgetLocation[1] + widgetLocation[2] + widgetLocation[3]);
				String x = (Integer.parseInt(widgetLocation[0]) + Integer.parseInt(widgetLocation[2]))/2 + "";
				String y = (Integer.parseInt(widgetLocation[1]) + Integer.parseInt(widgetLocation[3]))/2 + "";
				System.out.println(widgetLocation[0] + "," + widgetLocation[1] + "," + widgetLocation[2] + "," + widgetLocation[3] + "," + "  " + x + "," + y);
				
				JDBStuff.clicksAndBreakPoints.add("Click," + layoutName + "," + widgetID + "," + x + "," + y);
				
				m.interactiveModelTouch(x, y, MonkeyWrapper.DOWN_AND_UP);
				System.out.println("clicking " + widgetID);
				new JDBStuff().getClickBreakPoints();
				System.out.println("clicked " + widgetID);

				//checkCodeCoverage(file, layoutName, widgetID);
			}
		}
		
		public static boolean alreadyPerformed(File file, String eventCombo) {
			boolean result = false;
			String clickedEvents = getClickedEvents(file);
			if (clickedEvents.contains(eventCombo))	result = true;
			return result;
		}
		
		public static String getClickedEvents(File file) {
			File inFile = new File(Paths.appDataDir + file.getName() + "/TestRecorder/clickedWidgets.csv");
			if (!inFile.exists())	return "";
			return StaticInfo.readDatFile(inFile);
		}
		
		public static void updateClickedEvents(File file, String eventCombo) {
			try {
				String oldClickedEvents = getClickedEvents(file);
				File outFile = new File(Paths.appDataDir + file.getName() + "/TestRecorder/clickedWidgets.csv");
				outFile.getParentFile().mkdirs();
				PrintWriter out = new PrintWriter(new FileWriter(outFile));
				out.write(oldClickedEvents + eventCombo + "\n");
				out.close();
			}	catch (Exception e) {e.printStackTrace();}
		}
		
		public static void clearClickingRecord(File file) {
			try {
				File outFile = new File(Paths.appDataDir + file.getName() + "/TestRecorder/clickedWidgets.csv");
				outFile.getParentFile().mkdirs();
				PrintWriter out = new PrintWriter(new FileWriter(outFile));
				out.write("");
				out.close();
			}	catch (Exception e) {e.printStackTrace();}
		}
}

