package zhen.version1.framework;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException; 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.Paths;
import zhen.version1.Support.Utility;
import zhen.version1.component.*;

public class Executer {
	protected Framework frame;
	public static final String TAG = "Executer";
	public static final String UP = "MonkeyDevice.UP";
	public static final String DOWN = "MonkeyDevice.DOWN";
	public static final String DOWN_AND_UP = "MonkeyDevice.DOWN_AND_UP";
	public static boolean DEBUG = false;
	
	private Process monkeyProcess = null;
	private BufferedOutputStream ostream = null;
	private BufferedInputStream estream = null;
	private BufferedInputStream istream = null;
	private List<Event> sequence = new ArrayList<Event>();
	
	public Executer(Framework frame){
		this.frame = frame;
	}
	public void onBack(){
		this.applyEvent(Event.getOnBackEvent());
	}
	public void applyEvent(Event event){
		sequence.add(event);
		if(DEBUG){ Utility.log(TAG, event.toString()); }
		Utility.clearLogcat();
		int type = event.getEventType();
		switch(type){
		case Event.iLAUNCH:{
			String packageName = (String) event.getValue(Common.event_att_packname);
			String actName = (String) event.getValue(Common.event_att_actname);
//			this.startActivity(packageName, actName);
			try {
				Runtime.getRuntime().exec(Paths.adbPath + " shell am start -n " + packageName + "/" + actName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}break;
		case Event.iRESTART:{
			//TODO
		}break;
		case Event.iREINSTALL:{
			//TODO
		}break;
		case Event.iPRESS:{
			String keycode = (String)event.getValue(Common.event_att_keycode);
			this.press(keycode);
		}break;
		case Event.iONCLICK:{
			String x = event.getValue(Common.event_att_click_x).toString();
			String y = event.getValue(Common.event_att_click_y).toString();
			this.click(x, y);
		}break;
		case Event.iUPDATE:{
			//TODO -- should do nothing
		}break;
		case Event.iEMPTY:{
			//TODO -- should do nothing
		}break;
		case Event.iUNDEFINED:
		default: throw new IllegalArgumentException();
		}
		
		try { Thread.sleep(Event.getNeededSleepDuration(type));
		} catch (InterruptedException e) { }
	}
	public void applyEventSequence(Event... events){
		for(Event singleEvnet: events){
			this.applyEvent(singleEvnet);
			this.frame.rInfo.checkVisibleWindowAndCloseKeyBoard();
		}
	}
	public void registerEvent(Event event){
		sequence.add(event);
	}
	public List<Event> getEventList(){
		return this.sequence;
	}
	public Event getLastEventApplied(){
		//TODO
		return null;
	}
	public boolean init(Map<String, Object> attributes){
		if (DEBUG) Utility.log(TAG, "initialization starts");
		try {
			monkeyProcess = Runtime.getRuntime().exec(Configuration.MonkeyLocation);
			ostream = new BufferedOutputStream(monkeyProcess.getOutputStream());
			estream = new BufferedInputStream(monkeyProcess.getErrorStream());
			istream = new BufferedInputStream(monkeyProcess.getInputStream());
			sleepForMonekyReady();
			importLibrary();
			connectDevice();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			Utility.readString(estream);
			Utility.readString(istream);
			if (DEBUG) Utility.log(TAG, "initialization finishes");
		} catch (IOException e) {
			e.printStackTrace();
			try {
				if (ostream != null)
					ostream.close();
				if (estream != null)
					estream.close();
				if (istream != null)
					istream.close();
			} catch (IOException e1) {
			}
			if (monkeyProcess != null)
				monkeyProcess.destroy();
			if (DEBUG) Utility.log(TAG, "initialization fails");
			return false;
		} 
		return true;
	}
	public void terminate(){
		if (monkeyProcess != null) {
			try {
				ostream.close();
				istream.close();
				estream.close();
			} catch (IOException e) {
			}
			monkeyProcess.destroy();
		}
		if (DEBUG) Utility.log(TAG, "termination finished");
	}


	/** Monkey method **/
	
	public void click(String x, String y) {
		touch(x, y, DOWN_AND_UP);
	}
	public void touch(String x, String y, String type) {
		String toWrite = "device.touch(" + x + "," + y + "," + type + ")\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void type(String msg) {
		String toWrite = "device.type('" + msg + "')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void wakeDeviceup() {
		sendCommand("device.wake()\n");
	}
	public void press(String keyCode) {
		// http://developer.android.com/reference/android/view/KeyEvent.html
		// all string name begins with "KEYCODE_"
		sendCommand("device.press('" + keyCode + "')\n");
	}
	public void press(int keyCode) {
		sendCommand("device.press('" + keyCode + "')\n");
	}
	public void sleep(int sec) {
		String toWrite = "MonkeyRunner.sleep(" + sec + ")\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void install(String apkPath) {
		String toWrite = "device.installPackage('" + apkPath + "')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//	public void startActivity(String packageName, String actName) {
//		String toWrite = "runComponent = " + packageName + " + '/.' + "
//				+ actName + "\n"
//				+ "device.startActivity(component=runComponent)\n";
//		try {
//			ostream.write(toWrite.getBytes());
//			ostream.flush();
//			sleepForMonekyReady();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
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
		String toWrite = "device = MonkeyRunner.waitForConnection()\n";
		if(DEBUG)Utility.log(TAG, "Connecting");
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(DEBUG)Utility.log(TAG, "Connection Successful");
	}
	private boolean isNotReadyForNextInput(String msg) {
		if (msg == null) {
			return true;
		} else {
			return !msg.trim().endsWith(">>>");
		}
	}
	private void sleepForMonekyReady() {
		String output = null;
		do {
			output = getMonkeyOutput();
			if (DEBUG && output != null) {
				output.replace("\n", "\n\t");
				if (DEBUG)
					Utility.log(TAG, "stdout " + output);
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			String error = getMonkeyError();
			if (error != null) {
				if (DEBUG && output != null) {
					output.replace("\n", "\n\t");
					if (DEBUG)
						Utility.log(TAG, "stderr " + output);
				}
				break;
			}
		} while (isNotReadyForNextInput(output));
	}
	private String getMonkeyOutput() {
		try {
			int count = istream.available();
			if (count > 0) {
				byte[] reading = new byte[count];
				istream.read(reading);
				return new String(reading);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private String getMonkeyError() {
		try {
			int count = estream.available();
			if (count > 0) {
				byte[] reading = new byte[count];
				estream.read(reading);
				return new String(reading);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private void sendCommand(String command) {
		if (DEBUG) Utility.log(TAG, command);
		if (!command.endsWith("\n")) {
			command = command + "\n";
		}
		try {
			ostream.write(command.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
