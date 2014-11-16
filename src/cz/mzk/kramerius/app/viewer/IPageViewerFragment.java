package cz.mzk.kramerius.app.viewer;

import java.util.List;

import android.graphics.Rect;
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
	 * Shows page by index.
	 * 
	 * @param pageIndex
	 */
	public abstract void showPage(int pageIndex);

	/**
	 * 
	 * @return Number of pages.
	 */
	public abstract int getNumberOfPage();

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
		 * @param boundingBox
		 *            area containing the image or null
		 */
		public void onSingleTap(float x, float y, Rect boundingBox);
		
		
		public void onPageChanged(int index);

	}

}