package zhen.packet;
 
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import zhen.framework.Configuration;

public class Utility {
	
	public static void main(String[] args){
		WindowInformation[] list = getVisibleWindowInformation();
		for(WindowInformation e :	list){
			System.out.println(e);
		}
	}
	
	public static class WindowInformation{
		String name;
		String attachedTo;
		double width, height, startx, starty;
		
		ArrayList<WindowInformation> subWindow = new ArrayList<WindowInformation>();
		
		public String toString(){
			String result = name;
			if(attachedTo != null){
				result += " -> "+attachedTo;
			}
			result += ": ("+width +","+height+") to ("+startx+","+starty+")";
			if(subWindow.size() >0){
				result += " has ";
				for(WindowInformation win : subWindow){
					result += win.name+"; ";
				}
			}
			
			return result;
		}
	
	}
	
	public static WindowInformation[] getVisibleWindowInformation(){
		String command = Configuration.adbPath+" shell dumpsys window visible | grep -E  'Window #|mAttachedWindow=|  Surface:'";
		
		try {
			Process info = Runtime.getRuntime().exec(command);
			InputStream input = info.getInputStream();
			info.waitFor();
			
			ArrayList<WindowInformation> buffer = new ArrayList<WindowInformation>();
			Scanner sc = new Scanner(input); 
			
			WindowInformation last = null;
			while(sc.hasNext()){
				String line = sc.nextLine().trim();
				if(line.startsWith("Window #")){
					String[] parts = line.split(" ");
					String name = parts[4].replace("}:", "");
					
					last = new WindowInformation();
					buffer.add(last);
					
					last.name = name;
				}else if(line.startsWith("Surface:")){
					String[] parts = line.split(" ");
					String rect=parts[4];
					String sWidth = parts[5];
					String sHeight = parts[7];
					
					last.width = Double.parseDouble(sWidth);
					last.height = Double.parseDouble(sHeight);
					
					String subParts[] = rect.split("=")[1].replace("(", "").replace(")", "").split(",");
					last.startx = Double.parseDouble(subParts[0]);
					last.starty = Double.parseDouble(subParts[1]);
				}else if(line.startsWith("mAttachedWindow=")){
					String[] parts = line.split(" ");
					String attchedWindow = parts[2].replace("}", "");
					last.attachedTo = attchedWindow;
				}
			}	
			
			//build relationship 
			for(int index = 0; index < buffer.size() ; index++){
				WindowInformation win = buffer.get(index);
				if(win.attachedTo != null){ //it is attached to some other window
					//look for it
					for(int i = 0; i < buffer.size() ; i++){
						WindowInformation target = buffer.get(i);
						if(win.attachedTo.equals(target.name)){
							target.subWindow.add(win);
							break;
						}
					}
				}
			}
			
			return buffer.toArray(new WindowInformation[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//example
//		  Window #7 Window{95ef9de0 u0 NavigationBar}:
//		      Surface: shown=true layer=201000 alpha=1.0 rect=(0.0,1824.0) 1200.0 x 96.0
//		  Window #6 Window{95ef6a00 u0 StatusBar}:
//		      Surface: shown=true layer=161000 alpha=1.0 rect=(0.0,0.0) 1200.0 x 50.0
//		  Window #3 Window{95ec3318 u0 PopupWindow:95c98778}:
//		    mAttachedWindow=Window{95f19048 u0 com.example.testpopup/com.example.testpopup.AndroidPopupWindowActivity} mLayoutAttached=true
//		      Surface: shown=true layer=21015 alpha=1.0 rect=(50.0,266.0) 333.0 x 314.0
//		  Window #2 Window{95f19048 u0 com.example.testpopup/com.example.testpopup.AndroidPopupWindowActivity}:
//		      Surface: shown=true layer=21010 alpha=1.0 rect=(0.0,0.0) 1200.0 x 1824.0
		
		return null;
	}
	
	
	public static int findFirstFalse(boolean[] arr){
		for(int i=0;i<arr.length;i++){
			if(arr[i] == false) return i;
		}
		return -1;
	}
	
	public static Logger setupLogger(Class clazz){
		Logger logger = Logger.getLogger(clazz.getName());
		FileHandler fhandler;
		ConsoleHandler chandler;
		try {
			for(Handler handle : logger.getHandlers()){
				handle.setLevel(Level.FINER);
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logger;
	}
	
	public static void removeFileUnderFolder(String folderName){
		File folder = new File(folderName);
		String[] names = folder.list();
		for(String name:names){
			File afile = new File(folderName+"/"+name);
			afile.delete();
		}
	}
	
	public static void log(String msg){
		System.out.println(msg);
	}
	
	public static void info(String msg){
		System.out.println(msg);
	}
	
	public static void log(String tag, String msg){
		System.out.println(tag+";"+msg);
	}
}
