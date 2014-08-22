package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import main.Paths;

public class ParseLines {
	
	public ArrayList<String> parseLines(File path) {
		ArrayList<String> al = StaticInfo.getClassNames(path);
		ArrayList<String> ret = new ArrayList<String>();
		
		for(String string:al){
			String newPath = Paths.appDataDir + path.getName() + "/apktool/smali/" + string.replace(".", "/") + ".smali";
			
			try {
	            BufferedReader input = new BufferedReader(new FileReader(newPath));
	            String line;
	            while ((line = input.readLine()) != null) {
	               if (line.trim().startsWith(".line"))
	                	ret.add(string.replace("/", ".")+ ":" + line.trim().split(" ")[1]);
	            }
	            input.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
		return ret;
	}

}