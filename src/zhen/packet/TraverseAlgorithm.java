package zhen.packet;

import inputGeneration.JDBStuff;
import inputGeneration.Layout;
import inputGeneration.ParseSmali;
import inputGeneration.StaticInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import viewer.MonkeyWrapper;
import viewer.ViewPositionData;

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
	protected ViewPositionData.NodeDataFilter dataFilter;
	private static Logger logger = Utility.setupLogger(TraverseAlgorithm.class);
	
	public boolean enableAPKTool = false, enableStaticInfo = true, enableJDB = false,
			enableStaticAnalysis = false,
			showStaticInfo = true, enableDynamicInit = true , enablePostRun = true,
			enablePreRun = true, enableExcute = true, forceReinstallOnStartup = false;
			
	public TraverseAlgorithm(String apkPath){
		this.apkFile = new File(apkPath);
		this.apkPath = apkPath;
		if(!this.apkFile.exists()){
			logger.severe("File "+apkPath+" does not exist");
			throw new IllegalArgumentException("Invalid apk path");
		}
	}
	public void start(){
		long time1 = System.currentTimeMillis();
		logger.info("TraverseAlgorithm starts");
		if(enablePreRun)	preRun();
		if(enableExcute)	execute();
		if(enablePostRun)	postRun();
		long time2 = System.currentTimeMillis();
		System.out.println("Total: "+(time2-time1));
	}
	
	public abstract void execute();
	public abstract void showLogInformation();
	
	public void setNormalStartUp(){
		logger.info("normal setting");
		enableAPKTool = true;
		enableStaticInfo = true;
		enableJDB = true;
		showStaticInfo = false;
		enableDynamicInit = true;
		enablePostRun = true;
		enablePreRun = true;
		enableExcute = true;
	}
	private ArrayList<String> methodSignature;
	
	protected void initJDB(){
		try {
			if(jdb!=null) try { jdb.exitJDB(); } catch (Exception e1) {  }
			jdb = new JDBStuff();
			jdb.initJDB(apkFile);
			jdb.setMonitorStatus(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	protected void setUpBreakPoint(){
		if(enableJDB){
			try {
				jdb.setBreakPointsAllLines(methodSignature);
				Thread.sleep(1000);
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}
	
	
	
//	protected void setUpBreakPoint(){
//		if(enableJDB){
//			logger.info("JDB initialization starts");
////			if(jdb!=null)
////				try { jdb.exitJDB();
////				} catch (Exception e1) {  }
//			jdb = new JDBStuff();
//			try {
//				jdb.initJDB(apkFile);
//				jdb.setMonitorStatus(true);
//				jdb.setBreakPointsAllLines(methodSignature);
//			} catch (Exception e) {
//				e.printStackTrace();
//				logger.warning(e.getMessage());
//			}
//			try {
//				Thread.sleep(1500);
//			} catch (InterruptedException e) { }
//			logger.info("JDB initialization ends");
//		}
//	}
	protected void clearUpBreakPoint(){
		if(enableJDB){
			try {
				jdb.clearBreakPointsAllLines(methodSignature);
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}
	
	
	// --------------- private method starts -----------------
	private void preRun(){
		logger.info("pre-Run starts");
		
		//APKtool process
		if(enableAPKTool){
//			logger.info("APKTool initialization starts");
//			analysisTools.ApkTool.extractAPK(apkFile);
//			analysisTools.Soot.generateAPKData(apkFile);
//			StaticInfo.process_Intents_And_setContentView(apkFile);
			logger.info("APKTool initialization ends");
		}

		//setup static info
		if(enableStaticInfo){
			logger.info("static infomation generation starts");
			if(enableStaticAnalysis){
				StaticInfo.initAnalysis(apkFile, true);
			}
			packageName = StaticInfo.getPackageName(apkFile);
			staticLayout = StaticInfo.getLayoutList(apkFile);
			
			activityNames = StaticInfo.getActivityNames(apkFile);
			actMark = new boolean[activityNames.size()];
			logger.info("static infomation generation ends");
		}
		
		//init JDB and setup break points at ALL Lines!?
		if(this.enableJDB){
//			jdb = new JDBStuff();
			methodSignature = new ParseSmali().parseLines(apkFile);
		}

		//initialize hierarchy view and Monkey
		if(enableDynamicInit){
			logger.info("dynamic run objects initialization starts");
			viewData = new ViewPositionData();
			//TODO change the filter to calculate the x,y
			if(dataFilter != null) viewData.setDataFilter(dataFilter);
			viewData.debug = true;
			if(!viewData.init()){
				logger.severe("Cannot init viewPositionData");
				throw new IllegalStateException("Cannot init viewPositionData");
			}
			monkey = new MonkeyWrapper();
			monkey.enableTouchOffset = true;
			monkey.startInteractiveModel();
			//Wait for monkey stabilized
			try { Thread.sleep(2000);
			} catch (InterruptedException e) { }
			
			//setup device and install package
//			monkey.interactiveModelWakeUp();System.out.println("preRun finished");
//			ADBControl.sendADBCommand(ADBControl.unlockScreenCommand);
//			monkey.interactiveMdoelInstall(apkPath);

			logger.info("dynamic run objects initialization ends");
		}

		checkAPKInstallation();
		
		logger.info("pre-Run starts");
		if(showStaticInfo)showStaticInfo();
	}
	
	private void postRun(){
		logger.info("post-run starts");
		monkey.stopInteractiveModle();
		try {
			if(jdb!=null) jdb.exitJDB();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(jdb != null){
			jdb.getMethodCoverage();
		}
		this.showLogInformation();
		logger.info("post-run ends");
	}
	
	private void showStaticInfo(){
		logger.info("showing StaticInfo");
		System.out.println("Activities: "+activityNames);
//		System.out.println(StaticInfo.getMainActivityName(apkFile));
	}
	
	private void checkAPKInstallation(){
		if(forceReinstallOnStartup){
			ADBControl.sendADBCommand("adb uninstall "+packageName);
			//may want to check the feed back
			try { Thread.sleep(2000);
			} catch (InterruptedException e) { }
		}
		
		ADBControl.sendADBCommand("adb install "+this.apkPath);
	}
}
