package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.VersionUtils;
import cz.mzk.kramerius.app.view.MenuItemWidget;

public class HelpMenuFragment extends BaseFragment implements OnClickListener {

	private static final String LOG_TAG = HelpMenuFragment.class.getSimpleName();

	private MenuItemWidget mItemAbout;
	private MenuItemWidget mItemContent;
	private MenuItemWidget mItemFeedback;

	private HelpMenuListener mCallback;
	private List<MenuItemWidget> mMenuItems;

	public interface HelpMenuListener {
		public void onFragmentSelected(Fragment fragment, int titleResource);

		public void onFeedback();
	}

	public void setCallback(HelpMenuListener callback) {
		mCallback = callback;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_help_menu, container, false);
		mMenuItems = new ArrayList<MenuItemWidget>();
		mItemAbout = (MenuItemWidget) view.findViewById(R.id.menu_help_about);
		mItemContent = (MenuItemWidget) view.findViewById(R.id.menu_help_content);
		mItemFeedback = (MenuItemWidget) view.findViewById(R.id.menu_help_feedback);
		mMenuItems.add(mItemAbout);
		mMenuItems.add(mItemContent);
		mMenuItems.add(mItemFeedback);
		for (View v : mMenuItems) {
			v.setOnClickListener(this);
		}
		return view;
	}

	private void selectMenuItem(MenuItemWidget selectedItem) {
		for (MenuItemWidget item : mMenuItems) {
			item.setSelected(selectedItem == item);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		if(isTablet() && isLandscape()) {
			selectMenuItem(mItemAbout);
			if(mCallback != null) {
				mCallback.onFragmentSelected(new HelpAboutFragment(), R.string.help_menu_about);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (mCallback == null) {
			return;
		}
		if (v == mItemFeedback) {
			mCallback.onFeedback();
		} else if (v == mItemAbout) {
			selectMenuItem(mItemAbout);
			mCallback.onFragmentSelected(new HelpAboutFragment(), R.string.help_menu_about);
		} else if (v == mItemContent) {
			selectMenuItem(mItemContent);
			mCallback.onFragmentSelected(DomainDetailFragment.newInstance(null), R.string.help_menu_content);
		} 
	}
}
