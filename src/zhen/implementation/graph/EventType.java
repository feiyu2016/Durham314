package zhen.implementation.graph;

import java.util.HashMap;
import java.util.Map;

public class EventType {
 
	public final static String SETUP = "setup";
	public final static String LAUNCH = "launch";
	public final static String ONBACK = "onBack";
	public final static String ONCLICK = "android:onClick";
	public final static String PRESS = "press";
	public final static String ADBCOMMAND = "adbcommand";
	
	
	public final static int iSETUP = -1;
	public final static int iLAUNCH = 0;
	public final static int iONBACK = 1;
	public final static int iONCLICK = 2;
	public final static int iPRESS = 3;
	public final static int iADBCOMMAND = 4;
	
	private static Map<String,Integer> variableMap;
	static{
		variableMap = new HashMap<String,Integer>();
		variableMap.put(SETUP, iSETUP);
		variableMap.put(LAUNCH, iLAUNCH);
		variableMap.put(ONBACK, iONBACK);
		variableMap.put(ONCLICK, iONCLICK);
		variableMap.put(PRESS, iPRESS);
		variableMap.put(ADBCOMMAND, iADBCOMMAND);
	}
	
	public static int stringToInt(String input){
		if(variableMap.containsKey(input)){
			return variableMap.get(input);
		}else{
			return -1;
		}
	}
}
