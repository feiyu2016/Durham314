package zhen.test;

import java.util.Arrays;
import java.util.Scanner;

import zhen.graph.Event;
import zhen.graph.EventType;
import zhen.graph.TraversalGraph;
import zhen.packet.RunTimeLayoutInformation;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;

public class TestRunLayoutInfo {
	public static void main(String[] args){
		RunTimeLayoutInformation info = new RunTimeLayoutInformation("adb");
		info.init(); 
		Scanner sc = new Scanner(System.in);
		ViewNode root = null;
		Window win = null;
		while(true){
			System.out.println("========================");
			String read = sc.nextLine().trim();
			if(read.equals("0")) break;
			else if(read.equals("1")){
				win = info.getFocusedWindow();
				System.out.println("win:"+win); 
			}else if(read.equals("2")){
				Window[] ws = info.getWindowList();
				System.out.println(Arrays.toString(ws));
 
			} else if(read.equals("3")){
				win = info.getFocusedWindow();
				root = info.loadFocusedWindowData();
 
			} else if(read.equals("4")){
				win = info.getFocusedWindow();
 
			}
		}
		sc.close();
		System.exit(0);
		
	}

}
