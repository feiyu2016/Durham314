package john.runtimeValidation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import john.jdb.JDBInterface;
import main.Paths;
import smali.TaintAnalysis.TaintHelper;
import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import zhen.version1.component.Event;
import zhen.version1.framework.Common;
import zhen.version1.framework.Framework;
import zhen.version1.test.TaintedEventGeneration;

public class RuntimeValidation implements Runnable{
	
	private String scriptName;
	private String packageName;
	private String mainActivity;
	private StaticApp staticApp;
	private StaticMethod targetMethod;
	private Framework frame;
	
	private String deviceID;
	private int tcpPort;

	public ArrayList<String> overall_result = new ArrayList<String>();
	private ArrayList<Integer> overallInt = new ArrayList<Integer>();
	private Integer[] targetLines;
	
	public RuntimeValidation(String string, int deviceNumber, StaticMethod m, 
			StaticApp staticApp, String deviceID, int tcpPort, Framework frame, Integer[] targetLines) {
		this.targetMethod = m;
		this.staticApp = staticApp;
		this.scriptName = string;
		this.deviceID = deviceID;
		this.tcpPort = tcpPort;
		this.frame = frame;
		this.targetLines = targetLines;
	}
	
	public void run() {
		System.out.println("run");
		this.runAllScripts();
		System.out.println("done");
		this.getIntegerLineNumbersFromOverallResultsWhichIsAString();
		System.out.println("donedone");
		this.getNewEventSequencesAndRunThemImmediatelyAfterwards();
		System.out.println("donedonedone");
	}

	private ArrayList<String> getAllScripts()
	{
		File directory = new File(ScriptPath.scriptPath);  
		File[] files = directory.listFiles();
		ArrayList<String> ret = new ArrayList<String>();
		  
		for (File file: files)  {  
			String string = file.getAbsolutePath();
			if (string.contains(scriptName + "_"))
				ret.add(string); 
		}
		
		return ret;
	}
	
	public void runAllScripts()
	{
		ArrayList<String> scripts = getAllScripts();
		this.mainActivity = staticApp.getMainActivity().getName();
		this.packageName = staticApp.getPackageName();
		StaticClass c = targetMethod.getDeclaringClass(staticApp);
		try {
			int scriptCounter = 1;
			for (String script:scripts) {
				System.out.println("\nscript " + scriptCounter++ + "/" + scripts.size() + " running on Device " + deviceID + " ...");
				startApp();
				
				JDBInterface jdb = new JDBInterface(deviceID, packageName, tcpPort);
				jdb.initJDB();
				jdb.setBreakPointsAtLines(c.getName(), (ArrayList<Integer>) targetMethod.getAllSourceLineNumbers());
				jdb.setMonitorStatus(true);
				Process PC = Runtime.getRuntime().exec(Paths.androidToolPath + "monkeyrunner " + script);
				System.out.println("-HERE " + Paths.androidToolPath + "monkeyrunner " + script);
				PC.waitFor();
				jdb.exitJDB();
				stopApp();
				
				for (String bp: jdb.getBPsHit()) {
					if (!overall_result.contains(bp))
						overall_result.add(bp);
					
					System.out.println("    " + bp);
				}
			}
		} catch (Exception e) {
			System.out.println("Couldn't exec monkeyrunner or JDB.");
			e.printStackTrace();
		}
	}
	
	private void startApp() {
		try {
			final Process pc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + deviceID + " shell am start -n " + packageName + "/" + mainActivity);
			pc.waitFor();
			pc.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void stopApp() {
		try {
			final Process pc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + deviceID + " shell am force-stop " + packageName);
			pc.waitFor();
			pc.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getIntegerLineNumbersFromOverallResultsWhichIsAString()
	{
		for (String string : overall_result) {
			overallInt.add(Integer.parseInt(string.trim().split(",")[2]));
		}
		
		for (Integer integer : overallInt)
			System.out.println(integer);
	}
	
	private ArrayList<Event> getFinalEvents() 
	{
		ArrayList<Event> result = new ArrayList<Event>();
		Map<String, List<Event>> meMap = frame.rInfo.getMethodEventMap();
		for (Map.Entry<String, List<Event>> entry : meMap.entrySet()) {
			if (!result.contains(entry.getValue().get(entry.getValue().size()-1)))
				result.add(entry.getValue().get(entry.getValue().size()-1));
		}
		System.out.println("results " + result.size());
		return result;
	}
	
	private void getNewEventSequencesAndRunThemImmediatelyAfterwards()
	{
		TaintHelper th = new TaintHelper(staticApp);
		th.setMethod(targetMethod);
		th.setBPsHit(overallInt);
		
		TaintedEventGeneration teg = new TaintedEventGeneration();
		ValidationExecutor ve = new ValidationExecutor(deviceID);
		StaticClass c = targetMethod.getDeclaringClass(staticApp);
		ve.init();
		System.out.println("SADFASDGHASH");
		this.log("getNewEventSequencesAndRunThemImmediatelyAfterwards\n");
		
		for (Integer target : targetLines) {
			
			if (!overallInt.contains(target)) {
				System.out.println(target);
				for (Event event : getFinalEvents()) {
					try {
						ArrayList<Event[]> tegOut = (ArrayList<Event[]>) teg.findSequence(frame, staticApp, th.findTaintedMethods(target), event);
						System.out.println("tegOut,size:"+tegOut.size());
						log("findSequence checking:");
						log("target line:"+target);
						log("findTaintedMethods"+th.findTaintedMethods(target));
						log("finalEvent"+event);
						log("tegOut:\t"+tegOut);
						log("\n");
						
						startApp();
						JDBInterface jdb = new JDBInterface(deviceID, packageName, tcpPort);
						jdb.initJDB();
						jdb.setBreakPointsAtLines(c.getName(), (ArrayList<Integer>) targetMethod.getAllSourceLineNumbers());
						jdb.setMonitorStatus(true);
						Thread.sleep(1000);
						for (Event[] array : tegOut) {
							for (Event event2 : array) {
								try {
									String x = event2.getValue(Common.event_att_click_x).toString();
									String y = event2.getValue(Common.event_att_click_y).toString();
									ve.touch(x, y);
									Thread.sleep(1000);
									System.out.print("Zhen touch: (" + x + "," + y + ")");
								} catch (NullPointerException | InterruptedException e) {}
							}
							System.out.println();
						}
						Thread.sleep(1000);
						jdb.exitJDB();
						stopApp();
						
						for (String bp: jdb.getBPsHit()) {
							System.out.println("    " + bp);
						}
					} catch (Exception e) {}
				}
			}
		}
	}
	
	PrintWriter pw = null;
	private void log(Object o){
		if(o == null) return;
		if(pw == null)
			try {
				pw = new PrintWriter(new File("log"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		pw.println(o.toString());
		pw.flush();
	}
}
