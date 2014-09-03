package cz.mzk.kramerius.app.model;

public class Metadata {
	private String note;
	private String title;
	private String subtitle;
	private String publisher;
	private String issn;
	private String issuedDate;
	private String authorName;
	private String authorDate;
	
	public Metadata() {
		
	}
	
	public String getNote() {
		return note;
	}
	public String getTitle() {
		return title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public String getPublisher() {
		return publisher;
	}
	public String getIssn() {
		return issn;
	}
	public String getIssuedDate() {
		return issuedDate;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public void setIssn(String issn) {
		this.issn = issn;
	}
	public void setIssuedDate(String issuedDate) {
		this.issuedDate = issuedDate;
	}

	public String getAuthorName() {
		return authorName;
	}

	public String getAuthorDate() {
		return authorDate;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public void setAuthorDate(String authorDate) {
		this.authorDate = authorDate;
	}
	
	
	
}
