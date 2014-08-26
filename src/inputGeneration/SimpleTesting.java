package inputGeneration;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import main.Paths;
import viewer.MonkeyWrapper;
import viewer.ViewPositionData;


	public class SimpleTesting {
		
		public static ArrayList<String> currentViewData;
		public static MonkeyWrapper m;
		public static ViewPositionData view;
		public static File processedAPK;
		public static ArrayList<String> visitedActivities;

		public static void clickAll(File file) throws Exception{

			
//			RunTimeInfo.startApp(file);	// adb am start -n package/activity
			
			initRuntimeEnv();		// initiate HierarchyViewer and MonkeyRunner
			
			
			String mainActivityName = StaticInfo.getMainActivityName(file);
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
			}
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
			view.debug = true;
			
			System.out.println("HierarchyView initiated.");
			m = new MonkeyWrapper();
			m.startInteractiveModel();
			System.out.println("MonkeyRunner initiated.");
		}
		
		public static void clickWidgetsOfActivity(File file, String currentActivity, String activityOrLayout, String flag) {
			// this is a recursive method
			try {
				updateViewData();
				String currentLayout = activityOrLayout;
				if (flag.equals("activity")) {
					visitedActivities.add(activityOrLayout);
					currentLayout = OldStaticInfo.getDefaultLayout(file, activityOrLayout);
					System.out.println("-Activity State: " + activityOrLayout + ", layout: " + currentLayout + "\n");
				} else {
					System.out.println("-Activity State: " + currentActivity + ", layout: " + currentLayout + "\n");
				}
				ArrayList<String> stayingWidgets = OldStaticInfo.getStayingWidgets(file, currentLayout);
				ArrayList<String> leavingWidgets = OldStaticInfo.getLeavingWidgets(file, currentLayout);
				
				for (String stayingWidget: stayingWidgets) {
					String widgetType = stayingWidget.split(",")[0];
					String widgetID = stayingWidget.split(",")[1];
					String widgetOnClick = stayingWidget.split(",")[2];
					if (alreadyClicked(file, currentActivity + "," + currentLayout + "," + widgetID))	{System.out.println(widgetID + " has been clicked before, skipping...\n"); continue;}
					System.out.println("  clicking " + widgetID + ". Activity/Layout should stay the same\n");
					clickWidget(file, currentLayout, widgetID);
					updateViewData();
					//Thread.sleep(1000);
					updateClickedEvents(file, currentActivity + "," + currentLayout + "," + widgetID);
				}
				
				for (String leavingWidget : leavingWidgets) {
					String widgetType = leavingWidget.split(",")[1];
					String widgetID = leavingWidget.split(",")[2];
					String widgetOnClick = leavingWidget.split(",")[3];
					String target = leavingWidget.split(",")[4];
					if (alreadyClicked(file, currentActivity + "," + currentLayout + "," + widgetID))	{System.out.println(widgetID + " has been clicked before, skipping...\n"); continue;}
					if (leavingWidget.split(",")[0].equals("newLayout"))
						System.out.println("  clicking " + widgetID + ". Layout will change to: " + target + ".xml\n");
					else  if (leavingWidget.split(",")[0].equals("newActivity"))
						System.out.println("  clicking " + widgetID + ". Activity will change to: " + target + "\n");
					clickWidget(file, currentLayout, widgetID);
					updateViewData();
					updateClickedEvents(file, currentActivity + "," + currentLayout + "," + widgetID);
					if (leavingWidget.split(",")[0].equals("newLayout")) {
						clickWidgetsOfActivity(file, currentActivity, target, "layout");
						System.out.println("-layout of Activity\"" + currentActivity + "\" has changed, restarting App...\n");
						RunTimeInfo.exitApp(file);
						RunTimeInfo.startApp(file);
						clickWidgetsOfActivity(file, StaticInfo.getMainActivityName(file), StaticInfo.getMainActivityName(file), "activity");
					}
					else  if (leavingWidget.split(",")[0].equals("newActivity")) {
						clickWidgetsOfActivity(file, activityOrLayout, target, "activity");
						System.out.println("-just finished Clicking all widgets of Activity\"" + target + "\", returning to previous Activity\n");
						System.out.println("-Activity State: " + currentActivity + ", layout: " + currentLayout + "\n");
						clickReturnButton();
					}
				}
			}	catch (Exception e) {e.printStackTrace();}
		}
		
		
		public static void updateViewData() {
			currentViewData = view.retrieveViewInformation();
			try {
				PrintWriter out = new PrintWriter(new FileWriter("/home/zhenxu/workspace/result/aaa.txt", true));
				out.write("---------------------\n");
				for (String s: currentViewData) {
					out.write(s + "\n");
					out.flush();
				}
				out.close();
			}	catch (Exception e) {e.printStackTrace();}
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
		
		public static void clickWidget(File file, String layoutName, String widgetID) throws Exception{
			String[] widgetLocation = getWidgetLocation(widgetID);
			String x = (Integer.parseInt(widgetLocation[0]) + Integer.parseInt(widgetLocation[2]))/2 + "";
			String y = (Integer.parseInt(widgetLocation[1]) + Integer.parseInt(widgetLocation[3]))/2 + "";
			System.out.println(widgetLocation[0] + "," + widgetLocation[1] + "," + widgetLocation[2] + "," + widgetLocation[3] + "," + "  " + x + "," + y);
			JDBStuff.clicksAndBreakPoints.add("Click," + layoutName + "," + widgetID + "," + x + "," + y);
			m.interactiveModelTouch(x, y, MonkeyWrapper.DOWN_AND_UP);
			new JDBStuff().getClickBreakPoints();
			//Thread.sleep(10);
			//checkCodeCoverage(file, layoutName, widgetID);
		}
		
		
		public static boolean alreadyClicked(File file, String eventCombo) {
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

