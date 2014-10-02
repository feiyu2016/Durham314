package zhen.version1.Support;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.Paths;
import zhen.framework.Configuration;

public class Utility {
	public static boolean DEBUG = true;
	public static String TAG = "Utility";
	public static List<String> readInstrumentationFeedBack(){
		ArrayList<String> result = new ArrayList<String>();
		/**
		 * Situation that the process never terminated happened.
		 */
		try{
			final Process pc = Runtime.getRuntime().exec(Paths.adbPath + " logcat -v thread -d  -s "+Configuration.InstrumentationTag);
			InputStream in = pc.getInputStream();
			long point1 = System.currentTimeMillis();
			StringBuilder sb = new StringBuilder();
			Thread.sleep(20);
			while(true){
				int count = in.available();
				if(count < 0) break;
				byte[] buf = new byte[count];
				in.read(buf);
				sb.append(new String(buf));
				long point2 = System.currentTimeMillis();
				if(point2 - point1 > 100) break;
			}
			pc.destroy();
			String tmp = sb.toString();
			if(tmp.trim().equals("")) return result;
			String[] parts = tmp.split("\n");
			for(String part: parts){
				if(part.contains("METHOD_STARTING")){
					String methodName = part.split("METHOD_STARTING,<")[1].replace(">", "");
					result.add(methodName);
				}
			}
		}catch(Exception e){}
		return result;
	}
	
	public static void clearLogcat(){
		try {
			Runtime.getRuntime().exec(Configuration.adbPath + " logcat -c").waitFor();
		} catch (InterruptedException | IOException e) { 
			e.printStackTrace();
		}
	}
	
	public static String getUid(String appName){
		String command = "adb shell dumpsys "+appName+" | grep userId=";
		try {
			Process exec = Runtime.getRuntime().exec(command);
			exec.waitFor();
			InputStream in = exec.getInputStream();
			int count = in.available();
			byte[] buf = new byte[count];
			return new String(buf);
		} catch (IOException e) { 
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readString(InputStream in) {
		int count;
		try {
			count = in.available();
			if (count > 0) {
				byte[] buffer = new byte[count];
				in.read(buffer);
				return new String(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int findFirstFalse(boolean[] arr){
		for(int i=0;i<arr.length;i++){
			if(arr[i] == false) return i;
		}
		return -1;
	}
	
	public static Logger setupLogger(Class clazz){
		Logger logger = Logger.getLogger(clazz.getName());
		FileHandler fhandler;
		ConsoleHandler chandler;
		try {
			for(Handler handle : logger.getHandlers()){
				handle.setLevel(Level.FINER);
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logger;
	}
	
	public static void removeFileUnderFolder(String folderName){
		File folder = new File(folderName);
		String[] names = folder.list();
		for(String name:names){
			File afile = new File(folderName+"/"+name);
			afile.delete();
		}
	}
	
	public static void log(String msg){
		System.out.println(msg);
	}
	
	public static void info(String msg){
		System.out.println(msg);
	}
	
	public static void info(String tag, Object input ){
		int tagSize = 40, msgSize = 200;;
		if(tag.length() > tagSize){
			tag = (String) tag.subSequence(0, tagSize);
		}
		String part1 = String.format("%-"+tagSize+"s", tag);
		String[] part2 = String.format("%-"+msgSize+"s", (input==null?"null":input.toString())).split("\n");
		for(String part: part2){
			System.out.println(part1+"  "+part);
		}
	}
	
	public static void log(String tag, Object input){
		int tagSize = 40, msgSize = 200;;
		if(tag.length() > tagSize){
			tag = (String) tag.subSequence(0, tagSize);
		}
		String part1 = String.format("%-"+tagSize+"s", tag);
		String[] part2 = String.format("%-"+msgSize+"s", (input==null?"null":input.toString())).split("\n");
		for(String part: part2){
			System.out.println(part1+"  "+part);
		}
	}
}
