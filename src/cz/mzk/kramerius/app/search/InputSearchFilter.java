package cz.mzk.kramerius.app.search;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class InputSearchFilter extends SearchFilter {

	private TextView mInput;

	private boolean mWithIdentifier;

	public InputSearchFilter(Context context, ViewGroup parentView, List<SearchFilter> filters, boolean removable,
			String key, String name, boolean withIdentifier) {
		super(context, parentView, filters, removable, key, name);
		mWithIdentifier = withIdentifier;
		init(context, parentView);
	}

	private void init(Context context, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.view_search_input, parent, false);
		mInput = (EditText) view.findViewById(R.id.search_filter_input);
		mInput.setLongClickable(false);

//		final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
//			public boolean onDoubleTap(MotionEvent e) {
//				return true;
//			}
//		});
//
//		mInput.setOnTouchListener(new View.OnTouchListener() {
//			public boolean onTouch(View v, MotionEvent event) {
//				return gestureDetector.onTouchEvent(event);
//			}
//		});
		// mInput.setCustomSelectionActionModeCallback(new Callback() {
		//
		// @Override
		// public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// // TODO Auto-generated method stub
		// return false;
		// }
		//
		// @Override
		// public void onDestroyActionMode(ActionMode mode) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// // TODO Auto-generated method stub
		// return false;
		// }
		//
		// @Override
		// public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// // TODO Auto-generated method stub
		// return false;
		// }
		// });
		initView(view);
	}

	public String getValue() {
		return mInput.getText().toString();
	}

	@Override
	public int validate() {
		return 0;
	}

	@Override
	public void addToQuery(SearchQuery query) {
		if (mWithIdentifier) {
			query.identifier(getKey(), getValue());
		} else {
			query.add(getKey(), getValue());
		}

	}

}