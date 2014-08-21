package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import main.Paths;

public class StaticInfo {
	
	public static ArrayList<String> getClassNames(File file) {
		ArrayList<String> results = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + ));
		}	catch (Exception e) {e.printStackTrace();}
		return results;
	}
	
	
	public static String getPackageName(File file) {
		String result = "";
		try {
			File manifestFile = new File(Paths.appDataDir + file.getName() + "/apktool/AndroidManifest.xml");
			BufferedReader in = new BufferedReader(new FileReader(manifestFile));
			boolean found = false;
			while (!found) {
				String line = in.readLine();
				if (line.startsWith("<manifest")) {
					line = line.substring(line.indexOf("package=\"")+"package=\"".length());
					line = line.substring(0, line.indexOf("\""));
					result = line;
					found = true;
				}
			}
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static String getMainActivityName(File file) {
		return getActivityNames(file).get(0);
	}
	
	
	public static ArrayList<String> getActivityNames(File file) {
		ArrayList<String> results = new ArrayList<String>();
		File manifestFile = new File(Paths.appDataDir + file.getName() + "/apktool/AndroidManifest.xml");
		String whole = readDatFile(manifestFile);
		String activitySection = whole.substring(whole.indexOf("<application"), whole.indexOf("</application>"));
		// first step: get each block of activity information
		int start = activitySection.indexOf("<activity");
		int end = activitySection.indexOf("</activity>");
		while (start!=-1 && end!=-1) {
			results.add(activitySection.substring(start, end));
			activitySection = activitySection.substring(end + "</activity>".length());
			start = activitySection.indexOf("<activity");
			end = activitySection.indexOf("</activity>");
		}
		// second step: get activity name from each block information
		int mainActivityPos = 0;
		for (int i = 0; i < results.size(); i++) {
			String thisActivity = results.get(i).trim();
			String[] lines = thisActivity.trim().split("\n");
			// line 0 gets the activity name, line 2 gets first action name
			System.out.println(thisActivity);
			String activityName = lines[0].substring(lines[0].indexOf("android:name=\"")+"android:name=\"".length());
			activityName = activityName.substring(0, activityName.indexOf("\""));
			String actionName = lines[2].substring(lines[2].indexOf("<action android:name=\"")+"<action android:name=\"".length() , lines[2].lastIndexOf("\" />"));
			if (actionName.equals("android.intent.action.MAIN"))	mainActivityPos = i;
			String packageName = getPackageName(file);
			if (activityName.startsWith("."))	activityName = activityName.substring(1, activityName.length());
			if (!activityName.startsWith(packageName))	activityName = packageName + "." + activityName;
			if (validateActivity(file,activityName))
				results.set(i, activityName);
			else System.out.println("Error: activity " + activityName + " doesn't exist!");
		}
		// put MainActivity in the first slot
		if (mainActivityPos!=0) {
			String temp = results.get(mainActivityPos);
			results.set(mainActivityPos, results.get(0));
			results.set(0, temp);
		}
		return results;
	}
	
	private static boolean validateActivity(File file, String activityName) {
		// the activity name in AndroiManifest might be false, e.g., zhiming's Bugatti and bug
		boolean result = false;
		File classFile = new File(Paths.appDataDir + file.getName() + "/apktool/smali/" + activityName.replace(".", "/") + ".smali");
		if (classFile.exists())	result = true;
		return result;
	}
	
	public static ArrayList<String> getLeavingWidgets(File file, String activityName, String layoutName) {
		ArrayList<String> results = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/layout_info/" + layoutName + ".csv"));
			String line;
			while ((line = in.readLine())!=null) {
				String widgetType = line.split(",")[0];
				String widgetID = line.split(",")[1];
				String onClick = line.split(",")[2];
				if (widgetID.equals(" "))	continue;
				if (onClick.equals(" "))
					continue;	// need to add: search through the code and see if onClick was added in the code
				String onClickSubSig = onClick + "(android.view.View)";
				if (methodChangesLayoutOrActivity(onClickSubSig).equals("layout"))	results.add(widgetID);
				else if (methodChangesLayoutOrActivity(onClickSubSig).equals("activity"))	results.add(widgetID);
			}
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		return results;
	}
	
	public static ArrayList<String> getStayingWidgets() {
		ArrayList<String> results = new ArrayList<String>();
		
		return results;
	}
	
	private static String methodChangesLayoutOrActivity(String halfSignature) {
		String result = "NO";
		
		
		return result;
	}
	
	public static void process_Intents_And_setContentView(File file) {
		File[] classFolders = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/").listFiles();
		try {
			for (File classFolder: classFolders) {
				File[] methodJimples = classFolder.listFiles();
				String className = classFolder.getName();
				for (File methodJimple: methodJimples) {
					if (!methodJimple.getName().endsWith(".jimple"))	continue;
					String methodFileName = methodJimple.getName().substring(0, methodJimple.getName().length()-7);
					BufferedReader in_mJ = new BufferedReader(new FileReader(methodJimple));
					int lineNumber = 1;
					String line;
					while ((line = in_mJ.readLine())!=null) {
						if (line.contains("void startActivity(android.content.Intent)>") && line.contains("virtualinvoke"))
							solveIntent(file, className, methodFileName, lineNumber);
						else if (line.contains("void setContentView(int)>") && line.contains("virtualinvoke"))
							solveSetContentView(file, className, methodFileName, lineNumber);
						lineNumber++;
					}
					in_mJ.close();
				}
			}
		}	catch (Exception e) {e.printStackTrace();}
	}
	
	private static void solveIntent(File file, String className, String methodFileName, int lineNumber) throws Exception{
		BufferedReader in_mJ = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/" + methodFileName + ".jimple"));
		
		in_mJ.close();
	}
	
	private static void solveSetContentView(File file, String className, String methodFileName, int lineNumber) throws Exception{
		
	}
	
	private static String readDatFile(File file) {
		String result = "", currentLine = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while ((currentLine = in.readLine())!=null)
				result+=currentLine+"\n";
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
}
