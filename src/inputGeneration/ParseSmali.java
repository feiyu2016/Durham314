package inputGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import main.Paths;

public class ParseSmali {
	
	public ArrayList<String> parseLines(File path) {
		ArrayList<String> al = StaticInfo.getClassNames(path);
		ArrayList<String> ret = new ArrayList<String>();
		
		for (String string:al) {
			String newPath = Paths.appDataDir + path.getName() + "/apktool/smali/" + string.replace(".", "/") + ".smali";
			if(string.startsWith("android.support.v")) continue;
			try {
	            BufferedReader input = new BufferedReader(new FileReader(newPath));
	            String line;
	            while ((line = input.readLine()) != null) {
	               if (line.trim().startsWith(".line"))
	                	ret.add(string + ":" + line.trim().split(" ")[1]);
	            }
	            input.close();
	        } 
			catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
		return ret;
	}
	
	
	
	public ArrayList<String> parseBeginningAndReturnLines(File path) {
		ArrayList<String> al = StaticInfo.getClassNames(path);
		ArrayList<String> method = new ArrayList<String>();
		ArrayList<String> ret = new ArrayList<String>();
		boolean firstLine = false;
		
		for (String string:al) {
			String newPath = Paths.appDataDir + path.getName() + "/apktool/smali/" + string.replace(".", "/") + ".smali";
			if(string.startsWith("android.support.v")) continue;
			try {
	            BufferedReader input = new BufferedReader(new FileReader(newPath));
	            String line;
	            while ((line = input.readLine()) != null) {
	            	if (line.trim().startsWith(".method")) {
						String temp = string + "," + line.trim().split(" ")[line.trim().split(" ").length -1];
						while ((line = input.readLine()) != null){
							method.add(line);
							if (line.trim().startsWith(".line") && !firstLine) {
								 temp = temp + ",begins," + line.trim().split(" ")[1] +  ",returns";
								 firstLine = true;
							}
							if (line.trim().startsWith(".end method")) {
								for (int i = 0; i < method.size(); i++) {
									if (method.get(i).trim().startsWith("return")) {
										for (int j = i-1; j > 0; j--){
											if(method.get(j).trim().startsWith(".line")) {
												temp = temp + "," + method.get(j).trim().split(" ")[1];
												break;
											}
										}
										break;
									}
								}
									
								method.clear();
								ret.add(temp);
								firstLine = false;
								break;
							}
						}
					}
				
				
				}
	            input.close();
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	public ArrayList<String> parseMethodLines(File path) {
		ArrayList<String> al = StaticInfo.getClassNames(path);
		ArrayList<String> ret = new ArrayList<String>();
		
		for (String string:al) {
			String newPath = Paths.appDataDir + path.getName() + "/apktool/smali/" + string.replace(".", "/") + ".smali";
			if(string.startsWith("android.support.v")) continue;
			try {
	            BufferedReader input = new BufferedReader(new FileReader(newPath));
	            String line;
	            while ((line = input.readLine()) != null) {
	            	if (line.trim().startsWith(".method")) {
						String temp = (string + "," + line.trim().split(" ")[line.trim().split(" ").length -1]);
						while ((line = input.readLine()) != null){
							if (line.trim().startsWith(".line")) {
								temp = temp + "," + line.trim().split(" ")[1];
							}
							else if (line.trim().startsWith(".end method")) {
								ret.add(temp);
								break;
							}
						}
					}
				
				
				}
	            input.close();
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}

}