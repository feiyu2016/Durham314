package john.runtimeValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import main.Paths;

public class RuntimeValidation {
	
	private String thisScriptName;
	
	private Process pc;
	
	public RuntimeValidation(String scriptName) {
		thisScriptName = scriptName;
	}
	
	private ArrayList<String> getAllScripts()
	{
		File directory = new File(ScriptPath.scriptPath);  
		File[] files = directory.listFiles(); 
		ArrayList<String> ret = new ArrayList<String>();
		  
		for (File file: files)  {  
		   String string = file.toString();
		   
		  if (string.contains(thisScriptName))
			   ret.add(string); 
		}
		
		return ret;
	}
	
	public void runAllScripts()
	{
		ArrayList<String> scripts = getAllScripts();
		
		try {
			for (String script:scripts) {
				pc = Runtime.getRuntime().exec(Paths.androidToolPath + "monkeyrunner " + script);
				pc.waitFor();
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Couldn't exec monkeyrunner.");
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		new RuntimeValidation("handleOperation").runAllScripts();
	}
}
