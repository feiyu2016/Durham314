package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import main.Paths;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StaticInfo {
	
	private static ArrayList<Layout> layoutList = new ArrayList<Layout>();
	private static ArrayList<String> UIChanges = new ArrayList<String>();
	
	public static ArrayList<Layout> getLayoutList(File file) {
		return layoutList;
	}
	
	public static ArrayList<String> getUIChanges(File file) {
		return UIChanges;
	}
	
	public static ArrayList<String> getAllMethodSignatures(File file, String className) {
		// returns all method signature within a class (jimple format)
		// e.g.   void myMethod(java.lang.String int java.lang.String)
		ArrayList<String> results = new ArrayList<String>();
		try {
			File classInfoFile = new File(Paths.appDataDir + file.getName() + "/" + className + "/ClassInfo.csv");
			if (!classInfoFile.exists())	{
				System.out.println("can't find ApkInfo file! Did you call 'analysisTools.Soot.generateAPKData(file)' before this?");
				return results;
			}
			BufferedReader in = new BufferedReader(new FileReader(classInfoFile));
			int fieldCount = Integer.parseInt(in.readLine().split(",")[1]);
			for (int i = 0; i < fieldCount; i++) in.readLine();
			String line;
			while ((line = in.readLine())!=null)	results.add(line.split(",")[5]);
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		return results;
	}
	
	
	public static ArrayList<String> getClassNames(File file) {
		// return all class names within an app
		ArrayList<String> results = new ArrayList<String>();
		try {
			File apkInfoFile = new File(Paths.appDataDir + file.getName() + "/ApkInfo.csv");
			if (!apkInfoFile.exists())	{
				System.out.println("can't find ApkInfo file! Did you call 'analysisTools.Soot.generateAPKData(file)' before this?");
				return results;
			}
			BufferedReader in = new BufferedReader(new FileReader(apkInfoFile));
			in.readLine();
			String line;
			while ((line = in.readLine())!=null)
				results.add(line.split(",")[1]);
			in.close();
		}	catch (Exception e) {e.printStackTrace();}
		return results;
	}
	
	public static boolean hasClass(File file, String className) {
		boolean result = false;
		ArrayList<String> classNames = getClassNames(file);
		for (String c: classNames)
			if (c.equals(className))
				result = true;
		return result;
	}
	
	public static String getPackageName(File file) {
		String result = "";
		File manifestFile = new File(Paths.appDataDir + file.getName() + "/apktool/AndroidManifest.xml");
		if (!manifestFile.exists()) {
			System.out.println("can't find /apktool/AndroidManifest.xml! Run apktool first.");
			return result;
		}
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestFile);
			Node manifestNode = doc.getFirstChild();
			result = manifestNode.getAttributes().getNamedItem("package").getNodeValue();
		} catch (Exception e) {	e.printStackTrace();}		
		return result;
	}

	public static boolean isActivity(File file, String className) {
		boolean result = false;
		ArrayList<String> aa = getActivityNames(file);
		for (String a: aa)
			if (a.equals(className))
				result = true;
		return result;
	}
	
	public static String getMainActivityName(File file) {
		return getActivityNames(file).get(0);
	}
	
	public static ArrayList<String> getActivityNames(File file) {
		ArrayList<String> results = new ArrayList<String>();
		File manifestFile = new File(Paths.appDataDir + file.getName() + "/apktool/AndroidManifest.xml");
		if (!manifestFile.exists()) {
			System.out.println("can't find /apktool/AndroidManifest.xml! Run apktool first.");
			return results;
		}
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestFile);
			doc.getDocumentElement().normalize();
			NodeList activityList = doc.getElementsByTagName("activity");
			for (int i = 0; i < activityList.getLength(); i++) {
				Node activityNode = activityList.item(i);
				String activityName = activityNode.getAttributes().getNamedItem("android:name").getNodeValue();
				if (activityName.startsWith("."))	
					activityName = activityName.substring(1, activityName.length());
				if (!activityName.startsWith(StaticInfo.getPackageName(file)))
					activityName = StaticInfo.getPackageName(file) + "." + activityName;
				if (validateActivity(file, activityName))
					results.add(activityName);
			}
			// check if it's main activity
			String mainActvtName = "";
			NodeList actionList = doc.getElementsByTagName("action");
			for (int i = 0; i < activityList.getLength(); i++) {
				Node actionNode = actionList.item(i);
				if (actionNode.getAttributes().getNamedItem("android:name").getNodeValue().equals("android.intent.action.MAIN")) {
					Node mainActvtNode = actionNode.getParentNode().getParentNode();
					mainActvtName = mainActvtNode.getAttributes().getNamedItem("android:name").getNodeValue();
					if (mainActvtName.startsWith("."))	
						mainActvtName = mainActvtName.substring(1, mainActvtName.length());
					if (!mainActvtName.startsWith(StaticInfo.getPackageName(file)))
						mainActvtName = StaticInfo.getPackageName(file) + "." + mainActvtName;
					break;
				}
			}
			for (int i = 0; i < results.size(); i++)
				if (results.get(i).equals(mainActvtName)) {
					String temp = results.get(0);
					results.set(0, results.get(i));
					results.set(i, temp);
				}
		}	catch (Exception e) {e.printStackTrace();}
		return results;
	}
	
	
	private static boolean validateActivity(File file, String activityName) {
		// the activity name in AndroiManifest might be false, e.g., zhiming's Bugatti and bug.
		boolean result = false;
		File classFile = new File(Paths.appDataDir + file.getName() + "/apktool/smali/" + activityName.replace(".", "/") + ".smali");
		if (classFile.exists())	result = true;
		return result;
	}
	
	
	public static void parseXMLLayouts(File file) {
		// create a Layout Object for each layout xml, also spot out the custom layouts
		// create ViewNodes from components that has 'andriod:id' in each layout
		File layoutFolder = new File(Paths.appDataDir + file.getName() + "/apktool/res/layout/");
		if (!layoutFolder.exists()) {
			System.out.println("can't find /apktool/res/layout folder! Run apktool first.");
			return;
		}
		File[] layoutFiles = layoutFolder.listFiles();
		for (File layoutFile: layoutFiles){
			if (!layoutFile.getName().endsWith(".xml"))	continue;
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(layoutFile);
				Node layoutNode = doc.getFirstChild();
				String layoutName = layoutFile.getName().substring(0, layoutFile.getName().length()-4);
				String layoutType = layoutNode.getNodeName();
				ArrayList<String> classNames = getClassNames(file);
				boolean isCustom = false;
				for (String c: classNames)
					if (c.equals(layoutType))
						isCustom = true;
				Layout thisLayout = new Layout(layoutName, layoutNode, isCustom);
				layoutList.add(thisLayout);
				NodeList nodes = layoutNode.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (hasID(node)) {
						String ID = node.getAttributes().getNamedItem("android:id").getNodeValue();
						String type = node.getNodeName();
						isCustom = false;
						for (String c: classNames)
							if (c.equals(layoutType))
								isCustom = true;
						ViewNode thisNode = new ViewNode(type, ID, node, isCustom);
						thisLayout.addNode(thisNode);
					} else {
						// TODO need to add solution to nodes that has event handlers but no id
					}
				}
			}	catch (Exception e) {e.printStackTrace();}
		}
	}
	
	private static boolean hasID(Node node) {
		// if this view has ID, then it's worth keeping
		boolean result = false;
		if (!node.hasAttributes())	return false;
		NamedNodeMap attrs = node.getAttributes();
		for (int i = 0, len = attrs.getLength(); i < len; i++)
			if (attrs.item(i).getNodeName().equals("android:id")) {
				result = true;
				break;
			}
		return result;
	}
	
	public static void parseJavaLayouts() {
		// TODO parse java layouts and add views
		// first read the code of those custom layouts, then scan the others
		
	}
	
	public static String findViewNameByID(File file, String ID) {
		// takes a hex ID string as input, look for the view Name in /res/values/public.xml
		// this is to deal with 'findViewById()' in the jimple code
		String result = "";
		File manifestFile = new File(Paths.appDataDir + file.getName() + "/apktool/res/values/public.xml");
		if (!manifestFile.exists()) {
			System.out.println("can't find /apktool/res/values/public.xml! Run apktool first.");
			return result;
		}
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestFile);
			doc.getDocumentElement().normalize();
			NodeList nl = doc.getElementsByTagName("*");
			for (int i = 0, len = nl.getLength(); i < len; i++) {
				Node n = nl.item(i);
				if (!n.hasAttributes())	continue;
				Node idNode = n.getAttributes().getNamedItem("id");
				Node nameNode = n.getAttributes().getNamedItem("name");
				if (idNode==null)	continue;
				if (ID.equals(idNode.getNodeValue())) {
					result = nameNode.getNodeValue();
					break;
				}
			}
		}	catch (Exception e) {e.printStackTrace();}
		return result;
	}

	
	public static ArrayList<String> getLeavingWidgets(File file, String activityName, String layoutName) {
		ArrayList<String> results = new ArrayList<String>();
		try {
			for (String UIChange: UIChanges) {
				// maybe I don't need to see if it's an event handler, just look at the responsible views
				String[] u = UIChange.split(",");
				int viewNumber = Integer.parseInt(u[7]);
				if (viewNumber==0)	continue;
				for (int i = 8; i < u.length; i++) {
					String[] v = u[i].split("&&");
					// class name, method subsig, layout name, view id, event handler
					if (activityName.equals(v[0]) && layoutName.equals(v[2])) {
						results.add(u[0] + "," + v[3] + "," + v[4] + "," + u[6]);
					}
				}
				
				if (UIChange.startsWith("setContentView,")) {}
				else if (UIChange.startsWith("startActivity,")) {}
			}
		}	catch (Exception e) {e.printStackTrace();}
		return results;
	}
	
	public static ArrayList<String> getStayingWidgets(File file, String activityName, String layoutName) {
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<String> leaving = getLeavingWidgets(file, activityName, layoutName);
		Layout l = getLayoutObject(layoutName);
		ArrayList<ViewNode> viewNodes = l.getViewNodes();
		for (ViewNode viewNode: viewNodes) {
			String id = viewNode.getID();
			boolean isLeaving = false;
			for (String s: leaving)
				if (s.split(",")[1].equals(id))
					isLeaving = true;
			if (!isLeaving)
				results.add(id);
		}
		return results;
	}
	
	private static Layout getLayoutObject(String name) {
		Layout result = null;
		for (Layout l: layoutList)
			if (l.getName().equals(name))
				result = l;
		return result;
	}
	
	public static void process_Intents_And_setContentView(File file) {
		File[] classFolders = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/").listFiles();
		try {
			// first collect all startActivity and setContentView in code, and determine their characteristics
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
						if (EventHandlers.isStartActivity(line) > -1 && line.contains("virtualinvoke"))
							UIChanges.add(solveIntent(file, className, methodFileName, lineNumber));
						else if (EventHandlers.isSetContentView(line) > -1 && line.contains("virtualinvoke"))
							UIChanges.add(solveSetContentView(file, className, methodFileName, lineNumber));
						lineNumber++;
					}
					in_mJ.close();
				}
			}
			// TODO then find default layout for each activity
			//
		}	catch (Exception e) {e.printStackTrace();}
	}
	
	
	
	private static String solveIntent(File file, String className, String methodFileName, int lineNumber) throws Exception{
		// scan each method, look for startActivity()
		// if found, need to find two things:
		//   1. target activity
		//   2. Which event handler of which view in which activity will possibly call this method
		//      also need to consider some special places like onCreate.
		// return format is at the end of method
		String result = "", targetActivityName = "";
		ArrayList<String> responsibleViews = new ArrayList<String>();
		boolean targetClassFound = false, inOnCreate = false, inEventHandler = false;
		//------ Step 1 ------
		File mJFile = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/" + methodFileName + ".jimple");
		BufferedReader in_mJ = new BufferedReader(new FileReader(mJFile));
		for (int i = 0; i < lineNumber - 1; i++)
			in_mJ.readLine();
		String theStmt = in_mJ.readLine();
		in_mJ.close();
		// TODO add more solution to other startActivity() signatures
		// now it's just 'void startActivity(android.content.Intent)'
		if (EventHandlers.isStartActivity(theStmt) == 0) {
			String intentLocalName = theStmt.substring(theStmt.indexOf(EventHandlers.intentSigs[0])+EventHandlers.intentSigs[0].length(), theStmt.length());
			intentLocalName = intentLocalName.substring(intentLocalName.indexOf("(")+1, intentLocalName.lastIndexOf(")"));
			in_mJ = new BufferedReader(new FileReader(mJFile));
			String line;
			//get all stmts that contains that Intent's variable name
			ArrayList<String> intentStmts = new ArrayList<String>();
			while ((line = in_mJ.readLine())!=null)
				if (line.contains(intentLocalName))
					intentStmts.add(line);
			// TODO here I assume everyone will use 'void <init>()' to give target class name
			// I will put the exceptions in ~/UtilLogs/intentSolving.log
			String intentInitSig = "specialinvoke " + intentLocalName + ".<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>";
			for (int i = intentStmts.size()-1; i >=0; i--) {
				String thisStmt = intentStmts.get(i);
				if (intentStmts.get(i).contains(intentInitSig)) {
					targetActivityName = thisStmt.substring(thisStmt.indexOf(intentInitSig) + intentInitSig.length(), thisStmt.length());
					targetActivityName = targetActivityName.substring(targetActivityName.indexOf("(")+1, targetActivityName.lastIndexOf(")"));
					targetActivityName = targetActivityName.split(",")[1].trim();
					if (targetActivityName.startsWith("class \"")) {
						targetActivityName = targetActivityName.substring(targetActivityName.indexOf("\"")+1, targetActivityName.lastIndexOf("\""));
						if (targetActivityName.indexOf("/") > -1)
							targetActivityName = targetActivityName.replace("/", ".");
						targetClassFound = true;
					}
				}
			}
			if (!targetClassFound) {
				File outIntentLog = new File(Paths.appDataDir + file.getName() + "/UtilLogs/intentSolving.log");
				outIntentLog.getParentFile().mkdirs();
				PrintWriter out = new PrintWriter(new FileWriter(outIntentLog, true));
				out.write("Cannot Solve Target Activity," + file + "," + className + "," + methodFileName + "," + lineNumber + "\n");
				out.close();
			}
		}
		//--- end of Step 1 ----
		
		//--- Step 2 -----
		File mIFile = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/" + methodFileName + ".csv");
		BufferedReader in_mI = new BufferedReader(new FileReader(mIFile));
		String methodSubSig = in_mI.readLine().split(",")[5];
		in_mI.close();
		// 2.1  see if this method is some activity's onCreate or some view's event handler
		if (methodSubSig.equals("void onCreate(android.os.Bundle)")) {
			if (isActivity(file, className))
				inOnCreate = true;
		} else {
			// check if it's in an event handler
			for (Layout l: layoutList) {
				ArrayList<ViewNode> viewNodeList = l.getViewNodes();
				for (ViewNode vN: viewNodeList) {
					Map<String, String> ehMap = vN.getAllEventHandlers();
					String ehType = "";
					for (Map.Entry<String, String> entry: ehMap.entrySet()) {
						boolean matchEHMethodName = false;
						ehType = entry.getKey();
						String ehMethod = entry.getValue();
						String ehMethodSig = ehMethod + "(android.view.View)";
						if (methodSubSig.endsWith(ehMethodSig)) {
							inEventHandler = true;
							matchEHMethodName = true;
						}
						// TODO need to add another layer of validation, although we found matched widget by method name,
						// but this activity and method might not be used by that layout
						// this class might not even be an activity
						if (isActivity(file, className) && matchEHMethodName)
							responsibleViews.add(className + "&&" + methodSubSig + "&&" + l.getName() + "&&" + vN.getID() + "&&" + ehType);
					}
				}
			}
			if (!inEventHandler || inEventHandler) {
				// TODO 2.2 search through CallGraph, find all EventHandlers and onCreate that might call this method
				//
			}
		}
		//--- end of Step 2 ---
		
		// return format:
		// "StartActivity",foundTargetActvt?,inOnCreate?,inEventHandler?,classname,methodname,targetActvt,NumberOfViews,(layout&&widgetID),(layout&&widgetID),...
		result +="startActivity,";
		if (targetClassFound) result+="1,"; else result+="0,";
		if (inOnCreate)	result+="1,"; else result+="0,";
		if (inEventHandler)	result+="1,"; else result+="0,";
		int len = responsibleViews.size();
		result += className + "," + methodSubSig + "," + targetActivityName + "," + len;
		if (len > 0)
			for (int i = 0; i < len; i++)
				result += "," + responsibleViews.get(i);
		System.out.println(result);
		return result;
	}
	
	public static String solveSetContentView(File file, String className, String methodFileName, int lineNumber) throws Exception{
		// scan each method, look for setContentView()
		// if found, need to find two things:
		//   1. target layout
		//   2. Which event handler of which view in which activity will possibly call this method
		//      also need to consider some special places like onCreate.
		String result = "", targetLayout = " ";
		ArrayList<String> responsibleViews = new ArrayList<String>();
		boolean foundTargetLayout = false, inOnCreate = false, inEventHandler = false;
		// --- Step 1 ---
		File mJFile = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/" + methodFileName + ".jimple");
		BufferedReader in_mJ = new BufferedReader(new FileReader(mJFile));
		for (int i = 0; i < lineNumber - 1; i++)
			in_mJ.readLine();
		String theStmt = in_mJ.readLine();
		// TODO add more solution to other setContentView() signatures
		// now it's just 'void setContentView(int)'
		if (EventHandlers.isSetContentView(theStmt) == 0) {
			//TODO now I assume everyone will use constant
			//if the parameter is not int constant, write to /UtilsLog/setContentViewSolving.log
			String viewID = theStmt.substring(theStmt.indexOf(EventHandlers.setContentViewSigs[0]) + EventHandlers.setContentViewSigs[0].length() , theStmt.length());
			viewID = viewID.substring(viewID.indexOf("(") + 1 , viewID.lastIndexOf(")"));
			if (!viewID.startsWith("$")) {
				viewID = "0x" + Integer.toHexString(Integer.parseInt(viewID));
				while (viewID.length()<10)
					viewID += "0";
				targetLayout = findViewNameByID(file, viewID);
				if (!targetLayout.equals(""))
					foundTargetLayout = true;
			} else {
				File logFile = new File(Paths.appDataDir + file.getName() + "/UtilsLog/setContentViewSolving.log");
				logFile.getParentFile().mkdirs();
				PrintWriter out = new PrintWriter(new FileWriter(logFile, true));
				out.write("Can not solve setContentView target," + className + "," + methodFileName + "," + lineNumber + "\n");
				out.close();
			}
		}
		in_mJ.close();
		// --- end of Step 1 ---
		
		// --- Step 2 ---
		File mIFile = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/" + methodFileName + ".csv");
		BufferedReader in_mI = new BufferedReader(new FileReader(mIFile));
		String methodSubSig = in_mI.readLine().split(",")[5];
		in_mI.close();
		// I ran test on the PlayStore apps, every activity's onCreate method has signature 'void onCreate(android.os.Bundle)'
		// if this setContentView is in onCreate and in a activity class, then it's a default layout
		if (methodSubSig.equals("void onCreate(android.os.Bundle)")) {
			if (isActivity(file, className))
				inOnCreate = true;
		} else {
			// check if it's in an event handler
			for (Layout l: layoutList) {
				ArrayList<ViewNode> viewNodeList = l.getViewNodes();
				for (ViewNode vN: viewNodeList) {
					Map<String, String> ehMap = vN.getAllEventHandlers();
					for (Map.Entry<String, String> entry: ehMap.entrySet()) {
						boolean matchEHMethodName = false;
						String ehType = entry.getKey();
						String ehMethod = entry.getValue();
						String ehMethodSig = ehMethod + "(android.view.View)";
						if (methodSubSig.endsWith(ehMethodSig)) {
							inEventHandler = true;
							matchEHMethodName = true;
						}
						// TODO need to add another layer of validation, although we found matched widget by method name,
						// but that layout might not be used by this activity
						if (isActivity(file, className) && matchEHMethodName)
							responsibleViews.add(className + "&&" + methodSubSig + "&&" + l.getName() + "&&" + vN.getID() + "&&" + ehType);
					}
				}
			}
			if (!inEventHandler || inEventHandler) {
				// TODO search through call graph, find all event handlers and onCreate that might call this method
				//
			}
		}
		// --- end of Step 2 ---
		
		// return format:
		// "StartActivity",foundTargetLayout?,inOnCreate?,inEventHandler?,classname,methodname,targetActvt,NumberOfViews,(layout&&widgetID),(layout&&widgetID),...
		result +="setContentView,";
		if (foundTargetLayout) result+="1,"; else result+="0,";
		if (inOnCreate)	result+="1,"; else result+="0,";
		if (inEventHandler)	result+="1,"; else result+="0,";
		int len = responsibleViews.size();
		result += className + "," + methodSubSig + "," + targetLayout + "," + len;
		if (len > 0)
			for (int i = 0; i < len; i++)
				result += "," + responsibleViews.get(i);
		System.out.println(result);
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
}
