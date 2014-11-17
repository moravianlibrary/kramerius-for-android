package cz.mzk.kramerius.app.search;

import java.util.Locale;

import cz.mzk.kramerius.app.util.ModelUtil;

public class SearchQuery {

	public static final String AUTHOR = "dc.creator";
	public static final String TITLE = "dc.title";
	public static final String LANGUAGE = "language";
	public static final String ISSN = "issn";
	public static final String ISBN = "isbn";
	public static final String DDT = "ddt";
	public static final String MDT = "mdt";	
	public static final String POLICY = "dostupnost";
	public static final String MODEL = "document_type";
	public static final String COLLECTION = "collection";
	public static final String DATE_BEGIN = "datum_begin";
	public static final String DATE_END = "datum_end";
	public static final String IDENTIFIER = "dc.identifier";
	public static final String YEAR = "rok";
	public static final String SYSNO = "sysno";
	public static final String SIGNATURE = "signature";
	public static final String KEYWORDS = "keywords";
	public static final String OCR = "text_ocr";
	

	private String mQuery;
	boolean hasModel = false;
	boolean hasFulltext = false;
	
	public SearchQuery() {
		mQuery = "";
	}


	public SearchQuery identifier(String key, String value) {
		String iv =  key + "\\:" + value.trim();
		return add(IDENTIFIER, iv, false, false);
	}
	
	public SearchQuery add(String key, String value, boolean substring, boolean toLowerCase) {
		if (value != null && !value.trim().isEmpty()) {
			String v = value.trim();
			if(toLowerCase) {
				v = v.toLowerCase(Locale.ENGLISH);
			}
			if (!mQuery.isEmpty()) {
				mQuery = mQuery + " AND ";
			}			
			if(MODEL.equals(key)) {
				hasModel = true;
			}
			mQuery = mQuery + key + ":" + (substring ? "*" : "") + v + (substring ? "*" : "");
		}
		return this;
	}

	public SearchQuery fulltext(String value) {
		hasFulltext = true;
		return add(OCR, value, false, true);
	}
	
	public SearchQuery add(String key, String value) {
		return add(key, value, true, true);
	}

	public SearchQuery neg(String key, String value, boolean substring, boolean toLowerCase) {
		return add("-" + key, value, substring, toLowerCase);
	}	

	public SearchQuery neg(String key, String value) {
		return neg(key, value, true, true);
	}		
	
	public SearchQuery date(int begin, int end) {		
		String value = "[" + begin + " TO " + end + "]";
		if(begin < 1) {
			neg(YEAR, "0", false, false);
		}
		return add(YEAR, value, false, false);
	}	
	
	
	public SearchQuery virtualCollection(String pid) {
		if (!mQuery.isEmpty()) {
			mQuery = mQuery + " AND ";
		}
		mQuery = mQuery + COLLECTION + ":\"" + pid + "\"";
		return this;
	}

	
	private SearchQuery allModels() {
		if (!mQuery.isEmpty()) {
			mQuery = mQuery + " AND ";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append("document_type:" + ModelUtil.MONOGRAPH).append(" OR ");
		sb.append("document_type:" + ModelUtil.PERIODICAL).append(" OR ");
		sb.append("document_type:" + ModelUtil.GRAPHIC).append(" OR ");
		sb.append("document_type:" + ModelUtil.ARCHIVE).append(" OR ");
		sb.append("document_type:" + ModelUtil.MANUSCRIPT).append(" OR ");
		sb.append("document_type:" + ModelUtil.MAP).append(" OR ");
		sb.append("document_type:" + ModelUtil.SHEET_MUSIC).append(" OR ");
		sb.append("document_type:" + ModelUtil.SOUND_RECORDING);
		if(hasFulltext) {
			sb.append(" OR ").append("document_type:" + ModelUtil.PAGE);
		}
		sb.append(")");

		mQuery = mQuery + sb.toString();

		return this;
	}

	public String build() {
		if(!hasModel) {
			allModels();
			hasModel = true;
		}
		if (mQuery.isEmpty()) {
			return "*:*";
		}
		return mQuery;
	}

}
