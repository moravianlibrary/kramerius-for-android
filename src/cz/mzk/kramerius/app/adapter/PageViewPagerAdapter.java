package cz.mzk.kramerius.app.adapter;

import java.util.HashMap;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.viewer.IPageViewerFragment.EventListener;
import cz.mzk.kramerius.app.viewer.SinglePageViewerFragment;

public class PageViewPagerAdapter  extends FragmentStatePagerAdapter {

	private List<Item> mPages;
	private String mDomain;
	private int mBackground;
	private EventListener mEventListener;
	private ViewMode mViewMode;
	
	HashMap<Integer, SinglePageViewerFragment> mPageReferenceMap;
	
    
	public PageViewPagerAdapter(FragmentManager fragmentManager, String domain, List<Item> pages, int bgColor, ViewMode viewMode, EventListener eventListener) {
        super(fragmentManager);
        mPages = pages;
        mDomain = domain;
        mBackground = bgColor;
        mViewMode = viewMode;
        mEventListener = eventListener;
        mPageReferenceMap = new HashMap<Integer, SinglePageViewerFragment>();
    }

    @Override
    public int getCount() {
        if(mPages == null) {
        	return 0;
        }
    	return mPages.size();
    	
    }


    public SinglePageViewerFragment getFragment(int key) {
        return mPageReferenceMap.get(key);
    }    
    
    
    public void destroyItem(View container, int position, Object object) {
        super.destroyItem(container, position, object);
        mPageReferenceMap.remove(position);
    }
    
    @Override
    public Fragment getItem(int position) {
    	SinglePageViewerFragment fragment = SinglePageViewerFragment.newInstance(mDomain, mPages.get(position).getPid(), mBackground, mViewMode);
    	mPageReferenceMap.put(position, fragment);
    	return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }


}

