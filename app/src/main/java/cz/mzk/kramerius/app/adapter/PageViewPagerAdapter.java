package cz.mzk.kramerius.app.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.viewer.SinglePageViewerFragment;
import cz.mzk.kramerius.app.xml.AltoParser;

public class PageViewPagerAdapter  extends FragmentStatePagerAdapter {

    private static final String TAG = PageViewPagerAdapter.class.getSimpleName();

    private List<Item> mPages;
	private String mDomain;
	private int mBackground;
	private ViewMode mViewMode;
	
	private HashMap<Integer, SinglePageViewerFragment> mPageReferenceMap;
    //todo: tohle se bude uchovavat v aktivite kvuli zmene orientace a znovuvytvareni fragmentu
    private final Map<Integer, Set<AltoParser.TextBox>> mTextboxMap;


    public PageViewPagerAdapter(FragmentManager fragmentManager, String domain, List<Item> pages, int bgColor, ViewMode viewMode) {
        super(fragmentManager);
        mPages = pages;
        mDomain = domain;
        mBackground = bgColor;
        mViewMode = viewMode;
        mPageReferenceMap = new HashMap<Integer, SinglePageViewerFragment>();
        mTextboxMap = new HashMap<>();
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
        Log.v(TAG,"getItem, position: " + position);
    	SinglePageViewerFragment fragment = SinglePageViewerFragment.newInstance(mDomain, mPages.get(position).getPid(), mBackground, mViewMode);
    	//mPageReferenceMap.put(position, fragment);
        // TODO: 1.12.15 Should this be done in getItem or instantiateItem?
        fragment.setTextBoxes(mTextboxMap.get(position));
    	return fragment;
    }

    // TODO: 1.12.15 really needed to override this? See http://developer.android.com/intl/ko/reference/android/support/v4/app/FragmentStatePagerAdapter.html
    // I don't get this mPageReferenceMap. Isn't FragmentStatePagerAdapter suppposed implement this fragment caching on its own?
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.v(TAG,"instantiateItem, position: " + position);
    	SinglePageViewerFragment fragment = (SinglePageViewerFragment) super.instantiateItem(container, position);
    	mPageReferenceMap.put(position, fragment);
        // TODO: 1.12.15 Should this be done in getItem or instantiateItem?
        fragment.setTextBoxes(mTextboxMap.get(position));
        return fragment;
    }

    public void refreshFragment() {
    	
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }

    public void setTextBoxes(int position, Set<AltoParser.TextBox> boxes){
        if(boxes!=null) {
            Log.i(TAG, String.format("setting text boxes for page %d, boxes: %d", position, boxes.size()));
        }else{
            Log.i(TAG, String.format("setting text boxes for page %d, boxes: null", position));
        }
        mTextboxMap.put(position, boxes);
        getFragment(position).setTextBoxes(boxes);
        //((SinglePageViewerFragment)getItem(position)).setTextBoxes(boxes);
    }



}

