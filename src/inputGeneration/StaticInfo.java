package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import main.Paths;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StaticInfo {
	
	
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
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(layoutFile);
				Node layoutNode = doc.getFirstChild();
				String layoutType = layoutNode.getNodeName();
				ArrayList<String> classNames = getClassNames(file);
				boolean isCustom = false;
				for (String c: classNames)
					if (c.equals(layoutType))
						isCustom = true;
				Layout thisLayout = new Layout(layoutNode, isCustom);
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
		Node idNode = node.getAttributes().getNamedItem("android:id");
		if (idNode!=null)	result = true;
		return result;
	}
	
	public static void parseJavaLayouts() {
		// parse java layouts and add views
		// first read the code of those custom layouts, then scan the others
		
	}
	
	public static void findViewNameByID(String ID) {
		// takes a hex ID string as input, look for the view Name in /res/values/public.xml
		// this is to deal with 'findViewById()' in the jimple code
		
	}

	
	public static ArrayList<String> getLeavingWidgets(File file, String activityName, String layoutName) {
		// given layout and activity, search widgets' event handlers and see if they change layout/activity
		// need to solve Intent and setContentView first
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
						if (EventHandlers.isStartActivity(line) && line.contains("virtualinvoke"))
							solveIntent(file, className, methodFileName, lineNumber);
						else if (EventHandlers.isSetContentView(line) && line.contains("virtualinvoke"))
							solveSetContentView(file, className, methodFileName, lineNumber);
						lineNumber++;
					}
					in_mJ.close();
				}
			}
		}	catch (Exception e) {e.printStackTrace();}
	}
	
	
	
	private static void solveIntent(File file, String className, String methodFileName, int lineNumber) throws Exception{
		// scan each method, look for startActivity()
		// if found, need to find two things:
		//   1. target activity
		//   2. Which event handler of which view in which activity will possibly call this method
		//      also need to consider some special places like onCreate.
		BufferedReader in_mJ = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/" + methodFileName + ".jimple"));
		
		in_mJ.close();
	}
	
	private static void solveSetContentView(File file, String className, String methodFileName, int lineNumber) throws Exception{
		// scan each method, look for setContentView()
		// if found, need to find two things:
		//   1. target layout
		//   2. Which event handler of which view in which activity will possibly call this method
		//      also need to consider some special places like onCreate.
		BufferedReader in_mJ = new BufferedReader(new FileReader(Paths.appDataDir + file.getName() + "/ClassesInfo/" + className + "/" + methodFileName + ".jimple"));
		
		in_mJ.close();
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
