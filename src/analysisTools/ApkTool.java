package analysisTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import main.Paths;

public class ApkTool {
	
	public static void extractAPK(File file) {
		try {
			System.out.println("\n-- apktool starts, target file: " + file.getAbsolutePath());
			
			String outDir = Paths.appDataDir + file.getName() + "/apktool";
			Process pc = Runtime.getRuntime().exec("java -jar " + Paths.apktoolPath +
													" d" + " -f" +
													" " + file.getAbsolutePath() +
													" " + outDir);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			BufferedReader in_err = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
			String line;
			while ((line = in.readLine())!=null)	System.out.println(line);
			while ((line = in_err.readLine())!=null)	System.out.println(line);
			in.close();
			in_err.close();
			System.out.println("-- apktool done.");
			System.out.println("-- processing XMLs...");
			//extractXMLLayouts(file);
			System.out.println("-- layout information extracted.");
		}	catch (Exception e ) {e.printStackTrace();}
	}
	
}
