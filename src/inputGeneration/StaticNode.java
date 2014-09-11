package inputGeneration;

import java.io.Serializable;
import java.util.ArrayList;

public class StaticNode implements Serializable{

	
	private boolean isMethod = false;
	private boolean isField = false;
	private String className;
	private String name;
	private String signature;
	private ArrayList<String> outCallTargets;
	private ArrayList<String> inCallSources;
	
	public StaticNode(String c, String s, String flag) {
		className = c;
		signature = s;
		outCallTargets = new ArrayList<String>();
		inCallSources = new ArrayList<String>();
		if (flag.equals("Method")) {
			isMethod = true;
			name = s.substring(s.indexOf(" ")+1, s.indexOf("("));
		}
		else if (flag.equals("Field")) {
			isField = true;
			name = s.split(" ")[1];
		}
	}
	
	public String getDeclaringClassName() {
		return className;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public ArrayList<String> getOutCallTargets() {
		return outCallTargets;
	}
	
	public ArrayList<String> getInCallSources() {
		return inCallSources;
	}
	
	public void addOutCall(StaticNode targetNode, String lineNo) {
		if (!outCallTargets.contains(targetNode.toString() + ":" + lineNo))
			outCallTargets.add(targetNode.toString() + ":" + lineNo);
	}
	
	public void addInCall(StaticNode sourceNode, String lineNo) {
		if (!inCallSources.contains(sourceNode.toString() + ":" + lineNo))
			inCallSources.add(sourceNode.toString() + ":" + lineNo);

	}
	
	public boolean isMethod() {
		return isMethod;
	}
	
	public boolean isField() {
		return isField;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		if (isField)
			return "Field," + className + ":" + signature;
		else
			return className + ":" + signature;
	}
}
