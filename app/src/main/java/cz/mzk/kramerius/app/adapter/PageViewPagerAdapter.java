package cz.mzk.kramerius.app.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.search.TextBox;
import cz.mzk.kramerius.app.search.TextboxProvider;
import cz.mzk.kramerius.app.viewer.SinglePageViewerFragment;

public class PageViewPagerAdapter  extends FragmentStatePagerAdapter {

    private static final String TAG = PageViewPagerAdapter.class.getSimpleName();

    private final List<Item> mPages;
	private final String mDomain;
	private final int mBackground;
	private final ViewMode mViewMode;
	private final HashMap<Integer, SinglePageViewerFragment> mPageReferenceMap;
    private final TextboxProvider mTextboxProvider;

    public PageViewPagerAdapter(FragmentManager fragmentManager, String domain, List<Item> pages, int bgColor, ViewMode viewMode, TextboxProvider textboxProvider) {
        super(fragmentManager);
        //Log.e("test", "adapter: constructor");
        mPages = pages;
        mDomain = domain;
        mBackground = bgColor;
        mViewMode = viewMode;
        mPageReferenceMap = new HashMap<>();
        mTextboxProvider = textboxProvider;
    }

    @Override
    public int getCount() {
        if(mPages == null) {
        	return 0;
        }
    	return mPages.size();
    }

    public Object instantiateItem(ViewGroup container, int position) {
        SinglePageViewerFragment fragment = (SinglePageViewerFragment) super.instantiateItem(container,position);
        //Log.e("test", "adapter: instantiating item, position: " + position);
        mPageReferenceMap.put(position, fragment);
        fragment.setTextBoxes(mTextboxProvider.getTextBoxes(position));
        return fragment;
    }

    public SinglePageViewerFragment getFragment(int key) {
    	return mPageReferenceMap.get(key);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //Log.e("test", "adapter: destroyItem " + position);
        super.destroyItem(container, position, object);
        //Log.d(TAG, "destroyItem, position : " + position);
        SinglePageViewerFragment f = mPageReferenceMap.get(position);
        if(f != null && f.getActivity() != null) {
        	f.onDestroy();
        }
        mPageReferenceMap.remove(position);
    }

    @Override
    public Fragment getItem(int position) {
        //Log.e("test", "adapter: getItem " + position);
        Set<TextBox> textBoxes = mTextboxProvider.getTextBoxes(position);
        SinglePageViewerFragment fragment = mPageReferenceMap.get(position);
        //Log.v(TAG, String.format("getItem, position: %d, hit: %b, boxes: %s",position, fragment!=null, textBoxes == null? "null" : String.valueOf(textBoxes.size())));
        if(fragment == null){
            fragment = SinglePageViewerFragment.newInstance(mDomain, mPages.get(position).getPid(), mBackground, mViewMode);
            mPageReferenceMap.put(position, fragment);
        }
        fragment.setTextBoxes(textBoxes);
        return fragment;
   }

    public void refreshFragment(int position) {
        //Log.e("test", "adapter: refreshing fragment " + position);
        Set<TextBox> textBoxes = mTextboxProvider.getTextBoxes(position);
        //Log.v(TAG, String.format("refreshFragment, position: %d, boxes: %s",position,  textBoxes == null? "null" : String.valueOf(textBoxes.size())));
        SinglePageViewerFragment fragment = mPageReferenceMap.get(position);
        if(fragment!=null) {
            fragment.setTextBoxes(textBoxes);
        }
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }

}

