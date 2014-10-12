package john.runtimeValidation;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import staticFamily.StaticApp;
import staticFamily.StaticMethod;

public class DualWielding {

	public static ArrayList<ArrayList<String>> overall_result = new ArrayList<ArrayList<String>>();
	public static ArrayList<String> deviceIDs = new ArrayList<String>();
	public static ArrayList<String> methodSigs = new ArrayList<String>();
	public static ArrayList<String> scriptNames = new ArrayList<String>();
	public static ArrayList<Thread> threads = new ArrayList<Thread>();
	public static ArrayList<Integer> tcpPorts = new ArrayList<Integer>();
	public static ArrayList<StaticMethod> methods = new ArrayList<StaticMethod>();
	public static StaticApp appUnderTest;
	
	public DualWielding(File app) {
		appUnderTest = new StaticApp(app);
	}
	
	public static void addNewDevice(String deviceID, String methodSig, String scriptName, Integer tcpPort)
	{
		overall_result.add(new ArrayList<String>());
		deviceIDs.add(deviceID);
		methodSigs.add(methodSig);
		methods.add(appUnderTest.findMethodByFullSignature(methodSig));
		tcpPorts.add(tcpPort);
		scriptNames.add(scriptName);
	}
	
	public int getDeviceNumber(String ID)
	{
		return deviceIDs.indexOf(ID);
	}
	
	public static void main(String args[])
	{
		appUnderTest = new StaticApp(new File("APK/signed_CalcA.apk"));
		
		staticAnalysis.StaticInfo.initAnalysis(appUnderTest, true);

		String methodSig1 = "<com.bae.drape.gui.calculator.CalculatorActivity: "
				+ "void handleOperation(com.bae.drape.gui.calculator.CalculatorActivity$Operation)>";
		String methodSig2 = "<com.bae.drape.gui.calculator.CalculatorActivity: void handleNumber(int)>";
		String device1 = "015d3c26c9540809";
		String device2 = "015d3f1936080c05";
		
		addNewDevice(device1, methodSig1, "handleOperationUE", 7772);
		addNewDevice(device2, methodSig2, "handleNumberUE", 7773);
		
		for (int i = 0; i < deviceIDs.size(); i++) {
			threads.add(new Thread(new RuntimeValidation(scriptNames.get(i), i, methods.get(i), appUnderTest, deviceIDs.get(i), tcpPorts.get(i))));
			threads.get(i).start();
		}
		
		for (int i = 0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		for (int i = 0; i < deviceIDs.size(); i++) {
			int hitCount = overall_result.get(i).size();
			int total = methods.get(i).getAllSourceLineNumbers().size();
			
			System.out.println("\nOverall break points hit for " + scriptNames.get(i) + ": " + hitCount + "/" + total + "," +
					new DecimalFormat("#.##").format(100*(double)hitCount/(double)total) + "%");
			System.out.println("    Missed targets:");
			
			ArrayList<Integer> lines = (ArrayList<Integer>) methods.get(i).getAllSourceLineNumbers();
			ArrayList<String> hits = overall_result.get(i);
			ArrayList<Integer> intHits = new ArrayList<Integer>();
			
			for (String string: hits) {
				intHits.add(Integer.parseInt(string.trim().split(",")[2]));
			}
			
			for (Integer integer: lines) {
				if (!intHits.contains(integer))
					System.out.println("    line:" + integer);
			}
			System.out.println("    Hit targets:");
			for (Integer integer: lines) {
				if (intHits.contains(integer))
					System.out.println("    line:" + integer);
			}
		}
	}
	
}
