package john.runtimeValidation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import zhen.version1.component.Event;
import zhen.version1.framework.Common;

public class GenerateValidationScripts {
	
	private final String click = "MonkeyDevice.DOWN_AND_UP";
	
	private String scriptName;
	private String packageName;
	private String activityName;
	private String deviceID;
	
	private ArrayList<ArrayList<Event>> eventSequences;
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
			writer = new PrintWriter(ScriptPath.scriptPath + scriptName + "_" + sequenceNumber + ".seq", "UTF-8");
			sequenceNumber++;
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("Exception while creating file.");
			e.printStackTrace();
		}
	}
	
	private void closeFile()
	{
		if (writer != null)
			writer.close();
	}
	
	public void setEventSequences(ArrayList<ArrayList<Event>> eventSequences)
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
			for(ArrayList<Event> sequence : eventSequences){
				createFile();
				//writer.println("# " + method);
				//generateScriptTemplate();
				
				for (Event event : sequence) {
					try {
						String x = event.getValue(Common.event_att_click_x).toString();
						String y = event.getValue(Common.event_att_click_y).toString();
						writer.println(x + "," + y);
					} catch (Exception e) {}
				}
				//writer.println("device.shell('am force-stop " + packageName + "')");
				closeFile();
			}
		} finally {}
	}
}
