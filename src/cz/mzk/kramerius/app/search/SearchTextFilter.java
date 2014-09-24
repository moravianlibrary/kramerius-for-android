package cz.mzk.kramerius.app.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class SearchTextFilter implements SearchFilter, OnClickListener {

	private String mName;
	private String mKey;
	private ViewGroup mParentView;
	private Context mContext;
	private OnFilterDeletedListener mOnFilterDeletedListener;
	private TextView mInput;
	private View mDeleteButton;
	private View mView;
	
	private boolean mWithIdentifier;

	
	public SearchTextFilter(Context context, ViewGroup parentView, OnFilterDeletedListener onFilterDeletedListener, String key, String name, boolean withIdentifier) {
		mContext = context;
		mParentView = parentView;
		mKey = key;
		mName = name;
		mWithIdentifier = withIdentifier;
		mOnFilterDeletedListener = onFilterDeletedListener;
		populate();
	}

	
	public SearchTextFilter(Context context, ViewGroup parentView, OnFilterDeletedListener onFilterDeletedListener, String key, String name) {
		this(context, parentView, onFilterDeletedListener, key, name, false);
	}

	private void populate() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.view_search_text, mParentView, false);
		mInput = (EditText) mView.findViewById(R.id.search_text_input);
		TextView name = (TextView) mView.findViewById(R.id.search_text_name);
		name.setText(mName);
		mDeleteButton = mView.findViewById(R.id.search_text_delete);
		mDeleteButton.setOnClickListener(this);		
		mParentView.addView(mView);
		mInput.requestFocus();
	}
	
	@Override
	public View getView() {
		return mView;
	}
	
	@Override
	public String getKey() {
		return mKey;
	}

	public String getValue() {
		return mInput.getText().toString();
	}


	@Override
	public void onClick(View v) {
		if(v == mDeleteButton) {
			if(mOnFilterDeletedListener != null) {
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
		if(mWithIdentifier) {
			query.identifier(mKey, getValue());
		} else {
			query.add(mKey, getValue());
		}
		
	}

	@Override
	public String getName() {
		return mName;
	}


}
