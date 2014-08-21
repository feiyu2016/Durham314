package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import main.Paths;

public class JDBStuff {
	
	private Process pc;
	private OutputStream out;
	private int tcpPort = 7772;
	private String srcPath = "src";
	public ArrayList<String> breakPoints = new ArrayList<String>();
	
	
	public void initJDB(File file) throws Exception{
		String pID = findPID(file);
		
		//pc = Runtime.getRuntime().exec(Paths.adbPath + " shell am start -n com.example.helloworld/.MainActivity");
		//pc.waitFor();
		
		pc = Runtime.getRuntime().exec(Paths.adbPath + " forward tcp:" + tcpPort + " jdwp:" + pID);
		pc.waitFor();
		
		pc = Runtime.getRuntime().exec("jdb -sourcepath " + srcPath + " -attach localhost:" + tcpPort);
		printStreams();
		
		out = pc.getOutputStream();
	}
	
	public void setBreakPointLine(String className, int lineNumber) throws Exception{
		out.write(("stop at " + className + ":" + lineNumber + "\n").getBytes());
		out.flush();
		breakPoints.add("line," + className + "," + lineNumber);
	}
	
	public void setBreakPointMethod(String className, String methodName) throws Exception{
		out.write(("stop in " + className + "." + methodName + "\n").getBytes());
		out.flush();
		breakPoints.add("method," + className + "," + methodName);
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
		(new Thread(new OutputMonitor(new BufferedReader(new InputStreamReader(pc.getInputStream()))))).start();
		(new Thread(new OutputMonitor(new BufferedReader(new InputStreamReader(pc.getErrorStream()))))).start();
	}
	
	private String findPID(File file) throws Exception{
		Process prc = Runtime.getRuntime().exec(Paths.adbPath + " shell ps |grep " + StaticInfo.getPackageName(file));
		BufferedReader in = new BufferedReader(new InputStreamReader(prc.getInputStream()));
		String line;
		while ((line = in.readLine())!=null) {
			String[] parts = line.split(" ");
			for (int i = 1; i < parts.length; i++) {
				if (parts[i].equals(""))	continue;
				return parts[i].trim();
			}
		}
		return "";
	}
	
}
