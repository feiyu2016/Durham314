package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import main.Paths;

public class OldStaticInfo {

	public static String getDefaultLayout(File file, String activityName) {
		String result = "";
		try {
			File intentFile = new File(Paths.appDataDir+ file.getName() + "/Activities/Intents.csv");
			// 1st step, get activity's onCreate layout name
			BufferedReader in_Intent = new BufferedReader(new FileReader(intentFile));
			String line_Intent;
			while ((line_Intent = in_Intent.readLine())!=null)
				if (line_Intent.startsWith("setContentView,onCreate," + activityName + ",")) {
					result = line_Intent.split(",")[3];
					break;
				}
			in_Intent.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static String getOnClickMethodName(File file, String layoutName, String widgetID) {
		String result = " ";
		try {
			BufferedReader in_layout = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/Activities/Layouts/" + layoutName + ".csv"));
			String line_layout;
			while ((line_layout = in_layout.readLine())!=null) {
				String thisWidgetID = line_layout.split(",")[1];
				String onClick = line_layout.split(",")[2];
				if (thisWidgetID.equals(widgetID))	result = onClick;
			}
			in_layout.close();
		}	catch (Exception e ) {e.printStackTrace();}
		return result;
	}
	
	public static ArrayList<String> getStayingWidgets(File file, String layoutName) {
		ArrayList<String> results = new ArrayList<String>();
		if (layoutName.equals(""))	return results;
		try {
			BufferedReader in_layout = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/Activities/Layouts/" + layoutName + ".csv"));
			String line_layout;
			while ((line_layout = in_layout.readLine())!=null) {
				String widgetID = line_layout.split(",")[1];
				String onClick = line_layout.split(",")[2];
				
				if (widgetID.equals(" "))	continue;
				if (onClick.equals(" "))	{
					//results.add(line_layout); 
					continue;}
				BufferedReader in_Intent = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/Activities/Intents.csv"));
				String line_Intent;
				boolean isLeaving = false;
				while ((line_Intent = in_Intent.readLine())!=null) {
					if (line_Intent.startsWith("Intent,"))	{
						if (layoutName.equals(line_Intent.split(",")[2]) && onClick.equals(line_Intent.split(",")[5]))
							isLeaving = true;
						continue;
					}
					if (!line_Intent.split(",")[1].equals("onClick"))	continue;
					if (layoutName.equals(line_Intent.split(",")[3]) && onClick.equals(line_Intent.split(",")[6])) isLeaving = true;
				}
				if (!isLeaving)	results.add(line_layout);
				in_Intent.close();
			}
			in_layout.close();
		}	catch (Exception e) {e.printStackTrace();}
		return results;
	}
	
	public static ArrayList<String> getLeavingWidgets(File file, String layoutName) {
		ArrayList<String> results = new ArrayList<String>();
		if (layoutName.equals(""))	return results;
		try {
			BufferedReader in_layout = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/Activities/Layouts/" + layoutName + ".csv"));
			String line_layout;
			while ((line_layout = in_layout.readLine())!=null) {
				String widgetID = line_layout.split(",")[1];
				String onClick = line_layout.split(",")[2];
				if (widgetID.equals(" "))	continue;
				if (onClick.equals(" "))	continue;
				BufferedReader in_Intent = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/Activities/Intents.csv"));
				String line_Intent;
				while ((line_Intent = in_Intent.readLine())!=null) {
					if (line_Intent.startsWith("setContentView,onClick,"))	{
						//format: setContentView,onCreate,activityName,layoutName
						//	  or: setContentView,onClick,srcActivity,layOut,widgetType,widgetID,onClickMethod,layoutName
						if (layoutName.equals(line_Intent.split(",")[3]) && onClick.equals(line_Intent.split(",")[6]))
							results.add("newLayout," + line_layout + "," + line_Intent.split(",")[7]);
					} else if (line_Intent.startsWith("Intent,")) {
						//format: Intent,srcActivity,layOut,widgetType,widgetID,onClickMethod,targetActivity
						if (layoutName.equals(line_Intent.split(",")[2]) && onClick.equals(line_Intent.split(",")[5]))
							results.add("newActivity," + line_layout + "," + line_Intent.split(",")[6]);
					}
					
				}
				in_Intent.close();
			}
			in_layout.close();
		}	catch (Exception e) {e.printStackTrace();}
		return results;
	}

	public static void getIntentInfo(File file) {
		try {
			BufferedReader in_apkInfo = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/ApkInfo.csv"));
			PrintWriter out_widgets = new PrintWriter(new FileWriter(Paths.appDataDir + file.getName() + "/Activities/Intents.csv"));
			in_apkInfo.readLine();
			String line_apkInfo;
			while ((line_apkInfo = in_apkInfo.readLine())!=null) {
				String className = line_apkInfo.split(",")[1];
				if (className.startsWith("android.support.v")) continue;
				BufferedReader in_classInfo = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/ClassInfo.csv"));
				int fieldCount = Integer.parseInt(in_classInfo.readLine().split(",")[1]);
				for (int i = 0; i < fieldCount; i++)	in_classInfo.readLine();
				String line_classInfo;
				while ((line_classInfo = in_classInfo.readLine())!=null) {
					if (line_classInfo.split(",")[0].equals("AbstractMethod") || line_classInfo.split(",")[0].equals("NativeMethod"))	continue;
					String methodFileName = line_classInfo.split(",")[4];
					String methodSubSig = line_classInfo.split(",")[5];
					String methodName = line_classInfo.split(",")[1];
					File methodJimpleFile = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/" + methodFileName + ".jimple");
					BufferedReader in_methodJimple = new BufferedReader(new FileReader(methodJimpleFile));
					String line_methodJimple;
					int jimpleLineNo = 1;
					while ((line_methodJimple = in_methodJimple.readLine())!=null) {
						if (line_methodJimple.contains("virtualinvoke") && line_methodJimple.contains("void startActivity(android.content.Intent)>")) {
							String targetActivity = "";
							String intentName = line_methodJimple.substring(line_methodJimple.lastIndexOf(")>(")+3, line_methodJimple.lastIndexOf(")"));
							String targetClass = findIntentTargetClass(intentName, jimpleLineNo, methodJimpleFile)[0];
							int currentLine = Integer.parseInt(findIntentTargetClass(intentName, jimpleLineNo, methodJimpleFile)[1]);
							while (targetClass.startsWith("$")) {
								String lastAssignStmt = findLastAssignStmt(targetClass, currentLine, methodJimpleFile)[0];
								currentLine = Integer.parseInt(findLastAssignStmt(targetClass, currentLine, methodJimpleFile)[1]);
								if (lastAssignStmt.contains("staticinvoke <java.lang.Class: java.lang.Class forName("))
									targetClass = lastAssignStmt.substring(lastAssignStmt.lastIndexOf(")>(")+3, lastAssignStmt.lastIndexOf(")"));
							}
							// found an intent that triggers targetClass activity
							if (targetClass.indexOf("\"")>=0 && targetClass.lastIndexOf("\"")>0 && targetClass.lastIndexOf("\"")>targetClass.indexOf("\"")) {
								targetClass = targetClass.substring(targetClass.indexOf("\"")+1, targetClass.lastIndexOf("\"")).replace("/", ".");
								// check if methodName = one of the onClick event, and className is the right activity
								// if yes, then we got a match
								// if no, get callgraph to see every possible methods that can call this method, and collect all the onClick method from them
								// right now do not touch code generated widgets, only xml widgets
								ArrayList<String> widgetsThatUsesThisMethodAsOnClick = matchWidgetOnClickMethods(methodName, className, file);
								for (int i = 0; i < widgetsThatUsesThisMethodAsOnClick.size(); i++) {
									//format: Intent,srcActivity,layOut,widgetType,widgetID,onClickMethod,targetActivity
									out_widgets.write("Intent," + widgetsThatUsesThisMethodAsOnClick.get(i) + "," + targetClass + "\n");
								}
								
							}
							//String 
						}
						// search for setContentView
						if (line_methodJimple.contains("virtualinvoke") && line_methodJimple.contains("void setContentView(int)>")) {
							String viewString = line_methodJimple.substring(line_methodJimple.lastIndexOf("(")+1, line_methodJimple.lastIndexOf(")"));
							if (viewString.contains("$")) {System.out.println("can't find view ID for this statement(" + className + "): " + line_methodJimple); continue;};
							viewString = Integer.toHexString(Integer.parseInt(viewString));
							while (viewString.length()<8)	viewString = viewString + "0";
							viewString = "id=\"0x" + viewString + "\"";
							BufferedReader in_viewID = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/apktool/res/values/public.xml"));
							String viewLine;
							while ((viewLine = in_viewID.readLine())!=null) {
								// look at res/values/public.xml, use view id to get layout name
								if (!viewLine.contains("<public type=\"layout\"")) continue;
								if (viewLine.contains(viewString)) {
									String layoutName = viewLine.substring(viewLine.indexOf("name=\"")+"name=\"".length());
									layoutName = layoutName.substring(0, layoutName.indexOf("\""));
									//after finding setContentView, see it's in onCreate or others
									//format: setContentView,onCreate,activityName,layoutName
									//	  or: setContentView,onClick,srcActivity,layOut,widgetType,widgetID,onClickMethod,layoutName
									if (methodName.equals("onCreate"))	out_widgets.write("setContentView,onCreate," + className + "," + layoutName + "\n");
									else {
										ArrayList<String> widgetsThatUsesThisMethodAsOnClick = matchWidgetOnClickMethods(methodName, className, file);
										for (int i = 0; i < widgetsThatUsesThisMethodAsOnClick.size(); i++) {
											out_widgets.write("setContentView,onClick," + widgetsThatUsesThisMethodAsOnClick.get(i) + "," + layoutName + "\n");
										}
									}
									break;
								}
							}
							in_viewID.close();
						}
						
						jimpleLineNo++;
					}
					in_methodJimple.close();
				}
				in_classInfo.close();
			}
			in_apkInfo.close();
			out_widgets.close();
		}	catch (Exception e) {e.printStackTrace();}
	}
	//LineB(0,1,2,3,4,5,6,7):	"MethodCall", srcClass name, srcMethod name, tgtClass name, tgtMethod Subsignature, stmt, line number, column number
	public static ArrayList<String> findWidgetsOnClickThatMightCallThisMethod_fromCallGraph(String methodName, String className, File file) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			BufferedReader in_CG = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/CallGraph.csv"));
			String line_CG;
			while ((line_CG = in_CG.readLine())!=null) {
				if (!line_CG.startsWith("MethodCall"))	continue;
				if (line_CG.split(",")[1].startsWith("android.support.v"))	continue;
				String srcClass = line_CG.split(",")[1];
				String srtMethod = line_CG.split(",")[2];
				String tgtClass = line_CG.split(",")[3];
				String tgtMethodSubSig = line_CG.split(",")[4];
				
			}
			in_CG.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static ArrayList<String> matchWidgetOnClickMethods(String methodName, String className, File file) {
		ArrayList<String> result = new ArrayList<String>();
		// if the class is not an activity, it's impossible to be onClick method
		File activityFile = new File(Paths.appDataDir + file.getName() + "/Activities/" + className + ".csv");
		if (!activityFile.exists())	return null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(activityFile));
			String line;
			while ((line = in.readLine())!=null) {
				String layout = line.split(",")[0];
				String widgetType = line.split(",")[1];
				String widgetID = line.split(",")[2];
				String onClick = line.split(",")[3];
				if (onClick.equals(methodName))
					result.add(className + "," + line);
			}
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static String[] findLastAssignStmt(String localName, int beforeThisLine, File methodJimpleFile) {
		String[] result = new String[2];
		int lineNo = 1;
		try {
			BufferedReader in_methodJimple = new BufferedReader(new FileReader(methodJimpleFile));
			while (lineNo < beforeThisLine) {
				String line = in_methodJimple.readLine();
				if (line.contains(localName + " = "))
					{	result[0] = line;	result[1] = String.valueOf(lineNo); }
				lineNo++;
			}
			in_methodJimple.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static String[] findIntentTargetClass(String intentName, int beforeThisLine, File methodJimpleFile) {
		String[] result = new String[2];
		int lineNo = 1;
		try {
			BufferedReader in_methodJimple = new BufferedReader(new FileReader(methodJimpleFile));
			while (lineNo < beforeThisLine) {
				String line = in_methodJimple.readLine();
				if (line.contains("specialinvoke $r3.<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>"))
					{	result[0] = line.substring(line.lastIndexOf(", ")+2, line.lastIndexOf(")")); result[1] = String.valueOf(lineNo); }
				lineNo++;
			}
			in_methodJimple.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	
	// this will output a file containing all the widgets in each layout/activity
	public static void getLayoutInfo(File file) {
		try {
			ArrayList<String> activitiesInfo = getActivitiesInfo(file);
			for (int i = 0; i < activitiesInfo.size(); i++) {
				String singleActivity = activitiesInfo.get(i);
				String className = singleActivity.split(",")[0].split("/")[1];
				File jimpleFile = new File(Paths.appDataDir + file.getName() + "/Jimples/" + className + ".jimple");
				if (!jimpleFile.exists()) {System.out.println("can't find class file: " + className); continue;}
				BufferedReader in = new BufferedReader(new FileReader(jimpleFile));
				File outFile = new File(Paths.appDataDir + file.getName() + "/Activities/" + className + ".csv");
				outFile.getParentFile().mkdirs();
				PrintWriter out = new PrintWriter(new FileWriter(outFile));
				String line;
				while ((line = in.readLine())!=null) {
					// look for setContentView in each Activity class file, obtain the view id
					if (!line.contains("setContentView"))	continue;
					String viewString = line.substring(line.lastIndexOf("(")+1, line.lastIndexOf(")"));
					if (viewString.contains("$")) {System.out.println("can't find view ID for this statement(" + className + "): " + line); continue;};
					viewString = Integer.toHexString(Integer.parseInt(viewString));
					while (viewString.length()<8)	viewString = viewString + "0";
					viewString = "id=\"0x" + viewString + "\"";
					BufferedReader in_viewID = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/apktool/res/values/public.xml"));
					String viewLine;
					while ((viewLine = in_viewID.readLine())!=null) {
						// look at res/values/public.xml, use view id to get layout name
						if (!viewLine.contains("<public type=\"layout\"")) continue;
						if (viewLine.contains(viewString)) {
							String layoutName = viewLine.substring(viewLine.indexOf("name=\"")+"name=\"".length());
							layoutName = layoutName.substring(0, layoutName.indexOf("\""));
							File layoutFile = new File(Paths.appDataDir + file.getName() + "/apktool/res/layout/" + layoutName + ".xml");
							BufferedReader in_layout = new BufferedReader(new FileReader(layoutFile));
							File layoutOutFile = new File(Paths.appDataDir + file.getName() + "/Activities/Layouts/" + layoutName + ".csv");
							PrintWriter out_layout = new PrintWriter(new FileWriter(layoutOutFile));
							out_layout.write("");
							in_layout.readLine(); in_layout.readLine(); in_layout.readLine();
							String layoutLine;
							while ((layoutLine = in_layout.readLine())!=null) {
								// read layout xml, get id and onClick of each widget
								if (layoutLine.startsWith("</"))	continue;
								String widgetType = " ";
								if (layoutLine.indexOf("<")>=0) {
									widgetType = layoutLine.substring(layoutLine.indexOf("<")+1);
									widgetType = widgetType.substring(0, widgetType.indexOf(" "));
								}
								String widgetID = " ";
								if (layoutLine.indexOf("android:id=\"")>=0) {
									widgetID = layoutLine.substring(layoutLine.indexOf("android:id=\"@id/")+"android:id=\"@id/".length());
									widgetID = widgetID.substring(0, widgetID.indexOf("\""));
								}
								String onClickMethod = " ";
								if (layoutLine.indexOf("android:onClick=\"")>=0) {
									onClickMethod = layoutLine.substring(layoutLine.indexOf("android:onClick=\"")+"android:onClick=\"".length());
									onClickMethod = onClickMethod.substring(0, onClickMethod.indexOf("\""));
								}
								out.write(layoutName + "," + widgetType + "," + widgetID + "," + onClickMethod+ "\n");
								out_layout.write(widgetType + "," + widgetID + "," + onClickMethod+ "\n");
							}
							in_layout.close();
							out_layout.close();
							break;
						}
					}
					in_viewID.close();
				}
				in.close();
				out.close();
			}
		}	catch (Exception e) {e.printStackTrace();}
	}
	
	// read AndroidManifest.xml, get activity info: (Name1,Action1), (Name2,Action2), ...
	public static ArrayList<String> getActivitiesInfo(File file) {
		ArrayList<String> results = new ArrayList<String>();
		File manifestFile = new File(Paths.appDataDir+file.getName()+"/apktool/AndroidManifest.xml");
		String whole = readDatFile(manifestFile);
		String activitySection = whole.substring(whole.indexOf("<application"), whole.indexOf("</application>"));
		int start = activitySection.indexOf("<activity");
		int end = activitySection.indexOf("</activity>");
		while (start!=-1 && end!=-1) {
			results.add(activitySection.substring(start, end));
			activitySection = activitySection.substring(end + "</activity>".length());
			start = activitySection.indexOf("<activity");
			end = activitySection.indexOf("</activity>");
		}
		for (int i = 0; i < results.size(); i++) {
			String thisActivity = results.get(i).trim();
			String[] lines = thisActivity.split("\n");
			// line 1 gets the activity name, line 2 gets action name
			String activityName = lines[0].substring(lines[0].indexOf("android:name=\"")+"android:name=\"".length(), lines[0].lastIndexOf("\">"));
			String actionName = lines[2].substring(lines[2].indexOf("<action android:name=\"")+"<action android:name=\"".length() , lines[2].lastIndexOf("\" />"));
			String packageName = getPackageName(file);
			if (activityName.startsWith("."))	activityName = activityName.substring(1, activityName.length());
			if (!activityName.startsWith(packageName))	activityName = packageName + "/" + packageName + "." + activityName;
			else activityName = packageName + "/" + activityName;
			results.set(i, activityName + "," + actionName);
		}
		return results;
	}
	
	public static String getMainActivity(File file) {
		String result = "";
		ArrayList<String> activityList = getActivitiesInfo(file);
		for (int i = 0; i < activityList.size(); i++) {
			String thisActivity = activityList.get(i);
			if (thisActivity.endsWith(",android.intent.action.MAIN"))
				result = thisActivity.split(",")[0].split("/")[1];
		}
		System.out.println("mainactivity= " + result);
		return result;
	}
	
	public static String getPackageName(File file) {
		String result = "";
		try {
			File manifestFile = new File(Paths.appDataDir+file.getName()+"/apktool/AndroidManifest.xml");
			BufferedReader in = new BufferedReader(new FileReader(manifestFile));
			boolean found = false;
			while (!found) {
				String line = in.readLine();
				if (line.startsWith("<manifest")) {
					line = line.substring(line.indexOf("package=\"")+"package=\"".length());
					line = line.substring(0, line.indexOf("\""));
					found = true;
					result = line;
				}
			}
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		
		
		return result;
	}
	
	public static String readDatFile(File file) {
		String result = "", currentLine = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while ((currentLine = in.readLine())!=null)
				result+=currentLine+"\n";
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static void apktool(String filePath, String outPath) {
		try {
			Process pc = Runtime.getRuntime().exec("java -jar /home/wenhaoc/libs/apktool.jar d -f " + filePath + " " + outPath);
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			BufferedReader in_err = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
			String line;
			while ((line = in.readLine())!=null) 		{	System.out.println(line);	}
			while ((line = in_err.readLine())!=null) 	{	System.out.println(line);	}
		}	catch (Exception e) {e.printStackTrace();}
	}
	
	public static int getSrcLineNumber(File file, String className, String methodSubSig, int methodLineNumber) {
		int result = -1;
		
		
		
		return result;
	}
	
}
