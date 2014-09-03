package cz.mzk.kramerius.app.model;

import java.util.List;


public class ParentChildrenPair {
	private Item parent;
	private List<Item> children;

	public ParentChildrenPair(Item parent, List<Item> children) {
		this.parent = parent;
		this.children = children;
	}

	public Item getParent() {
		return parent;
	}

	public List<Item> getChildren() {
		return children;
	}
}
