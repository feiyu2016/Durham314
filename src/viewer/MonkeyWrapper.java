package viewer;

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
import java.util.logging.Logger;

import android.view.KeyEvent;
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
public class MonkeyWrapper {
	public static String PROGRAM_LOCATION 
		= Paths.androidToolPath+"/monkeyrunner";
	public static final String UP = "MonkeyDevice.UP";
	public static final String DOWN = "MonkeyDevice.DOWN";
	public static final String DOWN_AND_UP = "MonkeyDevice.DOWN_AND_UP";
	public static boolean Debug = true;
	public boolean enableTouchOffset = false;
	private int offset_x = 50, offset_y = 50;
	private Logger logger = Utility.setupLogger(MonkeyWrapper.class);
	
	public void setProgramLocation(String loc){
		PROGRAM_LOCATION = loc; 
	}
	
	private File tmp = null;
	private PrintWriter pw = null;
	private static String tempFileName = "testCase";
	private StringBuilder sb = null;
	private Process monkeyProcess = null;
	private BufferedOutputStream ostream = null;
	private BufferedInputStream estream = null;
	private BufferedInputStream istream = null;
//	private Thread monkeyInformationMonitor;
	private boolean started = false;
	
	public void startInteractiveModel(){
		if(started){
			logger.info("Already started");
			return;
		}
		if(Debug)logger.info("Stating interactive model");
		try {
			monkeyProcess = Runtime.getRuntime().exec(PROGRAM_LOCATION);
			ostream = new BufferedOutputStream(monkeyProcess.getOutputStream());
			estream = new BufferedInputStream(monkeyProcess.getErrorStream());
			istream = new BufferedInputStream(monkeyProcess.getInputStream());
			
			sleepForMonekyReady();
			
			started = true;
			interactiveModleImport();
			interactiveModleConnectDevice();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }
			readString(estream);
			readString(istream);
			if(Debug)logger.info("MonkeyRunner starts sucessfully");
		} catch (IOException e) {
			e.printStackTrace();
			try {
				if(ostream!=null) ostream.close();
				if(estream!=null) estream.close();
				if(istream!=null) istream.close();	
			} catch (IOException e1) { }
			if(monkeyProcess != null) monkeyProcess.destroy();
			started = false;
			if(Debug)logger.info("MonkeyWrapper fails to start");
		}
	}
	
	public void stopInteractiveModle(){
		if(Debug)logger.info("MonkeyWrapper stopped");
		if(!started) return;
		
		if(monkeyProcess!=null){
			try { 
//				System.out.println(getMonkeyFeedBack());
				ostream.close();
				istream.close();
				estream.close();
			} catch (IOException e) {  }
			monkeyProcess.destroy(); 
		}
		started = false;
	}
	
	public void inteactiveModelClick(String x, String y){
		interactiveModelTouch(x,y,DOWN_AND_UP);
	}
	
	public void interactiveModelTouch(String x, String y, String type){
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
	
	public void interactiveModelType(String msg){
		String toWrite =  "device.type('"+msg+"')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void interactiveModelWakeUp(){
		sendCommand("device.wake(\n)");
	}
	
	// http://developer.android.com/reference/android/view/KeyEvent.html
	// all string name begins with "KEYCODE_"
	public void interactiveModelPress(String keyCode){
		if(Debug)logger.info("press "+keyCode);
		sendCommand("device.press('"+keyCode+"')\n");
	}
	
	public void interactiveModelPress(int keyCode){
		if(Debug)logger.info("press "+keyCode);
		sendCommand("device.press('"+keyCode+"')\n");
	}
	
	public void interactiveModelSleep(int sec){
		String toWrite =  "MonkeyRunner.sleep("+sec+")\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void interactiveMdoelInstall(String apkPath){
		String toWrite =  "device.installPackage('"+apkPath+"')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void interactiveModelStartActivity(String packageName, String actName){
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
	
	private void interactiveModleImport(){
		String toWrite = "from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void interactiveModleConnectDevice(){
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
			if(Debug && output != null){
				output.replace("\n", "\n\t");
				logger.fine("stdout "+output);
			}
			
			try {Thread.sleep(100);
			} catch (InterruptedException e) {}
			
			String error = getMonkeyError();
			if(error != null){
				if(Debug && output != null){
					output.replace("\n", "\n\t");
					logger.info("stderr "+output);
				}
				break;
			}
		}while(isNotReadyForNextInput(output));
	}
	
	private String getMonkeyFeedBack(){
		StringBuilder sb = new StringBuilder();
		try {
			int count = estream.available();
			if(count > 0){
				byte[] eReading = new byte[count];
				estream.read(eReading);
				sb.append(new String(eReading));
			}
			
			count = istream.available();
			if(count > 0){
				byte[] iReading = new byte[count];
				istream.read(iReading);
				return new String(iReading);
			}
			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void sendCommand(String command){
		if(!command.endsWith("\n")){
			command = command+"\n";
		}
		try {
			ostream.write(command.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// ------------------------ Interaction Procedure ends ------------------------
	
	
	public void testWithScript(String path){
		Process p;
		try {
			p = Runtime.getRuntime().exec(PROGRAM_LOCATION+" "+path);
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void createTestCase(){
//		URL location = Test.class.getProtectionDomain().getCodeSource().getLocation();
//		location.getFile()+"/"+
		tmp = new File(tempFileName);
		try { pw = new PrintWriter(tmp);
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		}
		sb = new StringBuilder();
	}
	public void commitTestCase(){
		System.out.println("File\n"+sb);
		pw.println(sb);
		pw.close();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) { }
		
		Process p;
		try {
			p = Runtime.getRuntime().exec(PROGRAM_LOCATION+" "+tmp.getAbsolutePath());
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb = null;
//		tmp.delete();
	}
	
	public void appendInitString(){
		sb.append(
				"from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice\n"+
				"device = MonkeyRunner.waitForConnection()\n");
		sb.append("\n");
	}
	
	public void appendStartActivityString(String packageName, String actName){
		sb.append("runComponent = "+packageName+" + '/' + "+actName+"\n" +
					"device.startActivity(component=runComponent)\n"
				); 
		sb.append("\n");
	}
	
	public void appendInstallationString(String apkPath){
		sb.append("device.installPackage('"+apkPath+"')\n");sb.append("\n");
	}
	
	public void appendTouchString(String x, String y, String type){
		sb.append("device.touch("+x+","+y+","+type+")");sb.append("\n");
	}
	
	public void appendSleepString(int sec){
		sb.append("MonkeyRunner.sleep("+sec+")\n");sb.append("\n");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
			
}
