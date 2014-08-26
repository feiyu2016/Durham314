package inputGeneration;

import java.io.BufferedReader;
import java.io.IOException;

public class JDBMonkeyCorrelation implements Runnable {
	
	public BufferedReader in;
	
	public JDBMonkeyCorrelation(BufferedReader bR)
	{
		in = bR;
	}
	
	public void run() 
	{
		getBreakPointsFromClick();
	}
	
	private void getBreakPointsFromClick()
	{
		String line;
		long time;
		try {
			time = System.currentTimeMillis();
			while ((line = in.readLine())!=null) {
				// read that file, see if any of those break points are reached
				if ((System.currentTimeMillis() - time) > 100) {
					return;
				}
				if (!line.startsWith("Breakpoint hit: \"")) continue;
				time = System.currentTimeMillis();
				String classAndMethod = line.split(",")[1].trim();
				String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
				String methodSig = classAndMethod.substring(classAndMethod.lastIndexOf(".")+1, classAndMethod.length());
				int lineNumber = Integer.parseInt(line.split(",")[2].trim().split(" ")[0].split("=")[1]);
				long timeStamp = System.currentTimeMillis();
				JDBStuff.clicksAndBreakPoints.add("BreakPoint," + timeStamp + "," + className + "," + methodSig + "," + lineNumber);
				
			}
			System.out.println("WARNING: BufferedReader ended. This shouldn't have happened.");
		} catch (IOException e) {e.printStackTrace();}
	}
}
