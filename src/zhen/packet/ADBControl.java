package zhen.packet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ADBControl {
	public static String unlockScreenCommand = "adb shell input keyevent 82";
	public static String clickPowerButtonCommand = "adb shell input keyevent KEYCODE_POWER";
	public static boolean enableConsoleOutput = true;
	public static boolean debug = true;
	private static Logger  logger = Utility.setupLogger(ADBControl.class);
	public static void sendSellCommand(String command){
		sendADBCommand("adb shell "+command);
	}
	
	public static void sendADBCommand(String command , int timeout_ms){
		if(debug) logger.info("command:"+command);
		Process task = null;
		InputStream stderr = null, stdout = null;
		try {
			task = Runtime.getRuntime().exec(command);
			if(timeout_ms>0){
				task.wait(timeout_ms);
			}
			stderr = task.getErrorStream();
			int count = stderr.available();
			if(count > 0){
				byte[] buffer = new byte[count];
				stderr.read(buffer);
				String reading = new String(buffer);
				if(enableConsoleOutput) System.out.println("sendADBCommand  stderr:"+ reading);
				if(debug) logger.info("stderr:"+reading);
				stderrBuffer.add(reading);
			}
			stdout = task.getInputStream();
			count = stdout.available();
			if(count > 0){
				byte[] buffer = new byte[count];
				stdout.read(buffer);
				String reading = new String(buffer);
				if(enableConsoleOutput) System.out.println("sendADBCommand  stdout:"+reading);
				if(debug) logger.info("stdout:"+reading);
				stdoutBuffer.add(reading);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static ArrayList<String> stdoutBuffer = new ArrayList<String>();
	private static ArrayList<String> stderrBuffer = new ArrayList<String>();
	
	public static void sendADBCommand(String command){
		sendADBCommand(command,0);
	}
	
	public static String getLatestStdoutMessage(){
		if(stdoutBuffer.size() <= 0) return null;
		return stdoutBuffer.get(stdoutBuffer.size()-1);
	}
	
	public static String getLatestStderrMessage(){
		if(stderrBuffer.size() <= 0) return null;
		return stderrBuffer.get(stderrBuffer.size()-1);
	}
	public static void clearStdoutBuffer(){
		stdoutBuffer.clear();
	}
	public static void clearStderrBuffer(){
		stderrBuffer.clear();
	}
}
