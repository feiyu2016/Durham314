package john.runtimeValidation;

import java.io.File;
import java.util.ArrayList;

import main.Paths;
import john.jdb.JDBInterface;
import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;

public class RuntimeValidation implements Runnable{
	
	private String scriptName;
	private String packageName;
	private String mainActivity;
	private StaticApp staticApp;
	private StaticMethod targetMethod;
	private int deviceNumber;
	
	private Process monkeyPC;
	private String deviceID;
	private int tcpPort;


	public void run() {
		runAllScripts();
	}
	
	public RuntimeValidation(String string, int deviceNumber,
			StaticMethod m, StaticApp staticApp, String deviceID, int tcpPort) {
		this.targetMethod = m;
		this.staticApp = staticApp;
		this.deviceNumber = deviceNumber;
		this.scriptName = string;
		this.deviceID = deviceID;
		this.tcpPort = tcpPort;
	}

	private ArrayList<String> getAllScripts()
	{
		File directory = new File(ScriptPath.scriptPath);  
		File[] files = directory.listFiles();
		ArrayList<String> ret = new ArrayList<String>();
		  
		for (File file: files)  {  
		   String string = file.toString();
		  if (string.contains(scriptName))
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
				System.out.println("JDBInterface object created");
				jdb.initJDB();
				System.out.println("initJDB");
				jdb.setBreakPointsAtLines(c.getName(), (ArrayList<Integer>) targetMethod.getAllSourceLineNumbers());
				System.out.println("setBreakPointsAtLines");
				jdb.setMonitorStatus(true);
				System.out.println("before monkey");
				monkeyPC = Runtime.getRuntime().exec(Paths.androidToolPath + "monkeyrunner " + script);
				
				monkeyPC.waitFor();
				jdb.exitJDB();
				stopApp();
				
				for (String bp: jdb.getBPsHit()) {
					if (!DualWielding.overall_result.get(deviceNumber).contains(bp))
						DualWielding.overall_result.get(deviceNumber).add(bp);
					
					System.out.println("    " + bp);
				}
			}
		} catch (Exception e) {
			System.out.println("Couldn't exec monkeyrunner or JDB.");
			e.printStackTrace();
		}
	}
	
	private void startApp() throws Exception {
		Process pc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + deviceID + " shell am start -n " + packageName + "/" + mainActivity);
		pc.waitFor();
	}
	
	private void stopApp() throws Exception {
		Process pc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + deviceID + " shell am force-stop " + packageName);
		pc.waitFor();
	}
	
}
