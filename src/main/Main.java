package main;

import inputGeneration.JDBStuff;
import inputGeneration.StaticInfo;

import java.io.File;

import analysisTools.Soot;

public class Main {

	public static void main(String[] args) {
		
		//File file = new File("/home/wenhaoc/AppStorage/Fast.apk");
		//File file = new File("/home/wenhaoc/workspace/Test/bin/Test.apk");
		File file = new File("/home/wenhaoc/Downloads/HelloWorld.apk");
<<<<<<< HEAD
		initAnalysis(file);
		//testJDB(file);
=======
		//initAnalysis(file);
		//zhiming;
		JDBStuff jdb = new JDBStuff();
		try {
			jdb.initJDB(file);
			jdb.setMonitorStatus(true);
			// hahaha
			jdb.setBreakPointLine("com.example.helloworld.MainActivity", 321);
			while (true) {}
		} catch (Exception e) {	e.printStackTrace();}
>>>>>>> c76fb44642d8d8c2018561b9f4e75d4db30594c2
	}
	
	
	private static void initAnalysis(File file) {
		analysisTools.ApkTool.extractAPK(file);
		analysisTools.Soot.generateAPKData(file);
		//StaticInfo.process_Intents_And_setContentView(file);
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
