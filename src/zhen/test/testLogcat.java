package zhen.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import main.Paths;
import zhen.framework.AbstractExecuter.LogCatFeedBack;
import zhen.implementation.MonkeyExecuter;
import zhen.implementation.WrapperStaticInformation;
import inputGeneration.StaticInfo;

public class testLogcat {

	public static void main(String[] args) throws InterruptedException, IOException {
		String path = "APK/testApp1.apk";
		String name = StaticInfo.getPackageName(new File(path));
		
//		String pid = WrapperStaticInformation.getApplicationPid(name);
		Scanner sc = new Scanner(System.in);
		while(true){
			String reading = sc.nextLine();
			
			if(reading.equals("0")) break;
			if(reading.equals("1")){
				List<LogCatFeedBack> logcatReading = MonkeyExecuter.readApplicationLogcat(name);
				System.out.println("reading:");
				for(LogCatFeedBack cat :logcatReading){
					System.out.println(cat);
				}
			}else if(reading.equals("2")){
				System.out.println("cleariung");
				Runtime.getRuntime().exec(Paths.adbPath + " logcat -c").waitFor();
			}
		}
		
	}

}
