package inputGeneration;

import java.util.ArrayList;

public class Layout {

	private String Name;
	private String Type;
	private ArrayList<LayoutNode> Nodes;
	private boolean isCustomLayout;
	private boolean hasInclude;
	
	public Layout() {
		Name = "";
		Nodes = new ArrayList<LayoutNode>();
		isCustomLayout = false;
		hasInclude = false;
	}
	
}
