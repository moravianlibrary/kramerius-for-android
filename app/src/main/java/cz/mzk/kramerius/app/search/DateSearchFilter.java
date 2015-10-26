package cz.mzk.kramerius.app.search;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class DateSearchFilter extends SearchFilter {

	public static final String MIN_DATE = "-3000";
	public static final String MAX_DATE = "3000";

	private EditText mInputDateFrom;
	private EditText mInputDateTo;

	public DateSearchFilter(Context context, ViewGroup parentView, List<SearchFilter> filters, boolean removable,
			String key, String name) {
		super(context, parentView, filters, removable, key, name);
		init(context, parentView);
	}

	private void init(Context context, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.view_search_date, parent, false);
		mInputDateFrom = (EditText) view.findViewById(R.id.search_filter_date_from);
		mInputDateTo = (EditText) view.findViewById(R.id.search_filter_date_to);
		initView(view);
	}

	@Override
	public int validate() {
		String dateFrom = mInputDateFrom.getText().toString();
		String dateTo = mInputDateTo.getText().toString();
		if (dateFrom.isEmpty()) {
			dateFrom = MIN_DATE;
		}
		if (dateTo.isEmpty()) {
			dateTo = MAX_DATE;
		}
		try {
			int begin = Integer.valueOf(dateFrom);
			int end = Integer.valueOf(dateTo);
			if (begin > end) {
				return R.string.search_filter_date_invalid_tltf;
			}
		} catch (NumberFormatException ex) {
			return R.string.search_filter_date_invalid_nan;
		}
		return 0;
	}

	@Override
	public void addToQuery(SearchQuery query) {
		String dateFrom = mInputDateFrom.getText().toString();
		String dateTo = mInputDateTo.getText().toString();
		if (dateFrom.isEmpty()) {
			dateFrom = MIN_DATE;
		}
		if (dateTo.isEmpty()) {
			dateTo = MAX_DATE;
		}
		try {
			int begin = Integer.valueOf(dateFrom);
			int end = Integer.valueOf(dateTo);
			query.date(begin, end);
		} catch (NumberFormatException ex) {

		}
	}

}