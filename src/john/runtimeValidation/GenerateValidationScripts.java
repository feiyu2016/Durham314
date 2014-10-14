package john.runtimeValidation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class GenerateValidationScripts {
	
	private final String click = "MonkeyDevice.DOWN_AND_UP";
	
	private String scriptName;
	private String packageName;
	private String activityName;
	private String deviceID;
	
	private ArrayList<String> eventSequences;
	private boolean eventSequencesAreSet = false;
	
	private int sequenceNumber;

	private PrintWriter writer;
	
	public GenerateValidationScripts(String scriptName, String packageName, String mainActivityName, String deviceID)
	{
		this.scriptName = scriptName;
		this.packageName = packageName;
		this.activityName = mainActivityName;
		this.deviceID = deviceID;
		sequenceNumber = 0;
	}
	
	private void createFile()
	{
		try {
			writer = new PrintWriter(ScriptPath.scriptPath + scriptName + "_" + sequenceNumber + ".py", "UTF-8");
			sequenceNumber++;
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("Exception while creating file.");
			e.printStackTrace();
		}
	}
	
	private void generateScriptTemplate()
	{
		if (writer == null) {
			System.out.println("No File Opened.");
			return;
		}
		else {
			writer.println("#!/usr/bin/python");
			writer.println("from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice");
			writer.println("device = MonkeyRunner.waitForConnection(10.0,'" + deviceID + "')");
//			writer.println("package = '" + packageName + "'");
//			writer.println("activity = '" + activityName + "'");
//			writer.println("runComponent = package + '/.' + activity");
//			writer.println("device.startActivity(component=runComponent)");
			writer.println("MonkeyRunner.sleep(1)");
		}
	}
	
	private void closeFile()
	{
		if (writer != null)
			writer.close();
	}
	
	public void setEventSequences(ArrayList<String> eventSequences)
	{
		this.eventSequences = eventSequences;
		this.eventSequencesAreSet = true;
	}
	
	public void generateScripts() throws IOException
	{
		if (!this.eventSequencesAreSet) {
			System.out.println("Event sequences were not set.");
			return;
		}
			
		try {
			for(String sequence : eventSequences){
				createFile();
				//writer.println("# " + method);
				generateScriptTemplate();
				
				String string[] = sequence.trim().split("\\|");
				
				for (int i = 0; i < string.length; i++) {
					String x = string[i].trim().split("\\,")[0];
					String y = string[i].trim().split("\\,")[1];
					
					writer.println("device.touch(" + x + "," + y + "," + click + ")");
					writer.println("MonkeyRunner.sleep(1)");
				}
				//writer.println("device.shell('am force-stop " + packageName + "')");
				closeFile();
			}
		} finally {}
	}
}
