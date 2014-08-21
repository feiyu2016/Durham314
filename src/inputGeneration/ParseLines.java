package inputGeneration;

import java.io.File;
import java.util.ArrayList;

import main.Paths;

public class ParseLines {
	
	public void parseLines(File path) {
		ArrayList<String> al = StaticInfo.getClassNames(path);
		
		for(String string:al){
			String newPath = Paths.appDataDir + path.getName() + "/apktool/smali/" + string.replace(".", "/") + ".smali";
			System.out.println(newPath);
		}
	}
	
	public static void main(String args[]) {
		try {	
			new ParseLines().parseLines(new File("astro.apk"));
		} catch (Exception e) {}
	}
}