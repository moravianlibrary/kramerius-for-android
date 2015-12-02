package cz.mzk.kramerius.app.search;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import java.util.List;

import cz.mzk.kramerius.app.R;

public abstract class SearchFilter {

    private static final int ANIM_DELAY = 300;

    private String mName;
    private String mKey;
    private ViewGroup mParentView;
    private Context mContext;
    private boolean mRemovable;
    private View mView;
    private List<SearchFilter> mFilters;

    public SearchFilter(Context context, ViewGroup parentView, List<SearchFilter> filters, boolean removable,
                        String key, String name) {
        mContext = context;
        mParentView = parentView;
        mKey = key;
        mName = name;
        mRemovable = removable;
        mFilters = filters;
        mFilters.add(this);
    }

    public String getName() {
        return mName;
    }

    public String getKey() {
        return mKey;
    }

    public boolean isRemovable() {
        return mRemovable;
    }

    public View getView() {
        return mView;
    }

    public Context getContext() {
        return mContext;
    }

    public void initView(View view) {
        mView = view;
        TextView title = (TextView) view.findViewById(R.id.search_filter_title);
        title.setText(getName());
        View deleteButton = view.findViewById(R.id.search_filter_delete);
        if (isRemovable()) {
            deleteButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    removeFilter();
                }
            });
        } else {
            deleteButton.setVisibility(View.GONE);
        }
        mParentView.addView(mView, 0);
        TranslateAnimation anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, -1.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f);

        anim.setDuration(ANIM_DELAY);
        anim.setFillAfter(true);
        mView.startAnimation(anim);
    }

    protected void removeFilter() {
        if (mRemovable && mFilters != null && mParentView != null) {
            TranslateAnimation anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 1.0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0.0f);
            anim.setDuration(ANIM_DELAY);
            anim.setFillBefore(true);
            mView.startAnimation(anim);
            anim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mParentView.removeView(mView);
                    mFilters.remove(SearchFilter.this);
                }
            });

        }
    }

    public abstract int validate();

    public abstract void addToQuery(SearchQuery query);
}
