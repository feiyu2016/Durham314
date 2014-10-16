package john.runtimeValidation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import main.Paths;
import staticFamily.StaticApp;
import staticFamily.StaticMethod;
import zhen.version1.component.Event;
import zhen.version1.framework.Framework;

public class DualWielding {

	public ArrayList<ArrayList<String>> overall_result = new ArrayList<ArrayList<String>>();
	public ArrayList<ArrayList<Integer>> result_hit = new ArrayList<ArrayList<Integer>>();
	public ArrayList<ArrayList<Integer>> result_nohit = new ArrayList<ArrayList<Integer>>();
	public ArrayList<String> deviceIDs = new ArrayList<String>();
	public ArrayList<String> methodSigs = new ArrayList<String>();
	public ArrayList<String> scriptNames = new ArrayList<String>();
	public ArrayList<Thread> threads = new ArrayList<Thread>();
	public ArrayList<RuntimeValidation> rtv = new ArrayList<RuntimeValidation>();
	public ArrayList<Integer> tcpPorts = new ArrayList<Integer>();
	public ArrayList<StaticMethod> methods = new ArrayList<StaticMethod>();
	public StaticApp appUnderTest;
	public Framework frame;
	public Event finalEvent;
	private Integer[] targetLines;
	
	public DualWielding(StaticApp staticAppUnderTest, Framework frame) {
		this.appUnderTest = staticAppUnderTest;
		this.frame = frame;
	}
	
	public void addNewDevice(String deviceID, String methodSig, Integer[] targetLines, String scriptName, Integer tcpPort)
	{
		//overall_result.add(new ArrayList<String>());
		result_hit.add(new ArrayList<Integer>());
		result_nohit.add(new ArrayList<Integer>());
		deviceIDs.add(deviceID);
		methodSigs.add(methodSig);
		methods.add(appUnderTest.findMethodByFullSignature(methodSig));
		tcpPorts.add(tcpPort);
		scriptNames.add(scriptName);
		this.targetLines = targetLines;
		Process pc = null;
		
		try {
			pc = Runtime.getRuntime().exec(Paths.adbPath +" -s " + deviceID + " uninstall " + this.appUnderTest.getPackageName());
			pc.waitFor();
			System.out.println("SOOT APP UNINSTALLED!");
			pc = Runtime.getRuntime().exec(Paths.adbPath +" -s " + deviceID + " install " + this.appUnderTest.getSmaliInstrumentedAppPath());
			pc.waitFor();
			System.out.println("SMALI APP INSTALLED!");
		} catch (IOException | InterruptedException e) { e.printStackTrace(); }
		
	}
	
	public int getDeviceNumber(String ID)
	{
		return deviceIDs.indexOf(ID);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<Integer>> runTest()
	{
		for (int i = 0; i < deviceIDs.size(); i++) {
			rtv.add(new RuntimeValidation(scriptNames.get(i), i, methods.get(i), 
					appUnderTest, deviceIDs.get(i), tcpPorts.get(i), this.frame, targetLines));
			
			threads.add(new Thread(rtv.get(i)));
			threads.get(i).start();
		}
		
		for (int i = 0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
				overall_result.add((ArrayList<String>) rtv.get(i).overall_result.clone());
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		for (int i = 0; i < deviceIDs.size(); i++) {
			int hitCount = overall_result.get(i).size();
			int total = methods.get(i).getAllSourceLineNumbers().size();
			
			System.out.println("\nOverall break points hit for " + scriptNames.get(i) + ": " + hitCount + "/" + total + "," +
					new DecimalFormat("#.##").format(100*(double)hitCount/(double)total) + "%");
			//System.out.println("    Missed targets:");
			
			ArrayList<Integer> lines = (ArrayList<Integer>) methods.get(i).getAllSourceLineNumbers();
			ArrayList<String> hits = overall_result.get(i);
			ArrayList<Integer> intHits = new ArrayList<Integer>();
			
			for (String string: hits) {
				intHits.add(Integer.parseInt(string.trim().split(",")[2]));
			}
			
			for (Integer integer: lines) {
				if (!intHits.contains(integer)) {
				//	System.out.println("    line:" + integer);
					result_nohit.get(i).add(integer);
				}
			}
			//System.out.println("    Hit targets:");
			for (Integer integer: lines) {
				if (intHits.contains(integer)) {
				//	System.out.println("    line:" + integer);
					result_hit.get(i).add(integer);
				}
			}
		}
		
		return result_hit;
	}
}
