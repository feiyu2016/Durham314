package analysisTools;

import inputGeneration.StaticInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import main.Paths;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.util.Chain;


public class Soot {

	public static void generateAPKData(final File file) {
	/* this method writes App data to:
	 *  	/AppData/(file name)/CallGraph.csv
	 *  	/AppData/(file name)/ApkInfo.csv
	 *  	/AppData/(file name)/(class name)/ClassInfo.csv
	 *  	/AppData/(file name)/(class name)/(method name).csv
	 *  	/AppData/(file name)/(class name)/(method name).jimple
	 *  	....
	 *  	....
	*/
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
			protected void internalTransform(String phaseName, Map options) {
				try {
					System.out.println("test");
					CHATransformer.v().transform();
					Chain<SootClass> classes = Scene.v().getApplicationClasses();
					File cgFile = new File(Paths.appDataDir + file.getName() + "/CallGraph.csv");
					cgFile.getParentFile().mkdirs();
					PrintWriter out_CG = new PrintWriter(new FileWriter(cgFile));
					File apkInfoFile = new File(Paths.appDataDir + file.getName() + "/ApkInfo.csv");
					PrintWriter out_ApkInfo = new PrintWriter(new FileWriter(apkInfoFile));
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					String theTime = dateFormat.format(new Date());
					out_ApkInfo.write(file.getAbsolutePath() + "," + classes.size() + "," + theTime + "\n");
					for (SootClass c: classes) {
						out_ApkInfo.write("Class," + c.getName() + "\n");
						int longMethodNameID = 1;
						if (c.getName().startsWith("android.support.v"))	continue;
						File classFile = new File(Paths.appDataDir + file.getName() + "/ClassesInfo/" + c.getName() + "/ClassInfo.csv");
						classFile.getParentFile().mkdirs();
						PrintWriter out_Class = new PrintWriter(new FileWriter(classFile));
						out_Class.write(c.getName() + "," + c.getFieldCount() + "," + c.getMethodCount() + "\n");
						Chain<SootField> fields = c.getFields();
						for (SootField field: fields)
							out_Class.write("Field," + field.getName() + "," + field.getType().toString() + "," + field.getModifiers() + "\n");
						List<SootMethod> methods = c.getMethods();
						for (SootMethod method: methods) {
							String methodType = "Method,";
							if (method.isAbstract())	methodType = "AbstractMethod,";
							else if (method.isNative())	methodType = "NativeMethod,";
							String methodFileName = method.getSubSignature().replace(",", " ");
							if (methodFileName.length() > 100) {methodFileName = method.getName() + "_" + longMethodNameID; longMethodNameID++;}
							if ((System.getProperty("os.name").startsWith("Windows")))
								methodFileName = methodFileName.replace("<", "[").replace(">", "]");
							out_Class.write(methodType + method.getName() + "," + method.getReturnType().toString() + "," + 
											method.getModifiers() + "," + methodFileName + "," + method.getSubSignature().replace(",", " ") + "\n");
							if (method.isAbstract() || method.isNative())
								continue;
							Body b;
							try { b = method.retrieveActiveBody();}	catch (Exception e) {continue;}
							PrintWriter out_MethodJimple = new PrintWriter(new FileWriter(Paths.appDataDir + file.getName() + "/ClassesInfo/" + c.getName() + "/" + methodFileName + ".jimple"));
							out_MethodJimple.write(b.toString());
							out_MethodJimple.close();
							PrintWriter out_Method = new PrintWriter(new FileWriter(Paths.appDataDir + file.getName() + "/ClassesInfo/" + c.getName() + "/" + methodFileName + ".csv"));;
							out_Method.write(method.getName() + "," +
									method.getReturnType().toString() + "," + 
									method.getModifiers() + "," + 
									method.getParameterCount() + "," + 
									b.getLocalCount() + "," + 
									method.getSubSignature().replace("," , " ") + "\n");
							List<Local> params = b.getParameterLocals();
							for (Local param: params)
								out_Method.write("Param," + param.getName() + "," + param.getType().toString() + "\n");
							Chain<Local> locals = b.getLocals();
							for (Local local: locals)
								out_Method.write("Local," + local.getName() + "," + local.getType().toString() + "\n");
							Iterator<Unit> units = b.getUnits().iterator();
							int stmtCounter = 1;
							while (units.hasNext()) {
								Stmt stmt = (Stmt)units.next();
								if (stmt.containsFieldRef()) {
									SootField tgtField = stmt.getFieldRef().getField();
									SootClass tgtfieldC = tgtField.getDeclaringClass();
									out_Method.write("FieldRef," + tgtField.getName() + "," + 
													tgtField.getType().toString() +"," + 
													tgtfieldC.getName() + "," + 
													stmt.toString() + "," + stmtCounter + "\n");
									out_CG.write("FieldRef," + c.getName() + "," + 
												method.getName() + "," + 
												tgtfieldC.getName() + "," + 
												tgtField.getName() + "," + 
												tgtField.getType().toString() + "," +
												stmt.toString() + "," + stmtCounter + "\n");
								} else if (stmt.containsInvokeExpr()) {
									SootMethod tgtMethod = stmt.getInvokeExpr().getMethod();
									SootClass tgtMethod_Class = tgtMethod.getDeclaringClass();
									out_Method.write("MethodCall," + tgtMethod.getName() + "," + 
													tgtMethod_Class.getName() + "," + 
													tgtMethod.getSubSignature().replace(",", " ") + "," + 
													stmt.toString() + "," + stmtCounter + "\n");
									out_CG.write("MethodCall," + c.getName() + "," + 
												method.getName() + "," + 
												tgtMethod_Class.getName() + "," + 
												tgtMethod.getSubSignature().replace("," , " ") + "," + 
												stmt.toString() + "," + stmtCounter + "\n");
								}
								stmtCounter++;
							}
							out_Method.close();
						}
						out_Class.close();
					}
					out_CG.close();
					out_ApkInfo.close();
				}	catch (Exception e) {e.printStackTrace();}
			}
		}));
		
		String[] args = {};
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		argsList.addAll(Arrays.asList(new String[] {
				"-d", Paths.appDataDir + file.getName() + "/soot/Jimples",
				"-f", "J",
				"-src-prec", "apk",
				"-ire", "-allow-phantom-refs", "-w",
				"-force-android-jar", Paths.androidJarPath,
				"-process-path", file.getAbsolutePath()	}));
		args = argsList.toArray(new String[0]);
		soot.Main.main(args);
		soot.G.reset();
		sortClassNames(file);
	}

	private static void sortClassNames(File file) {
	// this method sorts the /AppData/(file name)/ApkInfo.csv
	// put MainActivity first, other Activities following, and regular classes at last
		File apkInfoFile = new File(Paths.appDataDir + file.getName() + "/ApkInfo.csv");
		try {
			BufferedReader in = new BufferedReader(new FileReader(apkInfoFile));
			String firstLine = in.readLine();
			int classCount = Integer.parseInt(firstLine.split(",")[1]);
			String[] oldClassNames = new String[classCount];
			String[] newClassNames = new String[classCount];
			for (int i = 0; i < classCount; i++)
				oldClassNames[i] = in.readLine().split(",")[1];
			in.close();
			Arrays.sort(oldClassNames);
			ArrayList<String> activities = StaticInfo.getActivityNames(file);
			for (int j = 0; j < activities.size(); j++)
				newClassNames[j] = activities.get(j);
			int counter = 0;
			for (int k = 0; k < classCount; k++) {
				boolean belong = false;
				for (String actvt: activities)	if (oldClassNames[k].equals(actvt))	belong = true;
				if (!belong) {
					newClassNames[activities.size() + counter] = oldClassNames[k];
					counter++;
				}
			}
			PrintWriter out = new PrintWriter(new FileWriter(apkInfoFile));
			out.write(firstLine + "\n");
			out.write("MainActivity," + newClassNames[0] + "\n");
			for (int l = 1; l < activities.size(); l++)
				out.write("Activity," + newClassNames[l] + "\n");
			for (int m = activities.size(); m < classCount; m++)
				out.write("Class," + newClassNames[m] + "\n");
			out.close();
		}	catch (Exception e) {e.printStackTrace();}
	}
	
	public static void InstrumentEveryMethod(File file) throws Exception{
		
		File instrumentLog = new File(Paths.appDataDir + file.getName() + "/soot/Instrumentation/Instrument_Method.csv");
		instrumentLog.getParentFile().mkdirs();
		final PrintWriter out = new PrintWriter(new FileWriter(instrumentLog));
		PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new BodyTransformer() {
			protected void internalTransform(Body b, String phaseName,Map<String, String> options) {
				if (true) return;
				String className = b.getMethod().getDeclaringClass().getName();
				if (className.startsWith("android.support.v"))	return;
				if (b.getMethod().getName().contains("<clinit>") || b.getMethod().getName().contains("<init>"))	return;
				Local l_outPrint = Jimple.v().newLocal("outPrint", RefType.v("java.io.PrintStream"));
				b.getLocals().add(l_outPrint);
				SootMethod out_println = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
				String methodSig = b.getMethod().getSignature();
				PatchingChain<Unit> units = b.getUnits();
				Iterator<Unit> unitsIT = b.getUnits().snapshotIterator();
				for (int i = 0; i < b.getMethod().getParameterCount()+1; i++)
					unitsIT.next();
				Unit firstUnit = unitsIT.next();
				units.insertBefore(Jimple.v().newAssignStmt(l_outPrint,
						Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())
						), firstUnit);
				units.insertBefore(Jimple.v().newInvokeStmt(
						Jimple.v().newVirtualInvokeExpr(l_outPrint, out_println.makeRef(), StringConstant.v("METHOD BEING TRIGGERED: " + methodSig))
						), firstUnit);
				b.validate();
				out.write(className + "," + b.getMethod().getName() + "\n");
			}
		}));

		String[] args = {};
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		argsList.addAll(Arrays.asList(new String[] {
				"-d", Paths.appDataDir + file.getName() + "/soot/Instrumentation",
				"-f", "dex",
				"-src-prec", "apk",
				"-ire", "-allow-phantom-refs", "-w",
				"-force-android-jar", Paths.androidJarPath,
				"-process-path", file.getAbsolutePath()	}));
		args = argsList.toArray(new String[0]);
		File outFile = new File(Paths.appDataDir + file.getName() + "/soot/Instrumentation/" + file.getName());
		if (outFile.exists())	outFile.delete();
		Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
		soot.Main.main(args);
		soot.G.reset();
		out.close();
	}

}
