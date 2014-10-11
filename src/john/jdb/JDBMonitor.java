package john.jdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class JDBMonitor implements Runnable {
	
	private BufferedReader in;
	private ArrayList<String> BPsHit;
	private boolean exit;
	
	public JDBMonitor (BufferedReader in) {
		this.in = in;
		this.exit = false;
		this.BPsHit.clear();
	}
	
	@Override
	public void run() {
		monitor();
	}
	
	public ArrayList<String> getBPsHit()
	{
		return BPsHit;
	}
	
	public void exitThread()
	{
		this.exit = true;
	}

	private void monitor() {
		String line;
		
		try {
			while (exit == false) {
				if ((line = in.readLine()) == null) continue;
					
				if (!line.startsWith("Breakpoint hit: \"")) continue;
				
				String classAndMethod = line.split(",")[1].trim();
				String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
				String methodSig = classAndMethod.substring(classAndMethod.lastIndexOf(".")+1, classAndMethod.length());
				int lineNumber = Integer.parseInt(line.split(",")[2].trim().split(" ")[0].split("=")[1]);
				
				if (!BPsHit.contains(className + "," + methodSig + "," + lineNumber)) {
					BPsHit.add(className + "," + methodSig + "," + lineNumber);
				}
			}

		} catch (IOException e) {e.printStackTrace();}
	}
}
