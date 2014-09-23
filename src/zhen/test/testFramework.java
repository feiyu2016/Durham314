package zhen.test;

import inputGeneration.RunTimeInfo;
import inputGeneration.StaticInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import zhen.framework.Framework;

public class testFramework {

	public static void main(String[] args) throws Exception {
//		String path = "APK/signed_KitteyKittey.apk";
		
		String path = "APK/signed_backupHelper.apk";
		
		File f = new File(path);
//		System.out.println("file exists:"+f.exists());
//		RunTimeInfo.installApp(f);
//		System.out.println("installing finished");
		StaticInfo.initAnalysis(f, false);
		

		
		
//		StaticInfo.methodAnalysis(new File(path), "com.example.backupHelper.BackupActivity", "boolean onChildClick(android.widget.ExpandableListView, android.view.View, int, int, long)".replace(",", " "));
		
		Map<String,Object> att = new HashMap<String,Object>();
//		ArrayList<String> tmp = new ArrayList<String>();
//		tmp.add("BackupActivity:checkRoot");
//		tmp.add("BackupActivity:onChildClick");
//		att.put("pattern", tmp);
		
		
		
//		String method  = //"com.example.backupHelper.BackupActivity: boolean onMenuItemSelected(int android.view.MenuItem)";
//		//"com.example.backupHelper.BackupActivity: void onCreate(android.os.Bundle)";
//		"com.example.backupHelper.BackupTask$1: boolean onKey(android.content.DialogInterface int android.view.KeyEvent)";
//		
//		att.put("method", method);
//		String[] targets = {
//				"com.example.testapp1.Leaf2,stub,button1,android:onClick",
//				"com.example.testapp1.SecondAct,stub,button2,android:onClick",	
//		};
//		
//		att.put("targets", targets);
		
		String[] methods = new String[]{
//				"com.example.backupHelper.BackupTask$1: boolean onKey(android.content.DialogInterface int android.view.KeyEvent)",	
		
//				"com.cs141.kittey.kittey.MainKitteyActivity: void nextButton(android.view.View)"
				
				"com.example.backupHelper.BackupActivity: void onCreate(android.os.Bundle)",

				"com.example.backupHelper.BackupActivity: boolean onCreateOptionsMenu(android.view.Menu)",

				"com.example.backupHelper.BackupActivity: boolean onMenuItemSelected(int android.view.MenuItem)",
				
				"com.example.backupHelper.BackupActivity: boolean onChildClick(android.widget.ExpandableListView android.view.View int int long)",
				
				
		};
		
		for(String s : methods){
			s.replace(",", "");
		}
		
		
		
		
		att.put("methods", methods);
		
		
		Framework frame = new Framework(path,att);
		
		frame.init();
		frame.execute();
		frame.terminate();
	}

}
