package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.search.SearchFilter;
import cz.mzk.kramerius.app.search.SearchQuery;

public class SearchDateCard extends Card implements SearchFilter {

	public static final String MIN_DATE = "-3000";
	public static final String MAX_DATE = "3000";
	
	
	private String mName;
	private String mKey;
	private EditText mInputDateFrom;
	private EditText mInputDateTo;
	private String mFromValue;
	private String mToValue;
	private int mType;


	public SearchDateCard(Context context, String key, String name, int type) {
		super(context, R.layout.view_card_search_date);
		mContext = context;
		mKey = key;
		mName = name;
		mType = type;
		init();
	}

	private void init() {
		SearchCardHeader header = new SearchCardHeader(getContext());
		header.setTitle(mName);
		addCardHeader(header);
		setSwipeable(true);
	}
	
	@Override
	public int getType() {
		return mType;
	}

	private void initInputs() {
		mInputDateFrom.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				mFromValue = mInputDateFrom.getText().toString();
			}
		});
		mInputDateTo.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				mToValue = mInputDateTo.getText().toString();
			}
		});
		if (mFromValue != null && !mFromValue.isEmpty()) {
			mInputDateFrom.setText(mFromValue);
		}
		if (mToValue != null && !mToValue.isEmpty()) {
			mInputDateTo.setText(mToValue);
		}
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		mInputDateFrom = (EditText) view.findViewById(R.id.search_date_from);
		mInputDateTo = (EditText) view.findViewById(R.id.search_date_to);
		initInputs();
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

	@Override
	public String getName() {
		return mName;
	}

}