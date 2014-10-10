package john.runtimeValidation;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import staticFamily.StaticApp;
import staticFamily.StaticMethod;

public class DualWielding {

	
	public static ArrayList<String> overall_result1 = new ArrayList<String>();
	public static ArrayList<String> overall_result2 = new ArrayList<String>();
	private static String device1 = "015d3c26c9540809";
	private static String device2 = "015d3f1936080c05";
	public static ArrayList<String> single_result_1 = new ArrayList<String>();
	public static ArrayList<String> single_result_2 = new ArrayList<String>();
	
	public static void main(String args[])
	{
		StaticApp staticApp = new StaticApp(new File("APK/signed_CalcA.apk"));
		
		staticAnalysis.StaticInfo.initAnalysis(staticApp, true);

		String methodSig1 = "<com.bae.drape.gui.calculator.CalculatorActivity: "
				+ "void handleOperation(com.bae.drape.gui.calculator.CalculatorActivity$Operation)>";
		String methodsig2 = "<com.bae.drape.gui.calculator.CalculatorActivity: void handleNumber(int)>";
		StaticMethod m1 = staticApp.findMethodByFullSignature(methodSig1);
		StaticMethod m2 = staticApp.findMethodByFullSignature(methodsig2);
		
		Thread t1 = new Thread(new RuntimeValidation("handleOperation", 1, m1, staticApp, device1, 7772));
		Thread t2 = new Thread(new RuntimeValidation("handleNumber", 2, m2, staticApp, device2, 7773));
		t1.start();
		t2.start();
		try {
			t1.join();
			t2.join();
		}	catch (Exception e) {e.printStackTrace();}
		
		int hitCount1 = overall_result1.size();
		int total1 = m1.getAllSourceLineNumbers().size();
		System.out.println("\nOverall break points hit: " + hitCount1 + "/" + total1 + ", " +
				new DecimalFormat("#.##").format(100*(double)hitCount1/(double)total1) + "%");
		for (String string: overall_result1) {
			System.out.println(string);
		}
		
		int hitCount2 = overall_result2.size();
		int total2 = m2.getAllSourceLineNumbers().size();
		System.out.println("\nOverall break points hit: " + hitCount2 + "/" + total2 + ", " +
				new DecimalFormat("#.##").format(100*(double)hitCount2/(double)total2) + "%");
		for (String string: overall_result2) {
			System.out.println(string);
		}

	}
	
}
