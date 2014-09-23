package zhen.test;

import inputGeneration.StaticInfo;

import java.io.File;
import java.util.ArrayList;

public class showMethods {

	public static void main(String[] args) {
		String path = "APK/signed_KitteyKittey.apk";
		File f= new File(path);
		StaticInfo.initAnalysis(f, false);
		ArrayList<String> alict = StaticInfo.getActivityNames(f);
		for(String actname : alict){
			ArrayList<String> mlist = StaticInfo.getAllMethodSignatures(f, actname);
			for(String m : mlist){
				System.out.println(m);
			}
		}
		
	}

}
