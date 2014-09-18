package zhen.underConsturction;

import inputGeneration.StaticInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import com.android.hierarchyviewerlib.models.Window;

import zhen.graph.TraversalGraph;
import zhen.packet.RunTimeLayoutInformation;
import zhen.packet.Utility;

public class Framework implements Runnable{
	private String apkFilePath;
	private File apkFile;
	private String TAG = "Framework";
	private MonkeyExecuter executer;
	private RunTimeLayoutInformation layoutInfo;
	private TraversalGraph traversalGraph;
	private boolean executeOnCommand = false;
	private boolean debug = false;
	private Scanner consoleInput;
	
	public Framework(String apkFilePath){
		this.apkFilePath = apkFilePath;
		executer = new MonkeyExecuter(this);
		traversalGraph = new TraversalGraph();
	}
	public ArrayList<String> actNameList;
	
	public void init(){
		//
		apkFile = new File(this.apkFilePath);
		if(!apkFile.exists()){
			Utility.log(TAG, "Cannot find apk file:"+this.apkFilePath);
			throw new AssertionError();
		}
		
		StaticInfo.initAnalysis(apkFile, false);
		actNameList = StaticInfo.getActivityNames(apkFile);
		StaticInfo.prepareCG(apkFile);
		//TODO other static info

		executer.init();
		layoutInfo.init();
		traversalGraph.defineActivityScope(actNameList.toArray(new String[0]));

		switchToHome();
		Window launcher = layoutInfo.getFocusedWindow();
		if(!launcher.getTitle().contains("launcher")){
			throw new AssertionError();
		}
		traversalGraph.setLauncherActName(launcher.getTitle());
		traversalGraph.enableGUI();
	}
	
	@Override
	public void run() {
		init();
		
		boolean operating = true;
		while(operating){
			if(executeOnCommand){
				if(consoleInput == null) consoleInput = new Scanner(System.in);
				System.out.print("Press any key to continue:");
				String reading = consoleInput.nextLine().trim();
				if(reading.equals("0")) break;
			}
			

			
		}
		
	
	}
	
	
	
	private void switchToHome(){
		//TODO
	}

	public TraversalGraph getTraversalGraph() {
		return traversalGraph;
	}

	public RunTimeLayoutInformation getLayoutInfo() {
		return layoutInfo;
	}
	
	
}
