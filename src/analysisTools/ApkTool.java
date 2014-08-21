package analysisTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
	
	private static void extractXMLLayouts(File file) {	// incomplete, parsing is pretty bad
		try {
			File[] layoutFiles = new File(Paths.appDataDir + file.getName() + "/apktool/res/layout/").listFiles();
			for (File layoutFile: layoutFiles) {
				if (!layoutFile.isFile())	continue;
				if (!layoutFile.getName().endsWith(".xml"))	continue;
				String layoutName = layoutFile.getName().substring(0, layoutFile.getName().length()-4);
				File outFile = new File(Paths.appDataDir + file.getName() + "/layout_info/" + layoutName + ".csv");
				outFile.getParentFile().mkdirs();
				PrintWriter out = new PrintWriter(new FileWriter(outFile));
				out.write("");
				BufferedReader in = new BufferedReader(new FileReader(layoutFile));
				in.readLine(); in.readLine(); in.readLine();
				String layoutLine;
				System.out.println(layoutFile.getName());
				while ((layoutLine = in.readLine())!=null) {
					if (layoutLine.startsWith("</"))	continue;
					String widgetType = " ";
					if (layoutLine.indexOf("<")>=0) {
						widgetType = layoutLine.substring(layoutLine.indexOf("<")+1);
						widgetType = widgetType.substring(0, widgetType.indexOf(" "));
					}
					String widgetID = " ";
					if (layoutLine.indexOf("android:id=\"")>0) {
						widgetID = layoutLine.substring(layoutLine.indexOf("android:id=\"@id/")+"android:id=\"@id/".length());
						widgetID = widgetID.substring(0, widgetID.indexOf("\""));
					}
					String onClickMethod = " ";
					if (layoutLine.indexOf("android:onClick=\"")>0) {
						onClickMethod = layoutLine.substring(layoutLine.indexOf("android:onClick=\"")+"android:onClick=\"".length());
						onClickMethod = onClickMethod.substring(0, onClickMethod.indexOf("\""));
					}
					out.write(widgetType + "," + widgetID + "," + onClickMethod+ "\n");
				}
				in.close();
				out.close();
			}
		}	catch (Exception e) {e.printStackTrace();}
	}
	
}
