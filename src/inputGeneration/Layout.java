package inputGeneration;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Layout {

	private Node Node;
	private String Name;
	private String Type;
	private ArrayList<ViewNode> viewNodes;
	private boolean isCustomLayout;
	private boolean hasInclude;
	
	public Layout(String name, Node layoutNode, boolean isCustom) {
		Name = name;
		viewNodes = new ArrayList<ViewNode>();
		Node = layoutNode;
		isCustomLayout = isCustom;
		hasInclude = false;
		checkInclude();
	}
	
	private void checkInclude() {
		Element e = (Element) Node;
		NodeList nl = e.getElementsByTagName("*");
		for (int i = 0, len = nl.getLength(); i < len; i++)
			if (nl.item(i).getNodeName().equals("include")) {
				hasInclude = true;
				return;
			}
	}
	
	public String getType() {
		return Type;
	}
	
	public void addNode(ViewNode node) {
		viewNodes.add(node);
	}
	
	public boolean isCustomLayout() {
		return isCustomLayout;
	}
	
	public boolean hasInclude() {
		return hasInclude;
	}
	
	public ArrayList<ViewNode> getAllViewNodes() {
		return viewNodes;
	}
	
	public Node getNode() {
		return Node;
	}

	public String getName() {
		return Name;
	}
	
	public ViewNode getViewNodeById(String id) {
		for (ViewNode vN: viewNodes)
			if (vN.getID().equals(id))
				return vN;
		return null;
	}
	
	public ArrayList<ViewNode> getLeavingViewNodes() {
		ArrayList<ViewNode> result = new ArrayList<ViewNode>();
		for (ViewNode vN : viewNodes)
			if (vN.hasLeavingEventHandlers())
				result.add(vN);
		return result;
	}
	
	public ArrayList<ViewNode> getStayingViewNodes() {
		ArrayList<ViewNode> result = new ArrayList<ViewNode>();
		for (ViewNode vN : viewNodes)
			if (!vN.hasLeavingEventHandlers())
				result.add(vN);
		return result;
	}
	
}
