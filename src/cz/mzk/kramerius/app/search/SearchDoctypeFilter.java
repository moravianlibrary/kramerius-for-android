package cz.mzk.kramerius.app.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.util.ModelUtil;

public class SearchDoctypeFilter implements SearchFilter, OnClickListener {

	private String mName;
	private String mKey;
	private ViewGroup mParentView;
	private Context mContext;
	private OnFilterDeletedListener mOnFilterDeletedListener;
	private Spinner mSpinner;
	private View mDeleteButton;
	private View mView;

	public SearchDoctypeFilter(Context context, ViewGroup parentView, OnFilterDeletedListener onFilterDeletedListener,
			String key, String name) {
		mContext = context;
		mParentView = parentView;
		mKey = key;
		mName = name;
		mOnFilterDeletedListener = onFilterDeletedListener;
		populate();
	}

	private void populate() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.view_search_doctype, mParentView, false);
		mSpinner = (Spinner) mView.findViewById(R.id.search_doctype_spinner);
		TextView name = (TextView) mView.findViewById(R.id.search_doctype_name);
		name.setText(mName);
		mDeleteButton = mView.findViewById(R.id.search_doctype_delete);
		mDeleteButton.setOnClickListener(this);
		mParentView.addView(mView);
	}

	@Override
	public View getView() {
		return mView;
	}

	@Override
	public String getKey() {
		return mKey;
	}

	public String getModel() {
		int position = mSpinner.getSelectedItemPosition();
		String type = null;
		switch (position) {
		case 0:
			type = ModelUtil.MONOGRAPH;
			break;
		case 1:
			type = ModelUtil.PERIODICAL;
			break;
		case 2:
			type = ModelUtil.MANUSCRIPT;
			break;
		case 3:
			type = ModelUtil.MAP;
			break;
		case 4:
			type = ModelUtil.GRAPHIC;
			break;
		case 5:
			type = ModelUtil.SHEET_MUSIC;
			break;
		case 6:
			type = ModelUtil.ARCHIVE;
			break;
		case 7:
			type = ModelUtil.SOUND_RECORDING;
			break;
		}
		return type;
	}

	@Override
	public void onClick(View v) {
		if (v == mDeleteButton) {
			if (mOnFilterDeletedListener != null) {
				mOnFilterDeletedListener.onFilterDeleted(this);
			}
		}
	}

	@Override
	public int validate() {
		return 0;
	}

	@Override
	public void addToQuery(SearchQuery query) {
		query.add(mKey, getModel(), false);
	}

	@Override
	public String getName() {
		return mName;
	}

}
