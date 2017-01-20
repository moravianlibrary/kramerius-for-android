package cz.mzk.kramerius.app.search;

import cz.mzk.kramerius.app.util.Logger;

/**
 * Created by rychtar on 19.01.17.
 */

public class Query {


    private static final String LOG_TAG = Query.class.getSimpleName();

    public static final String ACCESSIBILITY_ALL = "all";
    public static final String ACCESSIBILITY_PUBLIC = "public";
    public static final String ACCESSIBILITY_PRIVATE = "private";

    private String query;
    private String accessibility = ACCESSIBILITY_PUBLIC;



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
        return q;
    }







    public boolean isActive(String code, String value) {
        Logger.debug(LOG_TAG, "isActive - code: " + code + ", value: " + value);
        if(SearchQuery.POLICY.equals(code)) {
            return accessibility.equals(value);
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
        return false;
    }

}
