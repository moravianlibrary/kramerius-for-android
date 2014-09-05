package cz.mzk.kramerius.app.metadata;

public class Author {

	private String name;
	private String date;

	public Author() {

	}

	public Author(String name, String date) {
		this.name = name;
		this.date = date;
	}

	public boolean isEmpty() {
		return name == null && date == null;
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
