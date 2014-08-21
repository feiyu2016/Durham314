package inputGeneration;

import java.io.OutputStream;

import main.Paths;

public class ParseLines {
	
	//private String folderName;
	private Process pc;
	private OutputStream out;
	
	public void getFileNames(String pathToFolder) throws Exception {
		pc = Runtime.getRuntime().exec("grep -rl \"\\.lines\"" + pathToFolder + "/*");
		pc.waitFor();
		
		out = pc.getOutputStream();
	}
}
