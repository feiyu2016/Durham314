package zhen.implementation;

import inputGeneration.StaticInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.Paths;
import zhen.framework.AbstractStaticInformation;
import zhen.framework.Configuration;
import zhen.framework.Framework;
import zhen.framework.AbstractExecuter.LogCatFeedBack;

public class WrapperStaticInformation extends AbstractStaticInformation {

	ArrayList<String> actList,clist,mList;
	String packageName;
	
	public WrapperStaticInformation(Framework frame) {
		super(frame); 
	}

	@Override
	public boolean init(Map<String,Object> attributes) {
		File inputFile = (File) attributes.get("apkfile");
		StaticInfo.initAnalysis(inputFile, false);
		
		packageName = StaticInfo.getPackageName(inputFile);
		actList = StaticInfo.getActivityNames(inputFile);
		clist = StaticInfo.getClassNames(inputFile);
		attributes.put("package", packageName);
		ArrayList<String> tmp = new ArrayList<String>();
		for(int i=0;i<actList.size();i++){
			tmp.add(packageName+"/"+actList.get(i));
		}
		attributes.put("actlist", tmp.toArray(new String[0]));
		
		if(attributes.containsKey("targets")){
			return true;
		}
		
		ArrayList<String> slist = new ArrayList<String>();
		ArrayList<String> inquery = (ArrayList<String>) attributes.get("pattern");
		if(inquery != null){
			for(String pattern : inquery){
				String[] parts_1 = pattern.split(":");
				String classPattern = parts_1[0].toLowerCase();
				String methodPattern = parts_1[1].toLowerCase();
				
				String targetClass = null;
				for(String className : clist){
					if(className.toLowerCase().contains(classPattern)){
						targetClass = className;
						break;
					}
				}
				System.out.println("targetClass: "+targetClass);
				if(targetClass == null) throw new AssertionError();
				mList = StaticInfo.getAllMethodSignatures(inputFile, targetClass);
				
				String targetMethod = null;
//				String targetMethod = "void Function_1(android.view.View)";
				for(String methodname: mList){
//					System.out.println(methodname  +"   ?   "+methodPattern);
					if(methodname.toLowerCase().contains(methodPattern)){
						targetMethod = methodname;
						break;
					}
				}
				if( targetMethod == null) throw new AssertionError();
				System.out.println(inquery+" => "+targetClass+":"+targetMethod);
				
				ArrayList<String> possibleCallSequencesList = StaticInfo.getPossibleCallSequences(targetClass, targetMethod);
				for(String sequence: possibleCallSequencesList){
					String[] sub_methodList = sequence.split(",");
					for(int i =1;i<sub_methodList.length;i++){
						String methodSig = sub_methodList[i];
						String parts[] = methodSig.split(":");
						String className = parts[0];
						String methodName = parts[1];
						ArrayList<String> foundHandler = StaticInfo.findEventHandlersThatMightDirectlyCallThisMethod(inputFile, className, methodName);
						slist.addAll(foundHandler);
					}
				}
			}
		}
		attributes.put("targets", slist.toArray(new String[0]));
		
		return true;
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
	}
	
	
	public static List<String> readInstrumentationFeedBack(String pid){
		ArrayList<String> result = new ArrayList<String>();
		Process pc;
		try {
			pc = Runtime.getRuntime().exec(Paths.adbPath + " logcat -v thread -d  -s "+Configuration.InstrumentationTag);
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			String line;
			while ((line = in.readLine())!=null){
				if(line.startsWith("-----")){
					continue;
				}else{ result.add(line); }
			}
				
			in.close();
		} catch (IOException e) { }		
		return result;
	}
	
	
	public static String getApplicationPid(String packageName){
		Process prc;
		try {
			prc = Runtime.getRuntime().exec(Paths.adbPath + " shell ps |grep " + packageName);
			BufferedReader in = new BufferedReader(new InputStreamReader(prc.getInputStream()));
			String line;
			while ((line = in.readLine())!=null) {
				if (!line.endsWith(packageName)) continue;
				String[] parts = line.split(" ");
				for (int i = 1; i < parts.length; i++) {
					if (parts[i].equals(""))	continue;
					return parts[i].trim();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "-1";
	}
}
