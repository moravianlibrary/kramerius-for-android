package cz.mzk.kramerius.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.PageSelectionAdapter;
import cz.mzk.kramerius.app.model.Item;

public class PageSelectionFragment extends BaseFragment {

    private static final String TAG = PageSelectionFragment.class.getName();

    private GridView mGridview;
    private PageSelectionAdapter mAdapter;
    private OnPageNumberSelected mOnPageNumberSelected;

//	@Override
//	public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
//
//		Display display = getActivity().getWindowManager().getDefaultDisplay();
//		Point size = new Point();
//		display.getSize(size);
//		float h = size.y;
//
//		Animator animator = null;
//		if (enter) {
//			animator = ObjectAnimator.ofFloat(this, "translationY", h, 0);
//		} else {
//			animator = ObjectAnimator.ofFloat(this, "translationY", 0, h);
//		}
//
//		animator.setDuration(500);
//		return animator;
//	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_selection, container, false);
        mGridview = (GridView) view.findViewById(R.id.gridview);

        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                onItemSelected(position);
            }
        });
        return view;
    }

    public interface OnPageNumberSelected {
        public void onPageNumberSelected(int index);
    }

    public void setOnPageNumberSelected(OnPageNumberSelected onPageNumberSelected) {
        mOnPageNumberSelected = onPageNumberSelected;
    }

    private void onItemSelected(int position) {
        if (mOnPageNumberSelected == null || mAdapter == null) {
            return;
        }
        if (position < 0 || position >= mAdapter.getCount()) {
            return;
        }
        mOnPageNumberSelected.onPageNumberSelected(position);
    }

    public void assignItems(List<Item> items) {
        Context c = getActivity();
        if (c == null) {
            return;
        }
        mAdapter = new PageSelectionAdapter(c, items);
        mGridview.setAdapter(mAdapter);
    }


}
