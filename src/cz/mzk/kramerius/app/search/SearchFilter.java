package cz.mzk.kramerius.app.search;


public interface SearchFilter {

	
	public String getKey();
	
	public String getName();
	
	public int validate();
		
	public void addToQuery(SearchQuery query);
	
	
}
