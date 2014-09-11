package cz.mzk.kramerius.app.metadata;

import java.util.ArrayList;
import java.util.List;

public class Metadata {

	private String pid;

	private String issn;
	private String isbn;
	private String ccnb;
	private String oclc;

	private Part part;
	private TitleInfo titleInfo;
	private Location location;

	private List<Author> authors;
	private List<Publisher> publishers;

	private List<String> notes;

	private List<String> languages;
	private List<String> keywords;
	private String docAbstract;

	private PhysicalDescription physicalDescription;
	private Cartographics cartographics;

	public Metadata() {
		notes = new ArrayList<String>();
		authors = new ArrayList<Author>();
		publishers = new ArrayList<Publisher>();
		languages = new ArrayList<String>();
		keywords = new ArrayList<String>();
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public List<String> getNotes() {
		return notes;
	}

	public Cartographics getCartographics() {
		return cartographics;
	}

	public void setCartographics(Cartographics cartographics) {
		this.cartographics = cartographics;
	}

	public PhysicalDescription getPhysicalDescription() {
		return physicalDescription;
	}

	public void setPhysicalDescription(PhysicalDescription physicalDescription) {
		this.physicalDescription = physicalDescription;
	}

	public void addLanguage(String language) {
		languages.add(language);
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void addKayword(String keyword) {
		keywords.add(keyword);
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setAbstract(String a) {
		docAbstract = a;
	}

	public String getAbstract() {
		return docAbstract;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public Part getPart() {
		return part;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public void setTitleInfo(TitleInfo titleInfo) {
		this.titleInfo = titleInfo;
	}

	public TitleInfo getTitleInfo() {
		return titleInfo;
	}

	public void addNote(String note) {
		this.notes.add(note);
	}

	public String getIssn() {
		return issn;
	}

	public void setIssn(String issn) {
		this.issn = issn;
	}

	public String getCcnb() {
		return ccnb;
	}

	public String getOclc() {
		return oclc;
	}

	public void setCcnb(String ccnb) {
		this.ccnb = ccnb;
	}

	public void setOclc(String oclc) {
		this.oclc = oclc;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public void addAuthor(Author author) {
		authors.add(author);
	}

	public void addPublisher(Publisher publisher) {
		publishers.add(publisher);
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public List<Publisher> getPublishers() {
		return publishers;
	}

}
