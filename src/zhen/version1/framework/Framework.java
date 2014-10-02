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

public class Framework {
	
	public StaticInformation sInfo;
	public RunTimeInformation rInfo;
	public TraversalEventGenerater traverser;
	public Executer executer;
	public UIExplorer explorer;
	
	public String apkPath;
	private File apkFile;
	private Map<String, Object> attributes;
	private OnTraverseFinishCallBack callback;
	
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
	 * in the future, might want to put every thing into loop
	 */
	public void start(){
		//make decision
		
		//expand knowledge on the UI model
		explorer.traverse();
		
		callback.action(this);
	}
	
	private void checkArguments(){
		if(!this.attributes.containsKey(Common.apkPath)){
			throw new IllegalArgumentException("Require apk path.");
		}
		this.apkPath = (String) attributes.get(Common.apkPath);
		File apkFile = new File(apkPath);
		if(!apkFile.exists())throw new IllegalArgumentException("Require apk path.");
		this.attributes.put(Common.apkFile, apkFile);
	}
	
	public void registerOnTraverseFinishCallBack(OnTraverseFinishCallBack callback){
		this.callback = callback;
	}
	
	public interface OnTraverseFinishCallBack{
		public void action(Framework frame);
	}
}
