package zhen.framework;

import com.android.hierarchyviewerlib.models.ViewNode;

public class RunTimeLayout {
	private String actName;
	private ViewNode layoutRoot;
	public RunTimeLayout(String actName, ViewNode root){
		this.actName = actName;
		this.layoutRoot = root;
	}
	public String getActName() {
		return actName;
	}
	public ViewNode getLayoutRoot() {
		return layoutRoot;
	}
}
