package zhen.version1.framework;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import zhen.packet.ADBControl;
import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;

/**
 * Responsibility: Explore the UI until some condition is satisfied.
 * 	Conditions:
 * 	1.	No new Event could be applied on any layout.
 * 	2.	Max Event count is reached.
 * 	3.	Target layout is reached. 
 * 
 * By default, during traversal, the explorer will try to get back to the previous
 * layout if a known layout is encountered. A flag along with a maximum steps could 
 * be set if the explorer is instructed to keep forwarding all the time.(E.g. treat 
 * onBack the same as others event)
 * 
 * Step control (after each event applied) can be enabled and allows information 
 * inspection. 
 * 
 * 
 * @author zhenxu
 *
 */
public class UIExplorer {
	public static boolean DEBUG =true;
	public static String TAG = "UIExplorer";
	
	boolean operating = true;
	boolean debug = true; 
	private boolean enableStepControl = false;
	private int maxStep, currentStep;
	private StepControlCallBack stepControl;
	
	
	protected Framework frame;
	public UIExplorer(Framework frame){
		this.frame = frame;
	}
	
	public void traverse(){
		this.traverse(-1,false);
	}
	public void traverse(int maxCount){
		this.traverse(maxCount,false);
	}
	public void traverse(int maxStep, boolean keepForwarding){
		//get a copy of references
		TraversalEventGenerater traverser = frame.traverser;
		RunTimeInformation rInfo = frame.rInfo;
		StaticInformation sInfo = frame.sInfo;
		Executer executer = frame.executer;
		
		currentStep = 0;
		while(checkRestriction()){
			if(enableStepControl){ stepControlCallBack();}
			Event event = traverser.nextEvent(rInfo);
			if(DEBUG)Utility.log(TAG, "traverse next Event,"+event);
			if(event == null) break;
			executer.applyEvent(event);
			rInfo.update(event);
			currentStep += 1;
		}
	}
	public boolean reachUIState(UIState state){
		TraversalEventGenerater traverser = frame.traverser;
		RunTimeInformation rInfo = frame.rInfo;
		StaticInformation sInfo = frame.sInfo;
		Executer executer = frame.executer;
		
//		rInfo.getEventSequence(layout)
		
		return false;
	}
	
	/**
	 * check if any restriction is met. e.g. max step 
	 * @return
	 */
	public boolean checkRestriction(){
		if(maxStep>0 && currentStep>=maxStep)return false; 
		if(operating == false) return false;
		else return true;
	}
	
	
	public void enableStepControl(boolean flag){
		enableStepControl = flag;
	}
	public void registerStepControlCallBack(StepControlCallBack callback){
		stepControl = callback;
	}
	public static interface StepControlCallBack{
		public void action(Framework frame);
	}
	
	private void stepControlCallBack(){
		if(this.stepControl == null) return;
		if(DEBUG) Utility.log(TAG,"UIExplorer Step Control.");
		stepControl.action(this.frame);
		if(DEBUG) Utility.log(TAG,"UIExplorer Operation Continues.");
	}
}
