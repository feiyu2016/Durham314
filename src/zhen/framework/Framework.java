package zhen.framework;

import java.io.File;
import java.util.Map;
import java.util.Stack;

import zhen.implementation.MonkeyExecuter;
import zhen.implementation.WrapperStaticInformation;
import zhen.implementation.decisionMaker.SingleTargetOrientedDecisionMaker;
import zhen.implementation.graph.Event;
import zhen.implementation.graph.GraphStructureLayoutInformation;
import zhen.implementation.graph.RunTimeLayout;

public class Framework {
	public GraphStructureLayoutInformation dynamicInfo;
	public AbstractExecuter executer;
	public AbstractDecisionMaker generater;
	public AbstractStaticInformation staticInfo;
	
	boolean operating = true;
	boolean debug = true;
//	final Stack<Event> eventStack;
//	final Stack<RunTimeLayout> layoutStack;
	
	private Map<String,Object> attribute;
	String apkPath;
	File apkFile;
	public Framework(String apkPath, Map<String,Object> attribute){
		this.attribute = attribute;
		this.apkPath = apkPath;

		dynamicInfo = new GraphStructureLayoutInformation(this);
		executer = new MonkeyExecuter(this);
		generater = new SingleTargetOrientedDecisionMaker(this);
		staticInfo = new WrapperStaticInformation(this);
	}
	
	public void init(){
		apkFile = new File(apkPath);
		if(!apkFile.exists()){
			throw new AssertionError();
		}
		attribute.put("apkfile", apkFile);
		
		staticInfo.init(attribute);
		dynamicInfo.init(attribute);
		executer.init(attribute);
		generater.init(attribute);
	}

	public void execute(){
		while(operating){
			Event[] event = generater.nextEvent();
			if(event == null) continue;
			executer.carryOutEvent(event);
			dynamicInfo.update(event);
		}
	}
	
	public void terminate(){
		generater.terminate();
		executer.terminate();
		dynamicInfo.terminate();
		staticInfo.terminate();
	}

	public void requestFinish(){
		this.operating = false;
	}

	

//	public Stack<Event> getEventStack() {
//		return eventStack;
//	}
//
//	public Stack<RunTimeLayout> getLayoutStack() {
//		return layoutStack;
//	}
	
//	public void updateUnecessary(){
//		
//	}
}
