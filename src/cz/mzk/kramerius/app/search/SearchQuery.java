package cz.mzk.kramerius.app.search;

import cz.mzk.kramerius.app.util.ModelUtil;

public class SearchQuery {

	public static final String AUTHOR = "dc.creator";
	public static final String TITLE = "dc.title";
	public static final String LANGUAGE = "language";
	public static final String ISSN = "issn";
	public static final String POLICY = "dostupnost";
	public static final String MODEL = "document_type";

	private String mQuery;

	public SearchQuery() {
		mQuery = "";
	}

	public SearchQuery add(String key, String value) {
		if (value != null && !value.isEmpty()) {
			if (!mQuery.isEmpty()) {
				mQuery = mQuery + " AND ";
			}
			mQuery = mQuery + key + ":*" + value + "*";
		}
		return this;
	}

	public SearchQuery allModels() {
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
		sb.append(")");

		mQuery = mQuery + sb.toString();

		return this;
	}

	public String build() {
		if (mQuery.isEmpty()) {
			return "*:*";
		}
		return mQuery;
	}

}
