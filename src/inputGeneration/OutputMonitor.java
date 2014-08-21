package inputGeneration;

import java.io.BufferedReader;
import java.io.IOException;

public class OutputMonitor implements Runnable{

	public BufferedReader in;
	
	public OutputMonitor(BufferedReader bR) {
		in = bR;
	}
	
	@Override
	public void run() {
		startsMonitoring();
	}

	private void startsMonitoring() {
		String line;
		try {
			while ((line = in.readLine())!=null)
				{
				// read that file, see if any of those break points are reached
				System.out.println(line);
				if (!line.startsWith("Breakpoint hit: \"")) continue;
				String classAndMethod = line.split(",")[1].trim();
				String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
				String methodSig = classAndMethod.substring(classAndMethod.lastIndexOf(".")+1, classAndMethod.length());
				int lineNumber = Integer.parseInt(line.split(",")[2].trim().split(" ")[0].split("=")[1]);
				long timeStamp = System.currentTimeMillis();
				}
			System.out.println("WARNING: BufferedReader ended. This shouldn't have happened.");
		} catch (IOException e) {e.printStackTrace();}

	}
	
}
