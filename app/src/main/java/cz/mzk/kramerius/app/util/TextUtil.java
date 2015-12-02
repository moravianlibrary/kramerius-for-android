package cz.mzk.kramerius.app.util;

import java.util.List;

public class TextUtil {

    public static String shorten(String text, int length) {
        if (text.length() <= length) {
            return text;
        }
        return text.substring(0, length);
    }

    public static String shortenforActionBar(String text) {
        String title = text;
        if (title == null) {
            return "";
        }
        if (title.length() > 25) {
            title = title.substring(0, 24) + "...";
        }
        return title;
    }

    public static String parseTitle(String text) {
        String title = text;
        if (title.contains(" : ")) {
            title = title.substring(0, title.indexOf(" : "));
        }
        return title;
    }

    public static String writeNotes(List<String> notes) {
        String s = "";
        for (int i = 0; i < notes.size(); i++) {
            String note = notes.get(i).trim().replaceAll("\n", "");
            s += removeWhiteSpaces(note);
            if (i < notes.size() - 1) {
                s += "\n";
            }
        }
        return s;
    }

    public static String removeWhiteSpaces(String string) {
        if (string == null || !string.contains("  ")) {
            return string;
        }
        return removeWhiteSpaces(string.replace("  ", " "));
    }

}
