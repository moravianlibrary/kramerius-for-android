package cz.mzk.kramerius.app.viewer;

import java.util.List;

import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;

/**
 * @author Martin Řehánek
 * 
 */
public interface IPageViewerFragment {

	public abstract void setEventListener(EventListener eventListener);

	/**
	 * Populates fragment with page data.
	 * 
	 * @param domain
	 *            Domain of the Kramerius web server, for example
	 *            "kramerius.mzk.cz".
	 * @param pagePids
	 *            List of pids of pages to show.
	 */
	public abstract void populate(String domain, List<String> pagePids);

	/**
	 * 
	 * @return True if populate() has been called (at least once) already.
	 */
	public abstract boolean isPopulated();

	/**
	 * 
	 * @return Index of page being shown now.
	 */
	public abstract int getCurrentPageIndex();

	/**
	 * 
	 * @return Index of next page or null if current page is last one.
	 */
	public abstract Integer getNextPageIndex();

	/**
	 * 
	 * @return Index of previous page or null if current page is first one.
	 */
	public abstract Integer getPreviousPageIndex();

	/**
	 * Shows page by index.
	 * 
	 * @param pageIndex
	 */
	public abstract void showPage(int pageIndex);

	/**
	 * 
	 * @param pageIndex
	 * @return Pid of page by index.
	 */
	public abstract String getPagePid(int pageIndex);

	/**
	 * 
	 * @return Number of pages.
	 */
	public abstract int getPageNumber();

	/**
	 * Sets view mode for all pages from now on.
	 * 
	 * @param mode
	 */
	public void setViewMode(ViewMode mode);

	
	
	/**
	 * Sets background color.
	 * 
	 * @param color
	 */
	public abstract void setBackgroundColor(int color);
	
	
	public interface EventListener {
		/**
		 * Called after fragment populated by populate().
		 */
		public void onReady();

		public void onAccessDenied();

		/**
		 * 
		 * @param statusCode
		 *            http response status code. Can be null.
		 */
		public void onNetworkError(Integer statusCode);

		public void onInvalidDataError(String errorMessage);

		/**
		 * Called after single tap that has not been used internally by
		 * fragment.
		 * 
		 * @param x
		 *            X coordinates of tap.
		 * @param y
		 *            Y coordinates of tap.
		 */
		public void onSingleTap(float x, float y);

	}

}