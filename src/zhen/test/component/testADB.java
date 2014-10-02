package zhen.test.component;

import java.io.File;

import zhen.packet.ADBControl;
import inputGeneration.StaticInfo;

public class testADB {

	public static void main(String[] args) {
		String path = "APK/Fast.apk";
		File f = new File(path);
		String packname = StaticInfo.getPackageName(f);
		String actname = StaticInfo.getMainActivityName(f);
		
		int exit = ADBControl.executeADBCommand("adb shell am start -n "+packname+"/."+actname);
		System.out.println("asd:"+exit);
	}

}
