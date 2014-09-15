package zhen.test;

import java.util.ArrayList;
import java.util.Map;

import zhen.packet.IDOrientedAlgorithm;
import zhen.packet.StaticGuidedAlgoirthm;
import inputGeneration.StaticInfo;

public class Examine {
	static String[] path = {
		"APK/Fast.apk",
		"APK/SimpleCallGraphTestApp.apk"
	};
	static int i = 1;
	static String target = path[i];
	
	public static void main(String[] args) {
//		normaltest();
		testIdOriented();
	}
	
	private static void normaltest(){
		StaticGuidedAlgoirthm test = new StaticGuidedAlgoirthm(target);
		test.setNormalStartUp();
		
		test.enableJDB = false;
		test.enableAPKTool = false;
		test.enableStaticAnalysis = false;
		test.showStaticInfo = true;
		test.enableUsePartialSet = true;
		test.enableUseFullSet = true;
		test.enableStaticHandlerInfo = false;
		test.enablePathOptimization = false;

		
		test.start();
	}
	
	private static void testIdOriented(){
		String pattern = "function_4";
		ArrayList<String> arr =testCG.testSubCompoenent(target, pattern);
		String first = arr.get(1);
		String sig = arr.get(0);
		
		//[com.example.simplecallgraphtestapp.MainActivity,activity_main,trigger2,android:onClick]
		
		String[] parts = first.split(",");
		ArrayList<String> tmp =new ArrayList<String>();
		tmp.add(parts[3]);
		IDOrientedAlgorithm test = new IDOrientedAlgorithm(target,parts[0],parts[2],tmp);
		test.setNormalStartUp();
		test.enableJDB = false;
		test.enableAPKTool = false;
		test.enableStaticAnalysis = false;
		test.showStaticInfo = true;
		test.enableUsePartialSet = true;
		test.enableUseFullSet = true;
		test.enableStaticHandlerInfo = false;
		test.enablePathOptimization = false;
		
		test.start();
	}

}
