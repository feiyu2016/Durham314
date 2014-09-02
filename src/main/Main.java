package main;

import inputGeneration.JDBStuff;
import inputGeneration.Layout;
import inputGeneration.ParseSmali;
import inputGeneration.SimpleTesting;
import inputGeneration.StaticInfo;
import inputGeneration.ViewNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

import org.w3c.dom.Node;

public class Main {

	public static ArrayList<Node> nodes = new ArrayList<Node>();
	


	public static void main(String[] args){
		//File file = new File("/home/wenhaoc/AppStorage/TestThreads.apk");

		File file = new File("/home/wenhaoc/AppStorage/PlayStoreApps/co.vine.android.apk");
		file = new File("/home/wenhaoc/AppStorage/SpringVersion1.5.apk");
		file = new File("/home/wenhaoc/AppStorage/Fast.apk");
		StaticInfo.initAnalysis(file, false);
		//file = new File("/home/wenhaoc/workspace/Test/bin/Test.apk");
		//StaticInfo.initAnalysis(file, false);
		try {
			SimpleTesting.clickAll(file);
		}	catch (Exception e) {e.printStackTrace();}
		

	}
	
	private static void showAllEHs(File file) {
		for (Layout layout: StaticInfo.getLayoutList(file)){
			for (ViewNode vN: layout.getAllViewNodes()) {
				ArrayList<String> stayingList = vN.getStayingEvents(StaticInfo.getMainActivityName(file));
				Map<String, ArrayList<String>> leavingMap = vN.getLeavingEvents(StaticInfo.getMainActivityName(file));
				System.out.println("view with id of " + vN.getID() + " has following STAYING event handler:");
				for (String sEH : stayingList)
					System.out.println("  " + sEH);
				System.out.println("view with id of " + vN.getID() + " has following LEAVING event handler:");
				for (Map.Entry<String, ArrayList<String>> entry: leavingMap.entrySet()) {
					System.out.print("  " + entry.getKey() + ". Targets:");
					for (String s: entry.getValue())
						System.out.print(" " + s);
					System.out.print("\n");
				}
			}
		}
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
