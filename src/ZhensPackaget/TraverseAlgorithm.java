package ZhensPackaget;

import inputGeneration.JDBStuff;
import inputGeneration.Layout;
import inputGeneration.ParseSmali;
import inputGeneration.StaticInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import viewer.MonkeyWrapper;
import viewer.ViewPositionData;
import viewer.ViewPositionData.StringValueRetriever;

/**
 * 
 * Travel through view objects on device
 * @author zhenxu
 *
 */
public abstract class TraverseAlgorithm {
	protected File apkFile;
	protected String apkPath, packageName;
	protected ViewPositionData viewData;
	protected MonkeyWrapper monkey;
	protected JDBStuff jdb;
	protected List<String> activityNames;
	protected List<Layout> staticLayout;
	protected boolean[] actMark;
	private String[] layoutKeywords = {
			"drawing:getX()",
			"drawing:getY()",
			"drawing:mLayerType",
			
	};
	
	public boolean enableAPKTool = false, enableStaticInfo = true, enableJDB = false, 
			showStaticInfo = true, enableDynamicInit = true;
	
	public TraverseAlgorithm(String apkPath){
		this.apkFile = new File(apkPath);
		this.apkPath = apkPath;
		
		if(!this.apkFile.exists()){
			throw new IllegalArgumentException("Invalid apk path");
		}
	}
	public void start(){
		preRun();
		execute();
		afterRun();
	}
	
	public abstract void execute();
	
	// --------------- private method starts -----------------
	private void preRun(){
		//APKtool process
		if(enableAPKTool){
			analysisTools.ApkTool.extractAPK(apkFile);
			analysisTools.Soot.generateAPKData(apkFile);
			StaticInfo.process_Intents_And_setContentView(apkFile);
		}

		//setup static info
		if(enableStaticInfo){
			activityNames = StaticInfo.getActivityNames(apkFile);
			actMark = new boolean[activityNames.size()];
			
			packageName = StaticInfo.getPackageName(apkFile);
			List<Layout> staticLayout = StaticInfo.getLayoutList(apkFile);
		}
		
		//init JDB and setup break points at ALL Lines!?
		if(enableJDB){
			ArrayList<String> methodSignature = new ParseSmali().parseLines(apkFile);
			jdb = new JDBStuff();
			try {
				jdb.initJDB(apkFile);
				jdb.setMonitorStatus(true);
				jdb.setBreakPointsAllLines(methodSignature);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//initialize hierarchy view and Monkey
		if(enableDynamicInit){
			viewData = new ViewPositionData();
			//TODO change the filter to calculate the x,y
			viewData.setDataFilter(new ViewPositionData.StringValueRetriever(layoutKeywords));
			viewData.debug = true;
			if(!viewData.init()){
				throw new IllegalStateException("Cannot init viewPositionData");
			}
			monkey = new MonkeyWrapper();
			monkey.startInteractiveModel();
			//Wait for monkey stabilized
			try { Thread.sleep(2000);
			} catch (InterruptedException e) { }
			
			//setup device and install package
//			monkey.interactiveModelWakeUp();System.out.println("preRun finished");
//			ADBControl.sendADBCommand(ADBControl.unlockScreenCommand);
			monkey.interactiveMdoelInstall(apkPath);
				
		}
		
		System.out.println("preRun finished");
		if(showStaticInfo)showStaticInfo();
	}
	
	private void afterRun(){
		monkey.stopInteractiveModle();
		if(jdb != null){
			jdb.getMethodCoverage();
		}
	}
	
	private void showStaticInfo(){
		System.out.println("Activities: "+activityNames);
//		System.out.println(StaticInfo.getMainActivityName(apkFile));
	}
	

	
}
