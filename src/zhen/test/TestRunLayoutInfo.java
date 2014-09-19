package zhen.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import zhen.implementation.graph.Event;
import zhen.implementation.graph.EventType;
import zhen.implementation.graph.GraphStructureLayoutInformation;
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
		ArrayList<ViewNode> list = new ArrayList<ViewNode>();
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
				
				ArrayList<ViewNode> queue = new ArrayList<ViewNode>();
				queue.add(root);
				while(!queue.isEmpty()){
					ViewNode current = queue.remove(0);
					System.out.println(current.id+","+current.left+","+current.top);
					if(current.children == null) continue;
					for(ViewNode child : current.children){
						queue.add(child);
					}
				}
			} else if(read.equals("4")){
				
 
			}
		}
		sc.close();
		System.exit(0);
		
	}

}
