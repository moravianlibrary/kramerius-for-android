package cz.mzk.kramerius.app.metadata;

public class Part {
	private String pageNumber;
	private String pageIndex;
	private String volumeNumber;
	private String issueNumber;
	private String partNumber;
	private String date;
	private String text;

	public Part() {

	}


	public boolean isEmpty() {
		return pageNumber == null && pageIndex == null && volumeNumber == null && partNumber == null && issueNumber == null && text == null && date == null;
	}


	public String getDate() {
		return date;
	}

	public String getText() {
		return text;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setText(String text) {
		this.text = text;
	}


	public String getPageNumber() {
		return pageNumber;
	}


	public String getPageIndex() {
		return pageIndex;
	}


	public String getVolumeNumber() {
		return volumeNumber;
	}


	public String getIssueNumber() {
		return issueNumber;
	}


	public String getPartNumber() {
		return partNumber;
	}


	public void setPageNumber(String pageNumber) {
		this.pageNumber = pageNumber;
	}


	public void setPageIndex(String pageIndex) {
		this.pageIndex = pageIndex;
	}


	public void setVolumeNumber(String volumeNumber) {
		this.volumeNumber = volumeNumber;
	}


	public void setIssueNumber(String issueNumber) {
		this.issueNumber = issueNumber;
	}


	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}
	
	
	
	public String getIssueTitle() {
		if((issueNumber == null || issueNumber.isEmpty()) && (partNumber == null || partNumber.isEmpty())) {
			return "";
		}		
		if(issueNumber == null || issueNumber.isEmpty()) {
			return partNumber;
		}
		if(partNumber != null && !partNumber.isEmpty()) {
			issueNumber += "/" + partNumber;	
		} 
		return issueNumber;				
	} 
	
	

}
