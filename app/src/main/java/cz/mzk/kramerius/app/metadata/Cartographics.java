package cz.mzk.kramerius.app.metadata;


public class Cartographics {

    private String scale;
    private String coordinates;

    public Cartographics() {
    }

    public boolean isEmpty() {
        return coordinates == null && scale == null;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

}
