package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.search.SearchFilter;
import cz.mzk.kramerius.app.search.SearchQuery;

public class SearchDateCard extends Card implements SearchFilter {

	private String mName;
	private String mKey;
	private EditText mInputDateFrom;
	private EditText mInputDateTo;

	public SearchDateCard(Context context, String key, String name) {
		super(context, R.layout.view_card_search_date);
		mContext = context;
		mKey = key;
		mName = name;
		init();
	}

	private void init() {
		SearchCardHeader header = new SearchCardHeader(getContext());
		header.setTitle(mName);
		addCardHeader(header);
		setSwipeable(true);
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		mInputDateFrom = (EditText) view.findViewById(R.id.search_date_from);
		mInputDateTo = (EditText) view.findViewById(R.id.search_date_to);
	}


	@Override
	public String getKey() {
		return mKey;
	}

	@Override
	public int validate() {
		String dateFrom = mInputDateFrom.getText().toString();
		String dateTo = mInputDateTo.getText().toString();
		if (dateFrom.isEmpty()) {
			dateFrom = "0";
		}
		if (dateTo.isEmpty()) {
			dateTo = "2050";
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
			dateFrom = "0";
		}
		if (dateTo.isEmpty()) {
			dateTo = "2050";
		}
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