package cz.mzk.kramerius.app.util;

import java.util.Locale;

public class LangUtils {

	private static final String LANG_CS = "cs";
	private static final String LANG_EN = "en";
	
	
	public static String getLanguage() {
		String lang = LANG_EN;
		if(LANG_CS.equals(Locale.getDefault().getLanguage())) {
			lang = LANG_CS;
		}
		return lang;
	}
	
}
