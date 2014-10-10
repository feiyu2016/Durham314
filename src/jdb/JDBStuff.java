package jdb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import main.Paths;

public class JDBStuff {
	
	private Process pc;
	private OutputStream out;
	private int tcpPort = 7772;
	private String deviceID = "";
	private String srcPath = "src";
	private int flag = 0;

	public void initJDB(String packageName, String deviceID, int flag) throws Exception{
		this.deviceID = deviceID;
		this.flag = flag;
		
		String pID = getPID(packageName);
		
		pc = Runtime.getRuntime().exec(Paths.adbPath +" -s " + deviceID + " forward tcp:" + tcpPort + " jdwp:" + pID);
		pc.waitFor();
		
		pc = Runtime.getRuntime().exec("jdb -sourcepath " + srcPath + " -attach localhost:" + tcpPort);
		
		printStreams();
		out = pc.getOutputStream();
		if (pc == null) System.out.println("pc is null");
	}
	
	public String getPID(String packageName) throws Exception {
		Process prc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + deviceID + " shell ps |grep " + packageName);
		BufferedReader in = new BufferedReader(new InputStreamReader(prc.getInputStream()));
		String line;
		while ((line = in.readLine())!=null) {
			if (!line.endsWith(packageName)) continue;
			String[] parts = line.split(" ");
			for (int i = 1; i < parts.length; i++) {
				if (parts[i].equals(""))	continue;
				return parts[i].trim();
			}
		}
		return "-1";
	}
	
	public void setBreakPointsAtLines(String className, ArrayList<Integer> lineNumbers) throws Exception{
		for (int lineNumber: lineNumbers) {
			out.write(("stop at " + className + ":" + lineNumber + "\n").getBytes());
			out.flush();
		}
	}
	
	public void setMonitorStatus(boolean OnOrOff) throws Exception{
		if (OnOrOff) 	out.write("monitor cont\n".getBytes());
		else 			out.write("unmonitor 1\n".getBytes());
	}
	
	public void exitJDB() throws Exception{
		out.write("exit\n".getBytes());
		out.flush();
		pc.waitFor();
	}
	
	public void setlocalTcpPort(int i) {
		tcpPort = i;
	}
	
	public void setSrcPath(String path) {
		srcPath = path;
	}
	
	private void printStreams() throws Exception{
		(new Thread(new jdbMonitor(new BufferedReader(new InputStreamReader(pc.getInputStream())), flag))).start();
		(new Thread(new jdbMonitor(new BufferedReader(new InputStreamReader(pc.getErrorStream())), flag))).start();
	}
	
	
}
