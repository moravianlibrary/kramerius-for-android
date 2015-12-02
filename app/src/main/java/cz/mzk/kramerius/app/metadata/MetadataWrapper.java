package cz.mzk.kramerius.app.metadata;

public class MetadataWrapper {

    private Metadata metadata;
    private String model;
    private boolean documentPrivate;

    public MetadataWrapper(Metadata metadata, String model, boolean documentPrivate) {
        this.metadata = metadata;
        this.model = model;
        this.documentPrivate = documentPrivate;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getModel() {
        return model;
    }

    public boolean isDocumentPrivate() {
        return documentPrivate;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setDocumentPrivate(boolean documentPrivate) {
        this.documentPrivate = documentPrivate;
    }

}
