package cz.mzk.kramerius.app.search;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.util.Logger;

/**
 * Created by rychtar on 19.01.17.
 */

public class Query {


    private static final String LOG_TAG = Query.class.getSimpleName();

    public static final String ACCESSIBILITY_ALL = "all";



    private String query;
    private String accessibility = "public";
    private List<String> authors = new ArrayList<>();


    public Query(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public String getAccessibility() {
        return accessibility;
    }

    public  void setAccessibility(String accessibility) {
        this.accessibility = accessibility;
    }

    public boolean hasQuery() {
        return query != null;
    }

    public List<String> getAuthors() {
        return authors;
    }

    private void switchAuthor(String author) {
        if(authors.contains(author)) {
            authors.remove(author);
        } else {
            authors.add(author);
        }
    }

    public String buildQuery() {
        String q = "";
        if(hasQuery()) {
            q = "_query_:\"{!dismax qf='dc.title^1000 text^0.0001' v=$q1}\" AND level:0";
        } else {
            q = "level:0";
        }
        if(!ACCESSIBILITY_ALL.equals(getAccessibility())) {
            q+= " AND " + SearchQuery.POLICY + ":" + getAccessibility();
        }
        if(!authors.isEmpty()) {
            q+= " AND " + SearchQuery.AUTHOR_FACET + ":" + join(authors);
        }
        return q;
    }


    public String buildFacetQuery(String facet) {
        String q = "";
        if(hasQuery()) {
            q = getQuery() + " AND level:0";
        } else {
            q = "level:0";
        }
        if(!SearchQuery.POLICY.equals(facet) && !ACCESSIBILITY_ALL.equals(getAccessibility())) {
            q+= " AND " + SearchQuery.POLICY + ":" + getAccessibility();
        }
        if(!SearchQuery.AUTHOR_FACET.equals(facet) && !authors.isEmpty()) {
            q+= " AND " + SearchQuery.AUTHOR_FACET + ":" + join(authors);
        }
        return q;
    }



    public boolean isActive(String code, String value) {
        Logger.debug(LOG_TAG, "isActive - code: " + code + ", value: " + value);
        if(SearchQuery.POLICY.equals(code)) {
            return accessibility.equals(value);
        }
        if(SearchQuery.AUTHOR_FACET.equals(code)) {
            return authors.contains(value);
        }
        return false;
    }



    public boolean change(String code, String value) {
        Logger.debug(LOG_TAG, "Change - code: " + code + ", value: " + value);
        if(SearchQuery.POLICY.equals(code)) {
            if(accessibility.equals(value)) {
                return false;
            }
            setAccessibility(value);
            return true;
        }
        if(SearchQuery.AUTHOR_FACET.equals(code)) {
            switchAuthor(value);
            return true;
        }
        return false;
    }





    private String join(List<String> list) {
        if(list.isEmpty()) {
            return "";
        }
        if(list.size() == 1) {
            return escape(list.get(0));
        }
        String r = "(";
        for(int i = 0; i < list.size() - 1; i++) {
            r += escape(list.get(i)) + " OR ";
        }
        r += escape(list.get(list.size() - 1));
        r+= ")";
        return r;
    }


    private String escape(String s) {
        if(s == null) {
            return "";
        }
        return "\"" + s.replaceAll("\"", "%22") + "\"";
    }



}
