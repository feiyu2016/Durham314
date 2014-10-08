package zhen.version1.framework;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;

import zhen.version1.Support.CommandLine;
import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;
import zhen.version1.framework.UIExplorer.StepControlCallBack;

/**
 * A framework class
 * 1.	contains major objects
 * 2.	contains a procedure -- setup/function/terminate
 * 
 * @author zhenxu
 *
 */
public class Framework {
	public static String TAG = "Framework";
	
	public StaticInformation sInfo;
	public RunTimeInformation rInfo;
	public TraversalEventGenerater traverser;
	public Executer executer;
	public UIExplorer explorer;
	
	public String apkPath;
	private File apkFile;
	private Map<String, Object> attributes;
	private OnProcedureEndsCallBack callback;
	private boolean enableStdinMonitor =false;
	
	/**
	 * Constructor 
	 * @param attributes -- used to passed a list of attributes
	 * 					 -- see Class "common" to further information
	 */
	public Framework(Map<String, Object> attributes){
		this.attributes = attributes;
		checkArguments();

		sInfo = new StaticInformation(this);
		explorer = new UIExplorer(this);
		traverser = new TraversalEventGenerater(this);
		rInfo = new RunTimeInformation(this);
		executer = new Executer(this);
	}
	
	/**
	 * setup the environment, including install APP
	 */
	public void setup(){
		sInfo.init(attributes, false);
		rInfo.init(attributes);
		traverser.init(attributes);
		executer.init(attributes);
		rInfo.enableGUI();
		
		executer.wakeDeviceup();
		CommandLine.executeCommand(CommandLine.unlockScreenCommand);
	}
	
	/**
	 * Should be call when the program intend to terminates
	 */
	public void terminate(){
		this.rInfo.terminate();
		this.traverser.terminate();
		this.executer.terminate();
		this.sInfo.terminate();
	}
	
	/**
	 * in the future, might want to put every thing into loop
	 */
	public void start(){	
		if(enableStdinMonitor) stdinMonitor.start();
		//make decision
		
		//expand knowledge on the UI model
		explorer.traverse();
		
		callback.action(this);
	}
	
	/**
	 * enable stdin monitor which monitor input from stdin
	 * Currently used for manual termination after typing "stop"
	 * @param input
	 */
	public void enableStdinMonitor(boolean input){
		enableStdinMonitor = input;
	}
	
	private Thread stdinMonitor = new Thread(new Runnable(){
		private Scanner sc = new Scanner(System.in);
		@Override
		public void run() {  
			String reading ="";
			while(sc.hasNextLine()){
				reading=sc.nextLine();
				if(reading.equals("stop")){
					explorer.requestStop();
					break;
				}
			} 	
			sc.close();
		} 
	});
	
	private void checkArguments(){
		if(!this.attributes.containsKey(Common.apkPath)){
			throw new IllegalArgumentException("Require apk path.");
		}
		this.apkPath = (String) attributes.get(Common.apkPath);
		File apkFile = new File(apkPath);
		if(!apkFile.exists())throw new IllegalArgumentException("Require apk path.");
		this.attributes.put(Common.apkFile, apkFile);
	}
	
	/**
	 * set the call back method after the procedure ends 
	 * @param callback
	 */
	public void setOnProcedureEndsCallBack(OnProcedureEndsCallBack callback){
		this.callback = callback;
	}
	
	/**
	 * Call back interface 
	 * @author zhenxu
	 *
	 */
	public interface OnProcedureEndsCallBack{
		public void action(Framework frame);
	}
	
	public Map<String,Object> getAttributes(){
		return attributes;
	}
}
