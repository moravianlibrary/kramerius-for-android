package cz.mzk.kramerius.app.model;

public class Domain {

	private String title;
	private String domain;
	private String protocol;
	private int logo;
	private boolean unlocked; 
	
	public Domain() {

	}

	public Domain(boolean unlocked, String title, String protocol, String domain, int logo) {
		this.unlocked = unlocked;
		this.title = title;
		this.domain = domain;
		this.protocol = protocol;
		this.logo = logo;
	}

	public String getTitle() {
		return title;
	}

	public boolean isUnlocked() {
		return unlocked;
	}
	
	public String getDomain() {
		return domain;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getLogo() {
		return logo;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setLogo(int logo) {
		this.logo = logo;
	}

	public String getUrl() {
		return getProtocol() + "://" + getDomain();
	}
	
}
