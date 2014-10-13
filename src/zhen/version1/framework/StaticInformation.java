package zhen.version1.framework;
 

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import staticAnalysis.StaticInfo;
import staticFamily.StaticApp;
import staticFamily.StaticClass;
import zhen.version1.Support.Utility;
import main.Paths; 

/**
 * A wrapper class for the StaticInfo in case any further change 
 * @author zhenxu
 *
 */
public class StaticInformation {
	public static boolean DEBUG = true;
	public static String TAG = "StaticInformation";
	protected Framework frame;
	public List<String> activityList;
	public String packageName;
	public StaticInformation(Framework frame){
		this.frame = frame;
	}
	
	public void init(Map<String, Object> attributes, boolean forceAllSteps){
		File apkFile = (File)attributes.get(Common.apkFile);
		
		StaticApp app = new StaticApp(apkFile);
		app = StaticInfo.initAnalysis(app, false);
		
		List<StaticClass> classList =  app.getActivityList(); 
		
		activityList = new ArrayList<String>();
		for(StaticClass clazz : classList){
			activityList.add(clazz.getName());
		}
		
		packageName = app.getPackageName();
		
		attributes.put(Common.packageName, packageName);
		if(DEBUG)Utility.log(TAG,"packageName, "+packageName);
	}
	
	public void terminate(){
		
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
