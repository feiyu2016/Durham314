package zhen.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import main.Paths;
import zhen.framework.AbstractExecuter.LogCatFeedBack;
import zhen.implementation.MonkeyExecuter;
import zhen.implementation.WrapperStaticInformation;
import inputGeneration.RunTimeInfo;
import inputGeneration.StaticInfo;

public class testLogcat {

	public static void main(String[] args) throws Exception {
		String path = "APK/signed_backupHelper.apk";
		File f = new File(path);
		String name = StaticInfo.getPackageName(f);
		RunTimeInfo.installApp(f);
		RunTimeInfo.startApp(f);
		
//		String pid = WrapperStaticInformation.getApplicationPid(name);
		Scanner sc = new Scanner(System.in);
		while(true){
			String reading = sc.nextLine();
			
			if(reading.equals("0")) break;
			if(reading.equals("1")){
				List<String> logcatReading = MonkeyExecuter.readApplicationLogcat(name);
				System.out.println("reading:");
				for(String cat :logcatReading){
					System.out.println(cat);
				}
			}else if(reading.equals("2")){
				System.out.println("cleariung");
				Runtime.getRuntime().exec(Paths.adbPath + " logcat -c").waitFor();
			}
		}
		
	}

}
