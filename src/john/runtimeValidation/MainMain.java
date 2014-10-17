package john.runtimeValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import main.Paths;
import staticAnalysis.StaticInfo;
import staticFamily.StaticApp;
import john.generateSequences.GenerateSequences;
import john.runtimeValidation.DualWielding;
import john.runtimeValidation.GenerateValidationScripts;
import zhen.version1.Support.CommandLine;
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
				"net.mandaria.tippytipper.apk"
		};
		int appSelect = 0;
		String appPath = "APK/" + targetApp[appSelect];
		
		String[] targetMethods = {
//				"<com.cs141.kittey.kittey.MainKitteyActivity: void nextButton(android.view.View)>",
				"<com.bae.drape.gui.calculator.CalculatorActivity: "
						+ "void handleOperation(com.bae.drape.gui.calculator.CalculatorActivity$Operation)>",
//				"<com.bae.drape.gui.calculator.CalculatorActivity: void handleNumber(int)>"
//				"<net.mandaria.tippytipperlibrary.activities.TippyTipper: void addBillAmount(java.lang.String)>"
//				"<net.mandaria.tippytipperlibrary.activities.TippyTipper: boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)>"
		};
		
		Integer[] targetLines = {
			//287,321,322, // addBillAmount
			//431,247,438,443,257,258,262, // onOptionsItemSelected
			649,430,475,656,436,455,459, // handleOperation
		};
		
		System.out.println(appPath);
 		Framework frame = traversalStep(appPath);
 		System.out.println("Traversal Complete");
		//heuristicGenerationStep(frame, targetMethods);
 		System.out.println("Heuristic Generation Complete");
		heuristicValidationStep(new File(appPath), frame, targetMethods, targetLines);
		
		
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
			
			System.out.println("Unenhanced Sequences:");
			for (String string : gs.getUnenhancedSequences()) {
				System.out.println(string);
			}
			
			System.out.println("Enhanced Sequences:");
			for (String string : gs.getEnhancedSequences()) {
				System.out.println(string);
			}
			
			gvs.setEventSequences(gs.getEnhancedSequences());
			
			try {
				gvs.generateScripts();
			} catch (IOException e) { e.printStackTrace(); }
		//}
	}
	
	private static ArrayList<ArrayList<Integer>> heuristicValidationStep(File appUnderTest, Framework frame, String[] targets, Integer[] targetLines)
	{
		DualWielding dw = new DualWielding(frame.sInfo.app, frame);
		
		int tcpPort = 7772;
		for (int i = 0; i < targets.length; i++) {
			String scriptName = targets[i].trim().split(" ")[2].trim().split("\\(")[0];
			scriptName = "handleOperation";
			dw.addNewDevice(device1, targets[i], targetLines, scriptName, tcpPort++);
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
	
	
	public void installSootApp(StaticApp app, String deviceID) {
		try {
			Process pc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + deviceID + " uninstall " + app.getPackageName());
			pc.waitFor();
			pc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + deviceID + " install " + app.getSootInstrumentedAppPath());
			pc.waitFor();
		}	catch (Exception e) {e.printStackTrace();}
	}
	
	
}

/*
 
 staticAnalysis.StaticInfo.initAnalysis(appUnderTest, true);

		//String methodSig1 = "<com.bae.drape.gui.calculator.CalculatorActivity: "
				//+ "void handleOperation(com.bae.drape.gui.calculator.CalculatorActivity$Operation)>";
		//String methodSig2 = "<com.bae.drape.gui.calculator.CalculatorActivity: void handleNumber(int)>";
		String methodSig1 = "<com.cs141.kittey.kittey.MainKitteyActivity: void nextButton(android.view.View)>";
		String device1 = "015d3c26c9540809";
		//String device2 = "015d3f1936080c05";
		
		addNewDevice(device1, methodSig1, "nextButtonUE", 7772);
		//addNewDevice(device2, methodSig2, "nextButton", 7773);
		
		
		TaintHelper th = new TaintHelper(appUnderTest);
		
		for (int i = 0; i < deviceIDs.size(); i++) {
			th.setMethod(methods.get(i));
			th.setBPsHit(result_hit.get(i));
			
			for (int j : result_nohit.get(i)) {
				ArrayList<String> strings = th.findTaintedMethods(j);
				System.out.println("Line : " + j);
				for (String string: strings) 
					System.out.println(string);
			}
				
		}
 
 */
