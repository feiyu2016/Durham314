package zhen.test;

import java.util.Scanner;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;

import zhen.implementation.graph.Event;
import zhen.implementation.graph.EventType;
import zhen.implementation.graph.GraphStructureLayoutInformation;
import zhen.packet.RunTimeLayoutInformation;

public class testGraph {
	public static void main(String[] args){
		RunTimeLayoutInformation info = new RunTimeLayoutInformation("adb");
		info.init();
		GraphStructureLayoutInformation trversal = new GraphStructureLayoutInformation(null);
		Scanner sc = new Scanner(System.in);
		ViewNode root = null;
		Window win = null;
		while(true){
			String read = sc.nextLine().trim();
			if(read.equals("0")) break;
			else if(read.equals("1")){
				win = info.getFocusedWindow();
				System.out.println("win:"+win);
				trversal.setLauncherActName(win.getTitle());
				trversal.enableGUI();
			}else if(read.equals("2")){
				win = info.getFocusedWindow();
				Event event = new Event(EventType.LAUNCH,null);
				trversal.extendGraph(event, win.getTitle(), null);
			} else if(read.equals("3")){
				win = info.getFocusedWindow();
				root = info.loadFocusedWindowData();
				Event event = new Event(EventType.ONCLICK,null);
				trversal.extendGraph(event, win.getTitle(), root);
			} else if(read.equals("4")){
				win = info.getFocusedWindow();
				root = info.loadFocusedWindowData();
				Event event = new Event(EventType.ONBACK,null);
				trversal.extendGraph(event, win.getTitle(), root);
			}
		}
		sc.close();
		System.exit(0);
		
	}
}
