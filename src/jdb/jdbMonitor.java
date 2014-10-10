package jdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import john.runtimeValidation.DualWielding;

public class jdbMonitor implements Runnable{

	private BufferedReader in;
	private int flag;
	
	public jdbMonitor(BufferedReader bR, int flag) {
		in = bR;
		this.flag = flag;
	}
	
	@Override
	public void run() {
		if (flag == 1)
			DualWielding.single_result_1 = new ArrayList<String>();
		else if (flag ==2)
			DualWielding.single_result_2 = new ArrayList<String>();
		startsMonitoring();
	}

	private void startsMonitoring() {
		String line;
		
		try {
			while ((line = in.readLine())!=null) {
				//System.out.println(line);
				if (!line.startsWith("Breakpoint hit: \"")) continue;
				String classAndMethod = line.split(",")[1].trim();
				String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
				String methodSig = classAndMethod.substring(classAndMethod.lastIndexOf(".")+1, classAndMethod.length());
				int lineNumber = Integer.parseInt(line.split(",")[2].trim().split(" ")[0].split("=")[1]);
				addNonDupeBPHit(className + "," + methodSig + "," + lineNumber);
			}

		} catch (IOException e) {e.printStackTrace();}
	}
	
	private void addNonDupeBPHit(String bPHitInfo) {
		if (flag == 1) {
			if (!DualWielding.single_result_1.contains(bPHitInfo))
				DualWielding.single_result_1.add(bPHitInfo);
		}
		else if (flag == 2) {
			if (!DualWielding.single_result_2.contains(bPHitInfo))
				DualWielding.single_result_2.add(bPHitInfo);
		}
	}
	
}
