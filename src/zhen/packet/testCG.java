package zhen.packet;

import inputGeneration.StaticInfo;

import java.io.File;
import java.util.ArrayList;

public class testCG {

	public static void main(String[] args){
		String path = "APK/SimpleCallGraphTestApp.apk";
		testSubCompoenent(path);
	}
	
	private static void testSubCompoenent(String path){
		File f =new File(path);
		inputGeneration.StaticInfo.initAnalysis(f, false);
		analysisTools.Soot.generateAPKData(f);
		ArrayList<String> list = StaticInfo.getActivityNames(f);
		System.out.println(list);
//		ArrayList<String> clist = StaticInfo.getClassNames(f);
//		System.out.println(clist);
		
		String targetClass = "com.example.simplecallgraphtestapp.SimpleTarget";
		ArrayList<String> mList = StaticInfo.getAllMethodSignatures(f, targetClass);
		System.out.println(mList);
		
		String targetMethod = "void Function_1(android.view.View)";
		ArrayList<String> callerList = StaticInfo.getAllPossibleIncomingCallers(targetClass, targetMethod);
	
//		for(String caller : callerList){
//			
//		}
	}
	
//	private boolean isHanlerClass(String className){
//		
//		
//		return false;
//	}
}
