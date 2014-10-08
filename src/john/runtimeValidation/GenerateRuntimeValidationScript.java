package john.runtimeValidation;

import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import zhen.version1.framework.Framework;
import zhen.version1.component.UIModelGraph;
import zhen.version1.component.Event;

public class GenerateRuntimeValidationScript {
	
	public UIModelGraph targetEventSequences;
	
	private final String click = "MonkeyDevice.DOWN_AND_UP";
	private final String restart = "device.startActivity(component=runComponent)";
	
	private Framework thisFW;
	private String thisScriptName;
	private String thisPackageName;
	private String thisActivityName;
	
	private int sequenceNumber;

	private PrintWriter writer;
	
	public GenerateRuntimeValidationScript(Framework fw, String scriptName, String packageName, String activityName) {
		thisFW = fw;
		thisScriptName = scriptName;
		thisPackageName = packageName;
		thisActivityName = activityName;
		sequenceNumber = 0;
	}
	
	private void createFile()
	{
		try {
			writer = new PrintWriter(ScriptPath.scriptPath + thisScriptName + "_" + sequenceNumber + ".py", "UTF-8");
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
			writer.println("device = MonkeyRunner.waitForConnection()");
			writer.println("package = '" + thisPackageName + "'");
			writer.println("activity = '" + thisActivityName + "'");
			writer.println("runComponent = package + '/' + activity");
			writer.println("device.startActivity(component=runComponent)");
			writer.println("MonkeyRunner.sleep(1)");
		}
	}
	
	private void closeFile()
	{
		if (writer != null)
			writer.close();
	}
	
	public void generateScripts() throws IOException
	{
		String[] methods = (String[]) thisFW.getAttributes().get("methods");
		for(String method : methods){
			
			List<List<Event>>  llevent = thisFW.rInfo.findPotentialPathForHandler(method);
			
			
				
			for(List<Event> levent: llevent) {
				
				try {
					createFile();
					writer.println("# " + method);
					generateScriptTemplate();
//					writer.println("device.shell('am force-stop " + globalPackageName + "')");
//					writer.println(restart);
//					writer.println("MonkeyRunner.sleep(1)");
					for(Event event: levent) {
						String string = "";
						String temp = "";
						String x = "";
						String y = "";
						
						string = event.toString();
						
						if (!string.startsWith("android:") || string.isEmpty()) continue;
						
						temp = string.trim().split("\\(")[1];
						x = temp.trim().split("\\,")[0];
						y = temp.trim().split("\\,")[1];
						y = y.trim().split("\\)")[0];
							
						writer.println("device.touch(" + x + "," + y + "," + click + ")");
						writer.println("MonkeyRunner.sleep(1)");
						
					}
					
					writer.println("MonkeyRunner.sleep(1)");
					writer.println("device.shell('am force-stop " + thisPackageName + "')");
				} finally {
					closeFile();
				}
			}	
		}
	}

}

