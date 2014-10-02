package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.search.SearchFilter;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.ModelUtil;

public class SearchDoctypeCard extends Card implements SearchFilter {

	private String mName;
	private String mKey;
	private Spinner mSpinner;

	public SearchDoctypeCard(Context context, String key, String name) {
		super(context, R.layout.view_card_search_doctype);
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
		mSpinner = (Spinner) view.findViewById(R.id.search_doctype_spinner);
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