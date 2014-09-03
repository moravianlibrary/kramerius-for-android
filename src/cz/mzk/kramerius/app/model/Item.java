package cz.mzk.kramerius.app.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {

	private String pid;
	private String issn;
	private String model;
	private String date;
	private String title;
	private String rootTitle;
	private String rootPid;
	private boolean children;
	private String pdf;

	private String author;
	
	//periodicalVolume
	private String year;
	private String volumeNumber;
	
	//periodicalItem
	private String issueNumber;
	private String periodicalItemDate;
	private String partNumber;
	
	public Item() {

	}

	public Item(Parcel in) {
		pid = in.readString();
		issn = in.readString();
		model = in.readString();
		date = in.readString();
		title = in.readString();
		rootTitle = in.readString();
		rootPid = in.readString();
		children = in.readByte() != 0;
		pdf = in.readString();
		author = in.readString();
		year = in.readString();
		volumeNumber = in.readString();
		issueNumber = in.readString();
		periodicalItemDate = in.readString();
		partNumber = in.readString();
	}

	public String getPid() {
		return pid;
	}

	public String getIssn() {
		return issn;
	}

	public String getModel() {
		return model;
	}

	public String getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void setIssn(String issn) {
		this.issn = issn;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "caption: " + title + ", pid: " + pid + ", model: " + model + ", date: " + date + ", issn: " + issn;
	}

	public String getRootTitle() {
		return rootTitle;
	}

	public String getRootPid() {
		return rootPid;
	}

	public void setRootTitle(String rootTitle) {
		this.rootTitle = rootTitle;
	}

	public void setRootPid(String rootPid) {
		this.rootPid = rootPid;
	}

	public void setHasChildre(boolean children) {
		this.children = children;
	}

	public boolean hasChildren() {
		return children;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPdf() {
		return pdf;
	}

	public void setPdf(String pdf) {
		this.pdf = pdf;
	}
	
	
	
	

	public String getYear() {
		return year;
	}

	public String getVolumeNumber() {
		return volumeNumber;
	}

	public String getIssueNumber() {
		return issueNumber;
	}

	public String getPeriodicalItemDate() {
		return periodicalItemDate;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setVolumeNumber(String volumeNumber) {
		this.volumeNumber = volumeNumber;
	}

	public void setIssueNumber(String issueNumber) {
		this.issueNumber = issueNumber;
	}

	public void setPeriodicalItemDate(String periodicalItemDate) {
		this.periodicalItemDate = periodicalItemDate;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}





	public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
		public Item createFromParcel(Parcel in) {
			return new Item(in);
		}

		public Item[] newArray(int size) {
			return new Item[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(pid);
		dest.writeString(issn);
		dest.writeString(model);
		dest.writeString(date);
		dest.writeString(title);
		dest.writeString(rootTitle);
		dest.writeString(rootPid);
		dest.writeByte((byte) (children ? 1 : 0));
		dest.writeString(pdf);
		dest.writeString(author);
		dest.writeString(year);
		dest.writeString(volumeNumber);
		dest.writeString(issueNumber);
		dest.writeString(periodicalItemDate);
		dest.writeString(partNumber);
	}


	
}
