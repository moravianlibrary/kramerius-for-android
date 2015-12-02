package cz.mzk.kramerius.app.metadata;

import java.util.ArrayList;
import java.util.List;

public class PhysicalDescription {

    private String extent;
    private List<String> notes;

    public PhysicalDescription() {
        notes = new ArrayList<String>();
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    public void addNote(String note) {
        notes.add(note);
    }


    public boolean isEmpty() {
        return extent == null && notes.isEmpty();
    }


    public String removeWhiteSpaces(String string) {
        if (string == null || !string.contains("  ")) {
            return string;
        }
        return removeWhiteSpaces(string.replace("  ", " "));
    }

    public String getScale() {
        if (extent == null || !extent.contains(";")) {
            return null;
        }
        return extent.substring(0, extent.indexOf(";"));
    }

    public String getExtent() {
        if (extent == null) {
            return null;
        }
        if (!extent.contains(";")) {
            return extent;
        }
        if (extent.indexOf(";") == extent.length() - 1) {
            return null;
        }
        return extent.substring(extent.indexOf(";") + 1, extent.length());
    }

}
