package cz.mzk.kramerius.app.model;

import java.util.List;

import cz.mzk.kramerius.app.api.K5Api;


public class ParentChildrenPair {
	private Item parent;
	private List<Item> children;
	private int status;

	public ParentChildrenPair(Item parent, List<Item> children, int status) {
		this.parent = parent;
		this.children = children;
		this.status = status;
	}

	public ParentChildrenPair(Item parent, List<Item> children) {
		this(parent, children, K5Api.STATUS_UNKNOWN);
	}	
	
	public int getStatus() {
		return status;
	}
	

	public Item getParent() {
		return parent;
	}

	public List<Item> getChildren() {
		return children;
	}
}
