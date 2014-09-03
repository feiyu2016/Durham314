package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import main.Paths;

public class RunTimeInfo {

	
	public static void installApp(File file) throws Exception {
		Process pc = Runtime.getRuntime().exec(Paths.adbPath + " install " + file.getAbsolutePath());
		printStreams(pc);
	}
	
	public static void startApp(File file) throws Exception {
		String packageName = StaticInfo.getPackageName(file);
		String mainActivity = StaticInfo.getMainActivityName(file);
		Process pc = Runtime.getRuntime().exec(Paths.adbPath + " shell am start -n " + packageName + "/" + mainActivity);
		pc.waitFor();
		printStreams(pc);
	}
	
	public static void startActivity(File file, String activityName) throws Exception {
		String packageName = StaticInfo.getPackageName(file);
		Process pc = Runtime.getRuntime().exec(Paths.adbPath + " shell am start -n " + packageName + "/" + activityName);
		printStreams(pc);
	}
	
	public static void exitApp(File file) throws Exception {
		String pID = getPID(file);
		Process pc = Runtime.getRuntime().exec(Paths.adbPath + " shell kill " + pID);
		printStreams(pc);
	}
	
	public static void deleteApp(File file) throws Exception {
		String packageName = StaticInfo.getPackageName(file);
		Process pc = Runtime.getRuntime().exec(Paths.adbPath + " uninstall " + packageName);
		printStreams(pc);
	}
	
	public static String getPID(File file) throws Exception {
		String packageName = StaticInfo.getPackageName(file);
		Process prc = Runtime.getRuntime().exec(Paths.adbPath + " shell ps |grep " + packageName);
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
		return "-1";
	}
	
	private static void printStreams(Process pc) throws Exception{
		BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
		BufferedReader in_err = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
		String line;
		while ((line = in.readLine())!=null)
			System.out.println(line);
		while ((line = in_err.readLine())!=null)
			System.out.println(line);
	}
	
	public static String getCurrentUIStatus() {
		return "";
	}
}
