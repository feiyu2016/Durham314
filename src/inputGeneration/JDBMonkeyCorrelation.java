package inputGeneration;

public class JDBMonkeyCorrelation implements Runnable {
	
	public JDBMonkeyCorrelation()
	{

	}
	
	public void run() 
	{
		getBreakPointsFromClick();
	}
	
	private void getBreakPointsFromClick()
	{
		long timeNoSee;
		try {
			timeNoSee = System.currentTimeMillis();
			while ((System.currentTimeMillis() - timeNoSee) < 5000) {
				
				if(JDBStuff.hitBPfromJDBMonitor.isEmpty()) continue;
				String classAndMethod = JDBStuff.hitBPfromJDBMonitor.get(0).split(",")[1].trim();
				String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
				String methodSig = classAndMethod.substring(classAndMethod.lastIndexOf(".")+1, classAndMethod.length());
				int lineNumber = Integer.parseInt(JDBStuff.hitBPfromJDBMonitor.get(0).split(",")[2].trim().split(" ")[0].split("=")[1]);
				long timeStamp = timeNoSee = System.currentTimeMillis();
				JDBStuff.clicksAndBreakPoints.add(timeStamp + "," + className + "," + methodSig + "," + lineNumber);
				//System.out.println(timeStamp + "," + className + "," + methodSig + "," + lineNumber);
				JDBStuff.hitBPfromJDBMonitor.remove(0);
			}
		} 
		catch (Exception e) {
			System.out.println("Exception");
			e.printStackTrace();
		}
	}
}
