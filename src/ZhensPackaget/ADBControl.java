package ZhensPackaget;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ADBControl {
	public static String unlockScreenCommand = "adb shell input keyevent 82";
	public static String clickPowerButtonCommand = "adb shell input keyevent KEYCODE_POWER";
	
	public static void sendSellCommand(String command){
		sendADBCommand("adb shell "+command);
	}
	
	public static void sendADBCommand(String command , int timeout_ms){
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
				System.out.println("sendADBCommand  stderr:"+new String(buffer));
			}
			stdout = task.getInputStream();
			count = stdout.available();
			if(count > 0){
				byte[] buffer = new byte[count];
				stdout.read(buffer);
				System.out.println("sendADBCommand  stdout:"+new String(buffer));
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			try{
				if(stderr!=null)stderr.close();
				if(stdout!=null)stdout.close();
			}catch(IOException e){ }
			if(task!=null) task.destroy();
		}
		
	}
	
	public static void sendADBCommand(String command){
		sendADBCommand(command,0);
	}
}
