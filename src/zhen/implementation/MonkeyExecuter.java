package zhen.implementation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import android.view.KeyEvent;
import zhen.framework.AbstractExecuter;
import zhen.framework.Event;
import zhen.framework.Framework;
import zhen.packet.Utility;
import main.Paths;

/**
 * 	This class wrap MonkeyRunner inside. The location of the program 
 * needs to be specified. A process for monkey runner will be created.
 * 
 * 	The main process knows if the command Jython script finishes 
 * execution when ">>>" or some messages occur from stdin or stderr. 
 * 
 * 	The call sequence for an interacting session:
 * 1. startInteractiveModel
 * 2. Various Operations
 * 3. stopInteractiveModle
 * 
 * @author zhenxu
 *
 */
public class MonkeyExecuter extends AbstractExecuter{
	public MonkeyExecuter(Framework frame) {
		super(frame);
	}

	public static String PROGRAM_LOCATION 
		= Paths.androidToolPath+"/monkeyrunner";
	public static final String UP = "MonkeyDevice.UP";
	public static final String DOWN = "MonkeyDevice.DOWN";
	public static final String DOWN_AND_UP = "MonkeyDevice.DOWN_AND_UP";
	public static boolean DEBUG = true;
	public boolean enableTouchOffset = false;
	private int offset_x = 50, offset_y = 50;
	private static String TAG = "MonkeyExecuter";
	
	public void setProgramLocation(String loc){
		PROGRAM_LOCATION = loc; 
	}
	
	private Process monkeyProcess = null;
	private BufferedOutputStream ostream = null;
	private BufferedInputStream estream = null;
	private BufferedInputStream istream = null;
	
	@Override
	public boolean execute(Event... inputs) {
		for(Event input: inputs){
			switch(input.type){
			case Event.SYSTEM:{
				
			}return true;
			case Event.KEY:{
				
			}return true;
			case Event.MOTION:{
				Map<String,String> attributes = input.attribute;
				String type = attributes.get("detail");
				if(type == null) return false;
				if(type.equals("swipe")){
					String start_x = attributes.get("start_x");
					String start_y = attributes.get("start_y");
					String end_x = attributes.get("end_x");
					String end_y = attributes.get("end_y");
					
					//TODO to finish
				}else if(type.equals("click")){
					String x = attributes.get("x");
					String y = attributes.get("y");
					this.click(x, y);
				}else{
					//TODO
				}
			}return false;
			default:{
				if(DEBUG)Utility.log(TAG,"MonkeyExecuter: unidentified event type, "+input.type);	
			}return false;
			}
		}
		return true;
	}

	@Override public void onLaunchApplication() {}
	@Override public void onLaunchApplicationFinish() {}
	@Override public void onQuitApplication() {}

	@Override
	public boolean init() {
		if(this.DEBUG) Utility.log(TAG,"initialization starts");
		try {
			monkeyProcess = Runtime.getRuntime().exec(PROGRAM_LOCATION);
			ostream = new BufferedOutputStream(monkeyProcess.getOutputStream());
			estream = new BufferedInputStream(monkeyProcess.getErrorStream());
			istream = new BufferedInputStream(monkeyProcess.getInputStream());
			sleepForMonekyReady();
			importLibrary();
			connectDevice();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }
			readString(estream);
			readString(istream);
			if(this.DEBUG) Utility.log(TAG,"initialization finishes");
		} catch (IOException e) {
			e.printStackTrace();
			try {
				if(ostream!=null) ostream.close();
				if(estream!=null) estream.close();
				if(istream!=null) istream.close();	
			} catch (IOException e1) { }
			if(monkeyProcess != null) monkeyProcess.destroy();
			if(this.DEBUG) Utility.log(TAG,"initialization fails");
			return false;
		}
		return true;
	}

	@Override
	public void terminate() {
		if(monkeyProcess!=null){
			try { 
				ostream.close();
				istream.close();
				estream.close();
			} catch (IOException e) {  }
			monkeyProcess.destroy(); 
		}
		if(DEBUG) Utility.log(TAG,"termination finished");
	}

	
	
	
	
	
	
	public void click(String x, String y){
		touch(x,y,DOWN_AND_UP);
	}
	
	public void touch(String x, String y, String type){
		if(enableTouchOffset){
			x = (Double.parseDouble(x)+offset_x)+"";
			y = (Double.parseDouble(y)+offset_y)+"";
		}

		String toWrite =  "device.touch("+x+","+y+","+type+")\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void type(String msg){
		String toWrite =  "device.type('"+msg+"')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void wakeup(){
		sendCommand("device.wake()\n");
	}
	
	// http://developer.android.com/reference/android/view/KeyEvent.html
	// all string name begins with "KEYCODE_"
	public void press(String keyCode){
		sendCommand("device.press('"+keyCode+"')\n");
	}
	
	public void press(int keyCode){
		sendCommand("device.press('"+keyCode+"')\n");
	}
	
	public void sleep(int sec){
		String toWrite =  "MonkeyRunner.sleep("+sec+")\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void install(String apkPath){
		String toWrite =  "device.installPackage('"+apkPath+"')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startActivity(String packageName, String actName){
		String toWrite =  "runComponent = "+packageName+" + '/' + "+actName+"\n" +
				"device.startActivity(component=runComponent)\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void importLibrary(){
		String toWrite = "from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void connectDevice(){
		String toWrite = "device = MonkeyRunner.waitForConnection()\n";
		System.out.println("connecting");
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finish connecting");
	}
	
	private boolean isNotReadyForNextInput(String msg){
		if(msg == null){ return true;
		}else{ return !msg.trim().endsWith(">>>");}
	}
	
	private void sleepForMonekyReady(){
		String output = null;
		do{
			output = getMonkeyOutput();
			if(DEBUG && output != null){
				output.replace("\n", "\n\t");
				if(DEBUG) Utility.log(TAG,"stdout "+output);
			}
			
			try {Thread.sleep(100);
			} catch (InterruptedException e) {}
			
			String error = getMonkeyError();
			if(error != null){
				if(DEBUG && output != null){
					output.replace("\n", "\n\t");
					if(DEBUG) Utility.log(TAG,"stderr "+output);
				}
				break;
			}
		}while(isNotReadyForNextInput(output));
	}
	
	private String getMonkeyOutput(){
		try {
			int count = istream.available();
			if(count > 0){
				byte[] reading = new byte[count];
				istream.read(reading);
				return new String(reading);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getMonkeyError(){
		try {
			int count = estream.available();
			if(count > 0){
				byte[] reading = new byte[count];
				estream.read(reading);
				return new String(reading);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void sendCommand(String command){
		if(!command.endsWith("\n")){
			command = command+"\n";
		}
		if(DEBUG) Utility.log(TAG,command);
		try {
			ostream.write(command.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	// --------- private helper metods ------------
	private static String readString(InputStream in){
		int count;
		try {
			count = in.available();
			if(count > 0){
				byte[] buffer = new byte[count];
				in.read(buffer);
				return new String(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
