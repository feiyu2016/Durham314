package main;

import inputGeneration.JDBStuff;

import inputGeneration.ParseSmali;
import inputGeneration.SimpleTesting;

import inputGeneration.StaticInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.w3c.dom.Node;

public class Main {

	public static ArrayList<Node> nodes = new ArrayList<Node>();
	


	public static void main(String[] args) throws Exception {
		String apkFilePath = "APK/Fast.apk";
		
		File file = new File(apkFilePath);

//		initAnalysis(file);
//		StaticInfo.getActivityNames(file);
		setBreakPointsAtAllLines(file);

//		ZhensTest.clickAll(file);

	}
	
	
	private static void initAnalysis(File file) {
		analysisTools.ApkTool.extractAPK(file);
		analysisTools.Soot.generateAPKData(file);
		StaticInfo.process_Intents_And_setContentView(file);
	}
	
	private static void setBreakPointsAtAllLines(File file) {
		ArrayList<String> al = new ParseSmali().parseLines(file);
		JDBStuff jdb = new JDBStuff();
		
		try {
			jdb.initJDB(file);
			jdb.setMonitorStatus(true);
			jdb.setBreakPointsAllLines(al);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	private static void testOnCreateSig() throws Exception{
		int count = 0;
		File[] files = new File("/home/wenhaoc/AppStorage/PlayStoreApps/").listFiles();
		for (File file: files) {
			System.out.println(file.getName());
			ArrayList<String> aa = StaticInfo.getActivityNames(file);
			for (String a: aa) {
				System.out.println(" " + a);
				File classFolder = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/" + a);
				if (!classFolder.exists())	continue;
				
				BufferedReader in = new BufferedReader(new FileReader(classFolder + "/ClassInfo.csv"));
				String line;
				while ((line = in.readLine())!=null) {
					if (!line.startsWith("Method,"))	continue;
					String[] s = line.split(",");
					if (s[1].equals("onCreate"))
						if (!s[5].equals("void onCreate(android.os.Bundle)")) {
							count++;
							System.out.println("--->  " + s[5]);
						}
				}
				in.close();
			}
		}
		System.out.println(count);
	}
}
