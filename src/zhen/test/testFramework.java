package zhen.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import zhen.framework.Framework;

public class testFramework {

	public static void main(String[] args) {
		String path = "APK/testApp1.apk";
		Map<String,Object> att = new HashMap<String,Object>();
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("com.example.testapp1.Target:traget1");
		tmp.add("com.example.testapp1.Target:traget2");
		att.put("pattern", tmp);
		
		String[] targets = {
				"com.example.testapp1.Leaf2,stub,button1,android:onClick",
				"com.example.testapp1.SecondAct,stub,button2,android:onClick",	
		};
		
		att.put("targets", targets);
		
		Framework frame = new Framework(path,att);
		
		frame.init();
		frame.execute();
		frame.terminate();
	}

}
