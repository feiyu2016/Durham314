package zhen.version1.framework;

import inputGeneration.StaticInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.Paths;
import zhen.framework.Configuration;

public class StaticInformation {
	
	protected Framework frame;
	public List<String> activityList;
	public String packageName;
	public StaticInformation(Framework frame){
		this.frame = frame;
	}
	
	public void init(Map<String, Object> attributes, boolean forceAllSteps){
		File apkFile = (File)attributes.get(Common.apkFile);
		StaticInfo.initAnalysis(apkFile, forceAllSteps);
		activityList = StaticInfo.getActivityNames(apkFile);
		packageName = StaticInfo.getPackageName(apkFile);
		
		attributes.put(Common.packageName, packageName);
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
