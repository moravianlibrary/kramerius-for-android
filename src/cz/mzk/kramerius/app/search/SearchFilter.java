package cz.mzk.kramerius.app.search;

import android.view.View;

public interface SearchFilter {

	
	public String getKey();
	
	public String getName();
	
	public int validate();
	
	public View getView();
	
	public void addToQuery(SearchQuery query);
	
	
}
