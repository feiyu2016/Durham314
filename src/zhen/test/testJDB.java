package zhen.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import inputGeneration.JDBStuff;
import inputGeneration.ParseSmali;

public class testJDB {

	public static void main(String[] args) {
		JDBStuff jdb = new JDBStuff();
		String path = "APK/Fast.apk";
		File apkFile = new File(path);
		ArrayList<String> methodSignature = new ParseSmali().parseLines(apkFile);
		Scanner sc = new Scanner(System.in);
		while(true){
			String reading = sc.nextLine();
			
			if(reading.equals("0")) break;
			else if(reading.equals("1")){ //setup
				try {
					jdb.setMonitorStatus(true);
					for(int i=0;i<100;i++){
						String string = methodSignature.get(i);
						jdb.setBreakPointLine(string.split(":")[0], Integer.parseInt(string.split(":")[1]));
					}
//					jdb.setBreakPointsAllLines(methodSignature);
				} catch (Exception e) { 
					e.printStackTrace();
				}
				System.out.println("setup all");
			}else if(reading.equals("2")){ //remove
				try {
					for(int i=0;i<100;i++){
						String string = methodSignature.get(i); 
						jdb.clearBreakPointLine(string.split(":")[0], Integer.parseInt(string.split(":")[1]));
					}
//					jdb.clearBreakPointsAllLines(methodSignature);
				} catch (Exception e) { 
					e.printStackTrace();
				}
				System.out.println("remove all");
			}else if(reading.equals("3")){ //destroy 
				try {
					jdb.exitJDB();
				} catch (Exception e) { 
					e.printStackTrace();
				}
				System.out.println("destroy");
			}else if(reading.equals("4")){ //init
				if(jdb!=null)
				try { jdb.exitJDB();
				} catch (Exception e1) {  }
				jdb = new JDBStuff();
				try {
					jdb.initJDB(apkFile);
				} catch (Exception e) {
					e.printStackTrace(); 
				}
				System.out.println("init");
			}else if(reading.equals("9")){ //information
				jdb.getMethodCoverage();
			}else if(reading.equals("8")){ //information
				System.out.println(methodSignature.get(0));
			}else if(reading.equals("a")){ //information
				try {
					jdb.setMonitorStatus(true);
					for(int i=0;i<100;i++){
						String string = methodSignature.get(i);
						jdb.setBreakPointLine(string.split(":")[0], Integer.parseInt(string.split(":")[1]));
					}
				} catch (Exception e) { 
					e.printStackTrace();
				}
				System.out.println("setup 100 lines");
			}else if(reading.equals("b")){ //information
				try {
					for(int i=0;i<100;i++){
						String string = methodSignature.get(i); 
						jdb.clearBreakPointLine(string.split(":")[0], Integer.parseInt(string.split(":")[1]));
					}
				} catch (Exception e) { 
					e.printStackTrace();
				}
				System.out.println("remove 100 lines");
			}
		}
		
		sc.close();
	}

}
