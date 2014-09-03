package analysisTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import main.Paths;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalBlockGraph;

public class SootTemplate {

	public static void Template(File file) {
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
			protected void internalTransform(String phaseName, Map<String, String> options) {
				// this method will be called only once
				
			}
		}));
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new BodyTransformer() {
			protected void internalTransform(Body b, String phaseName,Map<String, String> options) {
				// this method will be called on each app method
				SootMethod m = b.getMethod();
				if (m.getDeclaringClass().getName().startsWith("android.support.v"))	return;
				if (!m.getName().equals("doIT"))	return;
				System.out.println("BYTECODE PARAMS " + m.getBytecodeSignature());
				ExceptionalBlockGraph eBG = new ExceptionalBlockGraph(b);
				for (Block bl: eBG.getBlocks())
					System.out.println(bl);
				System.out.println("exceptional BG size: " + eBG.size());
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
	}
	
}
