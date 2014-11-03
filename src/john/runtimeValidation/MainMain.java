package john.runtimeValidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import john.generateSequences.GenerateSequences;
import main.Paths;
import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;
import zhen.version1.framework.Common;
import zhen.version1.framework.Framework;
import zhen.version1.framework.RunTimeInformation;

public class MainMain {
	
	static String device1 = "015d3c26c9540809";
	
	public static void main(String args[])
	{
		
//		String tmp = "/home/zhenxu/git/Durham314/generated/CalcA.apk/InstrumentedApps/CalcA_soot.apk";
//		CommandLine.executeADBCommand("install -r "+tmp, device1);
		
		//choose the name of apk file
		String[] targetApp = {
				"CalcA.apk",
				"KitteyKittey.apk",
				"net.mandaria.tippytipper.apk",
				"backupHelper.apk",
				"TheApp.apk",
		};
		int appSelect = 0;
		String appPath = "APK/" + targetApp[appSelect];
		
		String[] targetMethods = {
//				"<com.cs141.kittey.kittey.MainKitteyActivity: void nextButton(android.view.View)>",
//				"<com.bae.drape.gui.calculator.CalculatorActivity: "
//					+ "void handleOperation(com.bae.drape.gui.calculator.CalculatorActivity$Operation)>",
//				"<com.bae.drape.gui.calculator.CalculatorActivity: void handleNumber(int)>",
//				"<com.example.backupHelper.BackupActivity: boolean onMenuItemSelected(int android.view.MenuItem)>", // 138 159 177 
				"<net.mandaria.tippytipperlibrary.activities.TippyTipper: void addBillAmount(java.lang.String)>",
//				"<net.mandaria.tippytipperlibrary.activities.TippyTipper: boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)>",
				"<net.mandaria.tippytipperlibrary.activities.SplitBill: void removePerson()>",
//				"<the.app.Irwin: void doTheThing()>", // 71
		};
		
		Integer[][] targetLines = {
			{287,321,322}, // addBillAmount
//			{431,247,438,443,257,258,262}, // onOptionsItemSelected
//			{649,430,475,656,436,455,459,442,445,448}, // handleOperation
//			{482}, // handleNumber
			{129,133}, // removePerson
			//{138,159,177}, // onMenuItemSelected
			//{71}, // doTheThing
		};
		
		ArrayList<String> connectedDevices = getConnectedDeviceIDs();
		
		System.out.println(appPath);
 		Framework frame = traversalStep(appPath);
 		System.out.println("Traversal Complete");
 		Scanner scanner = new Scanner(System.in);
 		System.out.println("Paused right now. Input 'go on' to continue.");
 		String input = "";
 		while (!input.equals("goon")) {
 			input = scanner.next();
 		}
		heuristicGenerationStep(frame, targetMethods);
 		System.out.println("Heuristic Generation Complete");
		heuristicValidationStep(new File(appPath), frame, targetMethods, targetLines, connectedDevices);
		//getConnectedDeviceIDs();
		
	}
	
	private static Framework traversalStep(String path) 
	{
		//setup input parameters
		Map<String,Object> att = new HashMap<String,Object>();
		att.put(Common.apkPath	, path);
		
		Framework frame = new Framework(att);
		
		//once the step control is added, the program will wait for human instruction
		//before entering next operation loop
		//addExplorerStepControl(frame);
		//add a call back method which is called after the finish of traversal
		addOnTraverseFinishCallBack(frame);

		//frame.enableStdinMonitor(true);
		//NOTE: right now it does require apk installed on the device manually
		//and please close the app if previous opened
		frame.setup();//initialize
		frame.start();//start experiment
		frame.terminate();
		
		
		return frame;
	}
	
	private static void heuristicGenerationStep(Framework frame, String[] targets) 
	{
		//for (String target : targets) {
			String target = targets[0];
			String scriptName = target.trim().split(" ")[2].trim().split("\\(")[0];
			String packageName = parsepackageName(target);
			String activityName = parseActivityName(target);
			
			GenerateSequences gs = new GenerateSequences(frame, targets, true);
			GenerateValidationScripts gvs = new GenerateValidationScripts(scriptName, packageName, activityName, device1);
			
			gvs.setEventSequences(gs.getEnhancedSequences());
			
			try {
				gvs.generateScripts();
			} catch (IOException e) { e.printStackTrace(); }
		//}
	}
	
	private static ArrayList<ArrayList<Integer>> heuristicValidationStep(File appUnderTest, Framework frame, String[] targets, Integer[][] targetLines, ArrayList<String> connectedDevices)
	{
		DualWielding dw = new DualWielding(frame.sInfo.app, frame);
		
		int tcpPort = 7772;
		for (int i = 0; i < connectedDevices.size(); i++) {
			String scriptName = targets[i].trim().split(" ")[2].trim().split("\\(")[0];
			Integer[] lines = targetLines[i];
			String device = connectedDevices.get(i);
			dw.addNewDevice(device, targets[i], lines, scriptName, tcpPort++);
		}
		
		return dw.runTest();
		// comment
	}
	
	private static String parsepackageName(String name)
	{
		String temp = name.trim().split("\\:")[0];
		String[] split = temp.split("\\.");
		temp = "";
		
		for (int i = 0; i < split.length - 1; i++) {
			temp += split[i];
			if (i < (split.length - 2)) {
				temp += ".";
			}
		}
		
		temp = temp.replace("<", "");
		
		return temp;
	}
	
	private static String parseActivityName(String name)
	{
		String temp = name.trim().split("\\:")[0];
		String[] split = temp.split("\\.");
		
		return split[split.length - 1].trim();
	}
	
	private static void addOnTraverseFinishCallBack(Framework frame){
		frame.setOnProcedureEndsCallBack(new Framework.OnProcedureEndsCallBack(){
			@Override
			public void action(Framework frame) {
				Utility.info(Framework.TAG, "OnTraverseFinishCallBack ");
				// show the map of method -> event
				// and find a set possible events which leads to a method
				String targetMethod = "com.bae.drape.gui.calculaor.CalculatorActivity: void handleOperation(com.bae.drape.gui.calculator.CalculatorActivity$Operation)";
				Map<String, List<Event>> map = frame.rInfo.getMethodEventMap();
				List<Event> events = null;
				for(Entry<String, List<Event>> entry : map.entrySet()){
					String key = entry.getKey();
					Utility.info(RunTimeInformation.TAG,entry);	//show everything
					if(key.trim().equals(targetMethod)){
						Utility.info(Framework.TAG, "found targetMethod");
						events = entry.getValue(); break;
					}
				}
				
				//sample of entry.toString()
				//	com.example.backupHelper.BackupFilesListAdapter: void reset(boolean)
				//	=[launch com.example.backupHelper/com.example.backupHelper.BackupActivity]     
				if(events!= null && !events.isEmpty()){
					Utility.info(Framework.TAG, "Possible event set");
					for(Event event : events){
						Utility.info(Framework.TAG, event);
					}
					Event targetEvent = events.get(0);
					UIState targetUI = targetEvent.getSource();
					List<Event> path = frame.rInfo.getEventSequence(UIState.Launcher, targetUI);
					if(path == null){
						if(targetEvent.getEventType() == Event.iLAUNCH){
							path = new ArrayList<Event>();
							path.add(targetEvent);
						}
					}else{
						path.add(targetEvent);
					}
					
					Utility.info(Framework.TAG, "Path to UI with event which trigger the target");
					Utility.info(Framework.TAG, path);
					
					//reply the event sequence
					if(path!=null && path.isEmpty()){
						frame.traverseExecuter.applyEventSequence(path.toArray(new Event[0]));
					}
				}else{
					Utility.info(Framework.TAG, "Event set empty");
				}
			}
		});
	}
	
	public static ArrayList<String> getConnectedDeviceIDs() {
		ArrayList<String> result = new ArrayList<String>();
		try {
			Process pc = Runtime.getRuntime().exec(Paths.adbPath + " devices");
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			while ((line = in.readLine())!= null) {
				if (line.startsWith("List of devices attached") || !line.contains("\t") || !line.contains("device"))
					continue;
				String id = line.substring(0, line.indexOf("\t"));
				System.out.println(id);
				result.add(id);
			}
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
}
