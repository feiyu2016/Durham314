package zhen.framework;

public class Configuration {
	
	public static String androidSDKPath = "/home/zhenxu/tools/adt-bundle-linux-x86_64-20140702/";
	public static String appDataDir = "generated/";
	
	public static String androidToolPath = androidSDKPath+"sdk/tools/";
	public static String androidPlatformToolPath = androidSDKPath+"/sdk/platform-tools/";
	public static String adbPath = androidPlatformToolPath+"adb";
	public static String androidJarPath = "libs/android.jar";
	public static String apktoolPath = "libs/apktool.jar";
	
	public static String InstrumentationTag = "System.out";//System.out
}
