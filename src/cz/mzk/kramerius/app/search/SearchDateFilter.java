package cz.mzk.kramerius.app.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class SearchDateFilter implements SearchFilter, OnClickListener {

	private String mName;
	private String mKey;
	private ViewGroup mParentView;
	private Context mContext;
	private OnFilterDeletedListener mOnFilterDeletedListener;
	private EditText mInputDateFrom;
	private EditText mInputDateTo;
	private View mDeleteButton;
	private View mView;

	public SearchDateFilter(Context context, ViewGroup parentView, OnFilterDeletedListener onFilterDeletedListener,
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
		mView = inflater.inflate(R.layout.view_search_date, mParentView, false);
		mInputDateFrom = (EditText) mView.findViewById(R.id.search_date_from);
		mInputDateFrom.requestFocus();
		mInputDateTo = (EditText) mView.findViewById(R.id.search_date_to);
		TextView name = (TextView) mView.findViewById(R.id.search_date_name);
		name.setText(mName);
		mDeleteButton = mView.findViewById(R.id.search_date_delete);
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
		String dateFrom = mInputDateFrom.getText().toString();
		String dateTo = mInputDateTo.getText().toString();
		try {
			int begin = Integer.valueOf(dateFrom);
			int end = Integer.valueOf(dateTo);
			query.date(begin, end);
		} catch (NumberFormatException ex) {

		}
	}

	@Override
	public String getName() {
		return mName;
	}

}
