package zhen.framework;

import inputGeneration.RunTimeInfo;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import zhen.implementation.MonkeyExecuter;
import zhen.implementation.WrapperStaticInformation;
import zhen.implementation.decisionMaker.SingleTargetOrientedDecisionMaker;
import zhen.implementation.graph.Event;
import zhen.implementation.graph.GraphStructureLayoutInformation;
import zhen.implementation.graph.RunTimeLayout;
import zhen.packet.ADBControl;

public class Framework {
	public GraphStructureLayoutInformation dynamicInfo;
	public AbstractExecuter executer;
	public AbstractDecisionMaker generater;
	public AbstractStaticInformation staticInfo;
	

	boolean operating = true;
	boolean debug = true;
	boolean enableLogcat= true;
	boolean enableHumanControl = false;
//	final Stack<Event> eventStack;
//	final Stack<RunTimeLayout> layoutStack;
	
	
	private Scanner sc = new Scanner(System.in);
	
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
		ADBControl.sendADBCommand(Configuration.adbPath+" install -r "+apkPath);
		
		staticInfo.init(attribute);
		dynamicInfo.init(attribute);
		executer.init(attribute);
		generater.init(attribute);
	}

	public void execute(){
		System.out.println("Executing");
		while(operating){
			if(enableHumanControl){
				System.out.println("Commandline Interaction");
				while(true){
					System.out.print("Command:");
					String reading = sc.nextLine();
					if(reading.equals("1")){
						dynamicInfo.printAllMethod();
					}else break;
				}
				System.out.println("Continue Operation ");
			}
			
			Event[] event = generater.nextEvent();
			System.out.println("Event:"+Arrays.toString(event));
			if(event == null) continue;
			executer.carryOutEvent(event);
			dynamicInfo.update(event);
		}
	}
	
	public void terminate(){
		
		
//		String method = (String)this.attribute.get("method");
//		if(method != null){
//			
//		}else{
//			System.out.println("can find method");
//		}
		
		
//		dynamicInfo.printAllMethod();
		if(sc != null) sc.close();
		
		generater.terminate();
		executer.terminate();
		dynamicInfo.terminate();
		staticInfo.terminate();
	}
	
	public void reply(){
		String[] methods = (String[])this.attribute.get("methods");
		for(String method : methods){
			System.out.println("Sequence for "+method);
			List<List<Event>>  llevent = this.dynamicInfo.findPotentialPathForHandler(method);
			for(List<Event> levent: llevent){
				System.out.println(levent);
				
				
//				this.executer.carryOutEvent(levent.toArray(new Event[0]));
				
				
			}
		}
		
		
		//DO something 
	}

	public void requestFinish(){
		this.operating = false;
	}

//	public static interface TerminationCallback{
//		
//	}

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
