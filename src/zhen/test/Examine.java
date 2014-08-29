package zhen.test;

import zhen.packet.StaticGuidedAlgoirthm;
import inputGeneration.StaticInfo;

public class Examine {

	public static void main(String[] args) {
		String path = "APK/Fast.apk";
		StaticGuidedAlgoirthm test = new StaticGuidedAlgoirthm(path);
		test.enableDynamicInit = false;
		test.enableStaticInfo = true;
		test.enableDynamicInit = false;
		test.enablePostRun = false;
		test.enablePostRun = false;
		test.enablePreRun = true;
		
		test.start();
	}

}
