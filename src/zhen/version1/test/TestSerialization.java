package zhen.version1.test;

import java.util.HashMap;
import java.util.Map;

import zhen.version1.Support.CommandLine;
import zhen.version1.framework.Common;
import zhen.version1.framework.Framework;

public class TestSerialization {

	public static void main(String[] args) {
		String[] name = { 
				"CalcA_soot.apk", 
		};

		
//		"signed_backupHelper.apk",
//		"signed_Butane.apk",
//		"signed_CalcA.apk",
//		"soot_CalcA.apk",
//		"signed_KitteyKittey.apk",
//		"CalcA_soot.apk",
		
		int index = 0;
		String path = "APK/"+name[index];
		
		//setup input parameters
		Map<String,Object> att = new HashMap<String,Object>();
		att.put(Common.originalApkPath, "Calc_A.apk");
		att.put(Common.apkPath	, path);
		
		Framework frame = new Framework(att);
		
		//once the step control is added, the program will wait for human instruction
		//before entering next operation loop
//		addExplorerStepControl(frame);
		//add a call back method which is called after the finish of traversal
//		addOnTraverseFinishCallBack(frame);
		CommandLine.executeCommand("adb install -r "+path);
//		frame.enableStdinMonitor(true);
		//NOTE: right now it does require apk installed on the device manually
		//		and please close the app if previous opened
		frame.setup();//initialize
		frame.start();//start experiment
		
		frame.rInfo.dumpeData("file1", true);
		System.out.println("dump successfully");

		Framework frame1 = new Framework(att);
		frame.rInfo.restoreData("file1");
		System.out.println("restore successfully");
	}

}
