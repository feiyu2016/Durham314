package zhen.version1.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.Map.Entry;

import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;
import zhen.version1.framework.Common;
import zhen.version1.framework.Framework;
import zhen.version1.framework.RunTimeInformation;
import zhen.version1.framework.UIExplorer;
import zhen.version1.framework.UIExplorer.StepControlCallBack;

public class SampleTestClass {
	
	public static void main(String[] args) {
		//choose the name of apk file
		String[] name = {
				"signed_backupHelper.apk",
				"signed_Butane.apk",
				"signed_CalcA.apk",
				"signed_KitteyKittey.apk",
		};
		int index = 0;
		String path = "APK/"+name[index];
		
		//setup input parameters
		Map<String,Object> att = new HashMap<String,Object>();
		att.put(Common.apkPath	, path);
		
		Framework frame = new Framework(att);
		
		//once the step control is added, the program will wait for human instruction
		//before entering next operation loop
		addExplorerStepControl(frame);
		//add a call back method which is called after the finish of traversal
		addOnTraverseFinishCallBack(frame);
		
		
		//NOTE: right now it does require apk installed on the device
		//		and please close the app previous opened
		frame.setup();//initialize
		frame.start();//start experiment
		
		System.out.println("Finish!");
	}
	
	private static void addExplorerStepControl(Framework frame){
		UIExplorer explorer = frame.explorer;
		explorer.enableStepControl(true);
		explorer.registerStepControlCallBack(new StepControlCallBack(){
			private Scanner sc = new Scanner(System.in);
			@Override
			public void action(Framework frame) {
				while(true){
					String read = sc.nextLine().trim();
					if(read.equals("1")){
						Stack<UIState> stack = frame.traverser.getUIStack();
						for(UIState state : stack){
							Utility.info(frame.traverser.TAG, state);
						}
					}else if(read.equals("2")){
						Map<String, List<Event>> map = frame.rInfo.getMethodEventMap();
						for(Entry<String, List<Event>> entry : map.entrySet()){
							Utility.info(RunTimeInformation.TAG,entry);
						}
					}else if(read.equals("h")){
						Utility.info(UIExplorer.TAG,"1: show stack, 2: get method map");
					}else break;
				}	
			}
		});
	}
	
	private static void addOnTraverseFinishCallBack(Framework frame){
		frame.registerOnTraverseFinishCallBack(new Framework.OnTraverseFinishCallBack(){
			@Override
			public void action(Framework frame) {
				// show the map between method and event
				Map<String, List<Event>> map = frame.rInfo.getMethodEventMap();
				for(Entry<String, List<Event>> entry : map.entrySet()){
					Utility.info(RunTimeInformation.TAG,entry);
				}
				
				//sample
				//	com.example.backupHelper.BackupFilesListAdapter: void reset(boolean)
				//	=[launch com.example.backupHelper/com.example.backupHelper.BackupActivity]     

				
				
			}
		});
	}
}
