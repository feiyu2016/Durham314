package john.runtimeValidation;

import java.io.File;
import java.util.ArrayList;

import jdb.JDBStuff;
import main.Paths;
import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;

public class RuntimeValidation implements Runnable{
	
	private String scriptName;
	private String packageName;
	private String mainActivity;
	private StaticApp staticApp;
	private StaticMethod targetMethod;
	private int flag;
	
	private Process monkeyPC;
	private String deviceID;
	private int tcpPort;


	public void run() {
		if (flag == 1)
			this.runAllScripts();
		else if (flag == 2)
			this.runAllScripts();
	}
	
	public RuntimeValidation(String string, int flag,
			StaticMethod m, StaticApp staticApp, String deviceID, int tcpPort) {
		this.targetMethod = m;
		this.staticApp = staticApp;
		this.flag = flag;
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
				JDBStuff jdb = new JDBStuff();
				jdb.setlocalTcpPort(tcpPort);
				jdb.initJDB(packageName, deviceID, flag);

				jdb.setBreakPointsAtLines(c.getName(), (ArrayList<Integer>) targetMethod.getAllSourceLineNumbers());
				jdb.setMonitorStatus(true);
				monkeyPC = Runtime.getRuntime().exec(Paths.androidToolPath + "monkeyrunner " + script);
				monkeyPC.waitFor();
				jdb.exitJDB();
				stopApp();
				if (flag == 1) {
					for (String bp: DualWielding.single_result_1) {
						if (!DualWielding.overall_result1.contains(bp))
							DualWielding.overall_result1.add(bp);
						System.out.println("   " + bp);
					}
				}
				else if (flag ==2) {
					for (String bp: DualWielding.single_result_2) {
						if (!DualWielding.overall_result2.contains(bp))
							DualWielding.overall_result2.add(bp);
						System.out.println("   " + bp);
					}
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
