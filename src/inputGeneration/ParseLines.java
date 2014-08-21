package inputGeneration;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;

import main.Paths;

public class ParseLines {
	
	private String path;
	
	public ParseLines(String p) {
		path = p;
	}
	
	public void parseLines() {
		ArrayList<String> al = new StaticInfo().getClassNames(new File(path));
		
		for(String string:al){
			System.out.println(string);
		}
	}
}
