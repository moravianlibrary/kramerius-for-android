package cz.mzk.kramerius.app.model;

public class Domain {

	private String title;
	private String subtitle;
	private String domain;
	private String protocol;
	private int logo;

	public Domain() {

	}

	public Domain(String title, String subtitle, String protocol, String domain, int logo) {
		this.title = title;
		this.subtitle = subtitle;
		this.domain = domain;
		this.protocol = protocol;
		this.logo = logo;
	}

	public String getTitle() {
		return title;
	}

	public String getSubtitle() {
		return subtitle;
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

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
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

}
