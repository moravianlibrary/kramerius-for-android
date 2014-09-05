package cz.mzk.kramerius.app.model;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.metadata.Author;
import cz.mzk.kramerius.app.metadata.Location;
import cz.mzk.kramerius.app.metadata.Part;
import cz.mzk.kramerius.app.metadata.Publisher;
import cz.mzk.kramerius.app.metadata.TitleInfo;

public class Metadata {
	
	private String issn;
	private String isbn;

	private Part part;
	private TitleInfo titleInfo;
	private Location location;
	
	private List<Author> authors;
	private List<Publisher> publishers;

	private List<String> notes;
	private List<String> physicalDescriptionNotes;
	

	public Metadata() {
		notes = new ArrayList<String>();
		physicalDescriptionNotes = new ArrayList<String>();
		authors = new ArrayList<Author>();
		publishers = new ArrayList<Publisher>();
	}

	public String writeNotes() {		
		String s = "";
		for(int i = 0; i < notes.size(); i++) {
			s+=notes.get(i);
			if(i < notes.size() - 1) {
				s+="\n";
			}
		}
		return s;
	}
	
	public List<String> getNotes() {
		return notes;
	}

	public String writePhysicalDescriptionNotes() {
		String s = "";
		for (String note : physicalDescriptionNotes) {
			s += note + "\n";
		}
		return s;
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

	public void addPhysicalDescriptionNote(String note) {
		this.physicalDescriptionNotes.add(note);
	}

	public String getIssn() {
		return issn;
	}

	public void setIssn(String issn) {
		this.issn = issn;
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
