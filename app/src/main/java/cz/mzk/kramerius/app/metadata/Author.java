package cz.mzk.kramerius.app.metadata;

import java.util.ArrayList;
import java.util.List;

public class Author {

	private String name;
	private String date;
	private List<String> roleCodes;

	public Author() {
		roleCodes = new ArrayList<String>();
	}

	public Author(String name, String date) {
		this();
		this.name = name;
		this.date = date;
	}
	
	public void addRole(String role) {
		roleCodes.add(role);
	}
	
	public List<String> getRoleCodes() {
		return roleCodes;
	}

	public boolean isEmpty() {
		return name == null && date == null && roleCodes.isEmpty();
	}

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
