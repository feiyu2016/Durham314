package zhen.test;

import java.io.File;

import inputGeneration.StaticInfo;

public class testStaticInfo {
	public static void main(String[] args){
		String[] path = {
			"APK/Fast.apk"	,
			"APK/SimpleCallGraphTestApp.apk",
			"APK/SimpleDataFlowTest.apk",
			"APK/Facebook.apk",
		};
		
		testBasicAttributes(path[0]);
	}
	
	private static void testBasicAttributes(String path){
		File f = new File(path);
		StaticInfo.initAnalysis(f, false);
		TestUtility.printLineByLine("activity names",StaticInfo.getActivityNames(f));
	}
}
