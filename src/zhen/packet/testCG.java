package zhen.packet;

import inputGeneration.StaticInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class testCG {

	public static void main(String[] args){
		String path = "APK/SimpleCallGraphTestApp.apk";
		testSubCompoenent(path);
	}
	
	private static void testSubCompoenent(String path){
		File f =new File(path);
		inputGeneration.StaticInfo.initAnalysis(f, false);
//		analysisTools.Soot.generateAPKData(f);
		System.out.println("Act names:");
		ArrayList<String> list = StaticInfo.getActivityNames(f);
		printLineByLine(list);
		
//		System.out.println("Class names:");
		ArrayList<String> clist = StaticInfo.getClassNames(f);
//		printLineByLine(clist); -- correct
		
		String targetClass = "";
//		String targetClass = "com.example.simplecallgraphtestapp.SimpleTarget";
		for(String className : clist){
			if(className.toLowerCase().contains("simpletarget")){
				targetClass = className;
				break;
			}
		}
		System.out.println("target class:" +targetClass );
		System.out.println("Method names:");
		ArrayList<String> mList = StaticInfo.getAllMethodSignatures(f, targetClass);
		printLineByLine(mList);
//		
//		String targetMethod = "void Function_1(android.view.View)";
//		ArrayList<String> callerList = StaticInfo.getAllPossibleIncomingCallers(targetClass, targetMethod);
	
//		for(String caller : callerList){
//			
//		}
	}
	
//	private boolean isHanlerClass(String className){
//		
//		
//		return false;
//	}
	static void printLineByLine(List li){
		if(li == null || li.isEmpty()){
			System.out.println("Empty");
		}
		
		for(Object o:li){
			System.out.println(o.toString());
		}
	}
}
