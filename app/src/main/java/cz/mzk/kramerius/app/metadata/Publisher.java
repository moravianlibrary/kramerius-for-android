package cz.mzk.kramerius.app.metadata;

public class Publisher {

    private String name;
    private String date;
    private String place;

    public Publisher() {

    }

    public Publisher(String name, String date, String place) {
        this.name = name;
        this.date = date;
        this.place = place;
    }

    public boolean isEmpty() {
        return name == null && place == null && date == null;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getPlace() {
        return place;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPlace(String place) {
        this.place = place;
    }

}
