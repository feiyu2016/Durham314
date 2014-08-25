package main;

import inputGeneration.JDBStuff;
import inputGeneration.SimpleTesting;
import inputGeneration.StaticInfo;

import java.io.File;

public class Main {

	public static void main(String[] args) {
		
		//File file = new File("/home/wenhaoc/AppStorage/Fast.apk");
		//File file = new File("/home/wenhaoc/workspace/Test/bin/Test.apk");
		File file = new File("/home/zhenxu/workspace/ExperimentalApp/bin/ExperimentalApp.apk");
		initAnalysis(file);
		//testJDB(file);
		
		try {
			SimpleTesting.clickAll(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void initAnalysis(File file) {
		analysisTools.ApkTool.extractAPK(file);
		analysisTools.Soot.generateAPKData(file);
		StaticInfo.process_Intents_And_setContentView(file);
	}
	
	
	private static void testJDB(File file) {
		JDBStuff jdb = new JDBStuff();
		try {
			jdb.initJDB(file);
			jdb.setMonitorStatus(true);
			jdb.setBreakPointLine("com.example.helloworld.MainActivity", 321);
			while (true) {}
		} catch (Exception e) {	e.printStackTrace();}
	}
}
