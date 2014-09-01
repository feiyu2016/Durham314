package analysisTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import main.Paths;

import soot.BodyTransformer;
import soot.PackManager;
import soot.SceneTransformer;
import soot.Transform;
import soot.Body;

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
