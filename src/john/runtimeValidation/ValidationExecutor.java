package john.runtimeValidation;

import java.io.BufferedOutputStream;
import java.io.IOException;

import zhen.version1.framework.Configuration;

public class ValidationExecutor {
	public static final String DOWN_AND_UP = "MonkeyDevice.DOWN_AND_UP";
	
	private Process monkeyProcess = null;
	private BufferedOutputStream ostream = null;
	private String serial = null;
	
	public ValidationExecutor(String serial) {
		this.serial = serial;
	}
	
	public void init()
	{
		try {
			monkeyProcess = Runtime.getRuntime().exec(Configuration.MonkeyLocation);
			ostream = new BufferedOutputStream(monkeyProcess.getOutputStream());
			
			importLibrary();
			connectDevice();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void touch(String x, String y) {
		String toWrite = "device.touch(" + x + "," + y + "," + DOWN_AND_UP + ")\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void exit() {
		try {
			ostream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		monkeyProcess.destroy();
	}
	
	private void importLibrary() {
		String toWrite = "from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void connectDevice() {
		String toWrite = "device = MonkeyRunner.waitForConnection(60, '"+serial+"')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sleepForMonekyReady() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}
}
