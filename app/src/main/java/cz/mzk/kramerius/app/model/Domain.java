package cz.mzk.kramerius.app.model;

public class Domain {

    private String code;
    private String title;
    private String domain;
    private String protocol;

    public Domain() {

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public String getDomain() {
        return domain;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUrl() {
        return getProtocol() + "://" + getDomain();
    }

    public String getLogo() {
        return "http://registr.digitalniknihovna.cz/libraries/" + this.code + "/logo";
    }

}
