package john.runtimeValidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import john.jdb.JDBInterface;
import main.Paths;
import smali.BackTrackAnalysis.BackTrackHelper;
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
	private ValidationExecutor cuter;
	
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
		this.cuter = new ValidationExecutor(deviceID);
		this.cuter.init();
	}
	
	public void run() {
		this.runAllScripts();
		//this.getIntegerLineNumbersFromOverallResultsWhichIsAString();
		//this.getNewEventSequencesAndRunThemImmediatelyAfterwards();
		System.out.println("done");
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
	private class RunOneSequence implements Runnable {
		
		String script;
		
		public RunOneSequence(String script) {
			this.script = script;
		}
		@Override
		public void run() {
			runOneSequence(script);
		}
		
		private void runOneSequence(String script)
		{
			BufferedReader br = null;
			
			try {
				br = new BufferedReader(new FileReader(script));
				String line;
				String x, y;
				
				while ((line = br.readLine()) != null ){
					x = line.split(",")[0];
					y = line.split(",")[1];
					cuter.closeKeyboard(deviceID);
					Thread.sleep(900);
					cuter.touch(x, y);
					Thread.sleep(900);
				}
				
				br.close();
			} catch (Exception e) { e.printStackTrace(); }
			
			
		}
	}
	
	private class SetUpJDB implements Runnable {
		
		StaticClass c;
		JDBInterface jdb;
		
		public SetUpJDB(StaticClass c) {
			this.c = c;
			jdb = new JDBInterface(deviceID, packageName, tcpPort);
		}
		
		@Override
		public void run() {
			doit();
		}
		
		private void doit()
		{
			try {
				jdb.initJDB();
				jdb.setBreakPointsAtLines(c.getName(), (ArrayList<Integer>) targetMethod.getAllSourceLineNumbers());
				jdb.setMonitorStatus(true);
			} catch (Exception e) {}
		}
		
		public void exit() 
		{
			jdb.exitJDB();
		}
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
				Thread.sleep(450);
				SetUpJDB suj = new SetUpJDB(c);
				Thread jdbThread = new Thread(suj);
				jdbThread.start();
				Thread.sleep(900);
				Thread seqThread = new Thread(new RunOneSequence(script));
				seqThread.start();
				seqThread.join();
				Thread.sleep(900);
				suj.exit();
				jdbThread.join();
				stopApp();
				
				for (String bp: suj.jdb.getBPsHit()) {
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
		BackTrackHelper bth = new BackTrackHelper(staticApp);
		bth.setMethod(targetMethod);
		bth.setBPsHit(overallInt);
		
		TaintedEventGeneration teg = new TaintedEventGeneration();
		StaticClass c = targetMethod.getDeclaringClass(staticApp);
		
		System.out.println("SADFASDGHASH");
		this.log("getNewEventSequencesAndRunThemImmediatelyAfterwards\n");
		
		for (Integer target : targetLines) {
			
			if (!overallInt.contains(target)) {
				log("target line: " +target);
				for (Event event : getFinalEvents()) {
					try {
						log("event method: " + event.getMethodHits());
						ArrayList<Event[]> tegOut = (ArrayList<Event[]>) teg.findSequence(frame, staticApp, bth.findResponsibleMethods(target), event);

						System.out.println("tegOut,size:"+tegOut.size());
						log("findSequence checking:");
						log("target line:"+target);
						log("findTaintedMethods"+bth.findResponsibleMethods(target));
						log("finalEvent"+event);
						log("tegOut:\t"+tegOut);
						log("\n");

						for (Event[] array : tegOut) {
							startApp();
							Thread.sleep(450);
							SetUpJDB suj = new SetUpJDB(c);
							Thread jdbThread = new Thread(suj);
							jdbThread.start();
							Thread.sleep(900);
							for (Event event2 : array) {
								try {
									String x = event2.getValue(Common.event_att_click_x).toString();
									String y = event2.getValue(Common.event_att_click_y).toString();
									cuter.closeKeyboard(deviceID);
									Thread.sleep(900);
									cuter.touch(x, y);
									Thread.sleep(9000);
									System.out.print(x + "," + y + " ");
								} catch (NullPointerException | InterruptedException e) {}
							}
							System.out.println();
							Thread.sleep(900);
							suj.exit();
							jdbThread.join();
							stopApp();
							
							for (String bp: suj.jdb.getBPsHit()) {
								if (!overall_result.contains(bp))
									overall_result.add(bp);
								
								System.out.println("    " + bp);
							}
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
    }
}
