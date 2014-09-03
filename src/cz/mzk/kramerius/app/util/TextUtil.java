package cz.mzk.kramerius.app.util;

public class TextUtil {

	public static String shorten(String text, int length) {
		if (text.length() <= length) {
			return text;
		}
		return text.substring(0, length);
	}
	
	public static String shortenforActionBar(String text) {
		String title = text;
		if(title == null) {
			return "";
		}
		if(title.length() > 25) {
			title = title.substring(0, 24) + "...";
		}
		return title;
	}	
}
