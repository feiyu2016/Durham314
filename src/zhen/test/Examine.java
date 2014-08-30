package zhen.test;

import zhen.packet.StaticGuidedAlgoirthm;
import inputGeneration.StaticInfo;

public class Examine {
	static String path = "APK/Fast.apk";
	public static void main(String[] args) {
		normaltest();
	}
	
	private static void normaltest(){
		
		StaticGuidedAlgoirthm test = new StaticGuidedAlgoirthm(path);
		test.setNormalStartUp();
		test.showStaticInfo = true;
		test.enableUsePartialSet = true;
		test.enableUseFullSet = true;
		test.enableStaticHandlerInfo = false;
		test.enablePathOptimization = false;
		
		test.start();
	}

}
