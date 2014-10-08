package zhen.version1.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import zhen.version1.framework.Configuration;
 

/**
 * Window information extracted from ADB shell dumpsys
 * Only information on visible window is retrieved. 
 * @author zhenxu
 *
 */
public class WindowInformation {
	public String name; 
	public String encode;
	public String uid;
	public String showToUserOnly;
	public String pkgName;
	public String appop;
	public double width, height, startx, starty;
	
	private static final String shellCommand = " shell dumpsys window visible | grep -E  'Window #|mAttachedWindow=|package=|mFrame='";
	
	@Override
	public boolean equals(Object other){
		if(other instanceof  WindowInformation){
			WindowInformation win = (WindowInformation)other;
			if(this.width != win.width) return false;
			if(this.height != win.height) return false;
			if(this.startx != win.startx) return false;
			if(this.starty != win.starty) return false;
		}
		return false;
	}
	
	
	
	@Override
	public String toString(){
		String result = name+", "+uid+", "+showToUserOnly+", "+pkgName+", "+appop+", ("+width+","+height+"), ("+startx+","+starty+")";
		return result;
	}
	
	/**
	 * 	Sample #1
	 *  Window #6 Window{95e2b0e8 u0 Import}:
     *		mOwnerUid=10069 mShowToOwnerOnly=true package=com.example.backupHelper appop=NONE
     *			Surface: shown=true layer=21030 alpha=1.0 rect=(20.0,668.0) 1160.0 x 538.0
     *
     *	Sample #2
     *  Window #6 Window{95fa3ea8 u0 PopupWindow:95cc3ea8}:
     *		mOwnerUid=10071 mShowToOwnerOnly=true package=com.example.testpopup appop=NONE
     *			Surface: shown=true layer=21030 alpha=1.0 rect=(50.0,266.0) 333.0 x 314.0
     *
	 */
	
	public static WindowInformation[] getVisibleWindowInformation(){
		String command = Configuration.ADBPath+" "+shellCommand;
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
					last = new WindowInformation();
					buffer.add(last);
					String[] parts = line.split(" ");
					last.name = parts[4].replace("}:", "");
					last.encode = parts[2].replace("Window{", "");
				}else if(line.startsWith("mFrame=")){
					// mFrame=[0,0][1200,1824]
					String[] parts = line.split(" ");
					String contentPairValue = parts[0].split("=")[1];
					String[] pair = contentPairValue.replaceAll("]|\\[", " ").trim().split("  ");
					String[] start = pair[0].split(",");
					last.startx = Double.parseDouble(start[0]);
					last.starty = Double.parseDouble(start[1]);
					String[] end = pair[1].split(",");
					last.width = Double.parseDouble(end[0]) - last.startx;
					last.height = Double.parseDouble(end[1]) - last.starty;
				}else if(line.startsWith("mOwnerUid=")){
					String[] parts = line.split(" ");
					last.uid = parts[0].replace("mOwnerUid=", "");
					last.showToUserOnly = parts[1].replace("mShowToOwnerOnly=", "");
					last.pkgName = parts[2].replace("package=", "");
					last.appop = parts[3].replace("appop=", "");
				}
			}	
		
			sc.close();
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
}
