package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.search.SearchFilter;
import cz.mzk.kramerius.app.search.SearchQuery;

public class SearchTextCard extends Card implements SearchFilter {

	private String mName;
	private String mKey;
	private TextView mInput;
	private String mValue;
	
	private int mType;

	private boolean mWithIdentifier;

	public SearchTextCard(Context context, String key, String name, boolean withIdentifier, int type) {
		super(context, R.layout.view_card_search_text);
		Log.d("aaaa", "search card create: " + name);
		mContext = context;
		mType = type;
		mKey = key;
		mName = name;
		mWithIdentifier = withIdentifier;
		init();
	}
	
	@Override
	public int getType() {
		return mType;
	}

	private void init() {
		SearchCardHeader header = new SearchCardHeader(getContext());
		// header.setButtonOverflowVisible(true);
		header.setTitle(mName);
		addCardHeader(header);
		setSwipeable(true);
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		mInput = (EditText) view.findViewById(R.id.search_text_input);
		Log.d("aaaa", "search card setup: " + mName);

		initInputs();
	}

	private void initInputs() {
		mInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				mValue = mInput.getText().toString();
			}
		});
		if (mValue != null && !mValue.isEmpty()) {
			mInput.setText(mValue);
		}
	}

	@Override
	public String getKey() {
		return mKey;
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