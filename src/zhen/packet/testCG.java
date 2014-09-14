package zhen.packet;

import inputGeneration.StaticInfo;
import inputGeneration.StaticLayout;
import inputGeneration.StaticViewNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class testCG {

	public static void main(String[] args){
		String path = "APK/SimpleCallGraphTestApp.apk";
		testSubCompoenent(path);
	}
	
	private static void testSubCompoenent(String path){
		long time1 = System.currentTimeMillis();
		
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
		
		
		
		String targetMethod = "";
//		String targetMethod = "void Function_1(android.view.View)";
		for(String methodname: mList){
			if(methodname.toLowerCase().contains("function_2")){
				targetMethod = methodname;
				break;
			}
		}
		System.out.println("targetMethod:"+targetMethod);
		
		ArrayList<String> outCallTargetsList = StaticInfo.getOutCallTargets(targetClass, targetMethod);
		ArrayList<String> inCallTargetsList = StaticInfo.getInCallSources(targetClass, targetMethod);
		ArrayList<String> possibleCallSequencesList = StaticInfo.getPossibleCallSequences(targetClass, targetMethod);
		ArrayList<String> allPossibleIncomingCallersList = StaticInfo.getAllPossibleIncomingCallers(targetClass, targetMethod);
		
		System.out.println("getOutCallTargets");printLineByLine(outCallTargetsList);
		System.out.println("getInCallSources");printLineByLine(inCallTargetsList);
		System.out.println("getPossibleCallSequences");printLineByLine(possibleCallSequencesList);
		System.out.println("getAllPossibleIncomingCallers");printLineByLine(allPossibleIncomingCallersList);
		

		ArrayList<String> classList = StaticInfo.getClassNames(f);		
		
		System.out.println("Until now: "+String.format("%.3f", (System.currentTimeMillis()-time1)/1000.0));
		System.out.println("Using getInCallSources");
		for(String methodSig: inCallTargetsList){
			ArrayList<String> eventHanlder = new ArrayList<String>();
			boolean isOncreate = false;
			for(String className: classList){
//				if(StaticInfo.isOnCreate(f, className, methodSig)){
//					isOncreate = true;
//					break;
//				}
				ArrayList<String> tmp = StaticInfo.findEventHandlersThatMightDirectlyCallThisMethod(f, className, methodSig);
				eventHanlder.addAll(tmp);
			}
			
			if(isOncreate){
				System.out.println("isOncreate");
			}else{
				System.out.println("Possible event handler:");
				System.out.println(eventHanlder);
			}
		}
		
		long total = System.currentTimeMillis();
		System.out.println("Total time: "+String.format("%.3f", (total-time1)/1000.0));
	}
	
//	private boolean isHanlerClass(String className){
//		
//		
//		return false;
//	}
	static void printLineByLine(List li){
		if(li == null || li.isEmpty()){
			System.out.println("Empty");
			return;
		}
		
		for(Object o:li){
			System.out.println(o);
		}
	}
}
