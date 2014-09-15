package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import zhen.packet.ADBControl;
import main.Paths;

public class JDBStuff {
	
	//public static boolean flag = false;
	//public static boolean monitorOn = false;
	private static Process pc;
	private static OutputStream out;
	private static int tcpPort = 7772;
	private static String srcPath = "src";
	public static ArrayList<String> breakPoints = new ArrayList<String>();
	public static ArrayList<String> bPHitLog = new ArrayList<String>();
	public static ArrayList<String> nonDupe_bPHitLog = new ArrayList<String>();
	public static ArrayList<String> clicksAndBreakPoints = new ArrayList<String>();
	public static ArrayList<String> hitBPfromJDBMonitor = new ArrayList<String>();
	
	public void initJDB(File file) throws Exception{
		String pID = RunTimeInfo.getPID(file);
		
		//pc = Runtime.getRuntime().exec(Paths.adbPath + " shell am start -n com.example.helloworld/.MainActivity");
		//pc.waitFor();
		
		pc = Runtime.getRuntime().exec(Paths.adbPath + " forward tcp:" + tcpPort + " jdwp:" + pID);
		pc.waitFor();
		
		pc = Runtime.getRuntime().exec("jdb -sourcepath " + srcPath + " -attach localhost:" + tcpPort);
		printStreams();
		
		out = pc.getOutputStream();
		if (pc == null) System.out.println("pc is null");
	}
	
	public void setBreakPointLine(String className, int lineNumber) throws Exception{
		out.write(("stop at " + className + ":" + lineNumber + "\n").getBytes());
		out.flush();
		addBreakPoints("line," + className + "," + lineNumber);
	}
	
	public void clearBreakPointLine(String className, int lineNumber) throws Exception{
		out.write(("clear " + className + ":" + lineNumber + "\n").getBytes());
		out.flush();
		ADBControl.sendADBCommand("adb forward --remove tcp:"+tcpPort);
	}
	
	public void setBreakPointsAllLines(ArrayList<String> breakPointList) throws Exception {
		//int i = 0;
		for (String string:breakPointList) {
			//i++;
			setBreakPointLine(string.split(":")[0], Integer.parseInt(string.split(":")[1]));
			//System.out.println("Set breakpoint " + i + "/" + breakPointList.size());
		}
	}
	
	public void clearBreakPointsAllLines(ArrayList<String> breakPointList) throws Exception {
		//int i = 0;
		for (String string:breakPointList) {
			//i++;
			clearBreakPointLine(string.split(":")[0], Integer.parseInt(string.split(":")[1]));
			//System.out.println("Set breakpoint " + i + "/" + breakPointList.size());
		}
	}
	
	public void setBreakPointMethod(String className, String methodName) throws Exception{
		out.write(("stop in " + className + "." + methodName + "\n").getBytes());
		out.flush();
		addBreakPoints("method," + className + "," + methodName);
	}
	
	public void setMonitorStatus(boolean OnOrOff) throws Exception{
		if (OnOrOff) 	out.write("monitor cont\n".getBytes());
		else 			out.write("unmonitor 1\n".getBytes());
	}
	
	public void exitJDB() throws Exception{
		out.write("exit\n".getBytes());
		out.flush();
		
	}
	
	public void setlocalTcpPort(int i) {
		tcpPort = i;
	}
	
	public void setSrcPath(String path) {
		srcPath = path;
	}
	
	private void printStreams() throws Exception{
		(new Thread(new jdbMonitor(new BufferedReader(new InputStreamReader(pc.getInputStream()))))).start();
		(new Thread(new jdbMonitor(new BufferedReader(new InputStreamReader(pc.getErrorStream()))))).start();
	}
	
	public void getClickBreakPoints() throws Exception {
		if(pc == null) System.out.println("pc is null");
		Thread nT = new Thread(new JDBMonkeyCorrelation());
		nT.start();
		nT.join();
	}
	
	private void addBreakPoints(String newBP) {
		boolean exists = false;
		for (String oldBP: breakPoints)
			if (oldBP.equals(newBP))	exists = true;
		if (!exists)	breakPoints.add(newBP);
	}
	
	public void getMethodCoverage() {
		System.out.println(String.format("Total of %d break points was set, %d was reached: %s", breakPoints.size(), nonDupe_bPHitLog.size(), (float)nonDupe_bPHitLog.size()/(float)breakPoints.size() ));
	}
	
}
