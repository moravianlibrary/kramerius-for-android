package cz.mzk.kramerius.app.adapter;

import java.util.HashMap;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.viewer.SinglePageViewerFragment;

public class PageViewPagerAdapter  extends FragmentStatePagerAdapter {

	private List<Item> mPages;
	private String mDomain;
	private int mBackground;
	private ViewMode mViewMode;
	
	HashMap<Integer, SinglePageViewerFragment> mPageReferenceMap;
	
    
	public PageViewPagerAdapter(FragmentManager fragmentManager, String domain, List<Item> pages, int bgColor, ViewMode viewMode) {
        super(fragmentManager);
        mPages = pages;
        mDomain = domain;
        mBackground = bgColor;
        mViewMode = viewMode;
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
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        SinglePageViewerFragment f = mPageReferenceMap.get(position); 
        if(f != null && f.getActivity() != null) {
        	f.onDestroy();
        }
        mPageReferenceMap.remove(position);
    }
    
    @Override
    public Fragment getItem(int position) {
    	SinglePageViewerFragment fragment = SinglePageViewerFragment.newInstance(mDomain, mPages.get(position).getPid(), mBackground, mViewMode);
    	//mPageReferenceMap.put(position, fragment);
    	return fragment;
    }
    
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
    	SinglePageViewerFragment fragment = (SinglePageViewerFragment) super.instantiateItem(container, position);
    	mPageReferenceMap.put(position, fragment);
        return fragment;
    }

    public void refreshFragment() {
    	
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }


}

