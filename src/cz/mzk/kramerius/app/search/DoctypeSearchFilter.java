package cz.mzk.kramerius.app.search;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.util.ModelUtil;

public class DoctypeSearchFilter extends SearchFilter {

	private Spinner mSpinner;


	public DoctypeSearchFilter(Context context, ViewGroup parentView, List<SearchFilter> filters, boolean removable,
			String key, String name) {
		super(context, parentView, filters, removable, key, name);		
		init(context, parentView);
	}

	private void init(Context context, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.view_search_doctype, parent, false);
		mSpinner = (Spinner) view.findViewById(R.id.search_filter_spinner);
		initView(view);
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
		query.add(getKey(), getModel(), false, false);
	}

}