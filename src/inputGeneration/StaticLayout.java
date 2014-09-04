package inputGeneration;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StaticLayout {

	private Node Node;
	private String Name;
	private String Type;
	private ArrayList<StaticViewNode> viewNodes;
	private boolean isCustomLayout;
	private boolean hasInclude;
	
	public StaticLayout(String name, Node layoutNode, String type, boolean isCustom) {
		Name = name;
		viewNodes = new ArrayList<StaticViewNode>();
		Node = layoutNode;
		isCustomLayout = isCustom;
		hasInclude = false;
		Type = type;
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
	
	public void addNode(StaticViewNode node) {
		viewNodes.add(node);
	}
	
	public boolean isCustomLayout() {
		return isCustomLayout;
	}
	
	public boolean hasInclude() {
		return hasInclude;
	}
	
	public ArrayList<StaticViewNode> getAllViewNodes() {
		return viewNodes;
	}
	
	public Node getNode() {
		return Node;
	}

	public String getName() {
		return Name;
	}
	
	public StaticViewNode getViewNodeById(String id) {
		for (StaticViewNode vN: viewNodes)
			if (vN.getID().equals(id))
				return vN;
		return null;
	}
	
	public ArrayList<StaticViewNode> getLeavingViewNodes() {
		ArrayList<StaticViewNode> result = new ArrayList<StaticViewNode>();
		for (StaticViewNode vN : viewNodes)
			if (vN.hasLeavingEventHandlers())
				result.add(vN);
		return result;
	}
	
	public ArrayList<StaticViewNode> getStayingViewNodes() {
		ArrayList<StaticViewNode> result = new ArrayList<StaticViewNode>();
		for (StaticViewNode vN : viewNodes)
			if (!vN.hasLeavingEventHandlers())
				result.add(vN);
		return result;
	}
	
}
