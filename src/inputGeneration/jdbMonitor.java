package inputGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class jdbMonitor implements Runnable{

	private BufferedReader in;
	private boolean inStream;
	
	public jdbMonitor(BufferedReader bR, boolean iS) {
		in = bR;
		inStream = iS;
	}
	
	@Override
	public void run() {
		startsMonitoring();
	}

	private void startsMonitoring() {
		String line;
		long time;
		
		try {
			time = System.currentTimeMillis();
			while ((line = in.readLine())!=null) {
				// read that file, see if any of those break points are reached
				if ((System.currentTimeMillis() - time) > 1000) {
					if(inStream) JDBStuff.flag = true;
				}
				if (!line.startsWith("Breakpoint hit: \"")) continue;
				//System.out.println(line);
				JDBStuff.hitBPfromJDBMonitor.add(line);
				time = System.currentTimeMillis();
				String classAndMethod = line.split(",")[1].trim();
				String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
				String methodSig = classAndMethod.substring(classAndMethod.lastIndexOf(".")+1, classAndMethod.length());
				int lineNumber = Integer.parseInt(line.split(",")[2].trim().split(" ")[0].split("=")[1]);
				long timeStamp = System.currentTimeMillis();
				JDBStuff.bPHitLog.add(timeStamp + "," + className + "," + methodSig + "," + lineNumber);
				JDBStuff.clicksAndBreakPoints.add(timeStamp + "," + className + "," + methodSig + "," + lineNumber);
				addNonDupeBPHit(className + "," + methodSig + "," + lineNumber);
			}
			System.out.println("WARNING: BufferedReader ended. This shouldn't have happened.");
		} catch (IOException e) {e.printStackTrace();}
	}
	
	private void addNonDupeBPHit(String bPHitInfo) {
		boolean exists = false;
		for (String bpHit: JDBStuff.nonDupe_bPHitLog)
			if (bpHit.equals(bPHitInfo))
				exists = true;
		if (!exists)	JDBStuff.nonDupe_bPHitLog.add(bPHitInfo);
	}
	
}
