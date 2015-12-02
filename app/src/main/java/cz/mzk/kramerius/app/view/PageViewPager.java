package cz.mzk.kramerius.app.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cz.mzk.kramerius.app.adapter.PageViewPagerAdapter;
import cz.mzk.kramerius.app.viewer.SinglePageViewerFragment;

public class PageViewPager extends ViewPager {

    private static final String LOG_TAG = PageViewPager.class.getSimpleName();

    public PageViewPager(Context context) {
        super(context);
    }

    public PageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSwipeEnabled()) {
            return super.onTouchEvent(event);
        }
        return false;
    }


    private boolean isSwipeEnabled() {
        int c = getCurrentItem();
        if (getAdapter() == null) {
            return false;
        }
        PageViewPagerAdapter adapter = (PageViewPagerAdapter) getAdapter();
        //SinglePageViewerFragment fragmanet = adapter.getFragment(c);
        //Log.e("test", "viewPager: getItem " + c);
        SinglePageViewerFragment fragmanet = (SinglePageViewerFragment) adapter.getItem(c);
        if (fragmanet == null) {
            return true;
        }
        return fragmanet.isSwipeEnabled();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isSwipeEnabled()) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }


}
