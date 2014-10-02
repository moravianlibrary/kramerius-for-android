package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.CardHeader;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class SearchCardHeader extends CardHeader {

	public SearchCardHeader(Context context) {
		super(context);
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		super.setupInnerViewElements(parent, view);
		TextView txt = (TextView) view.findViewById(R.id.card_header_inner_simple_title);
		txt.setTextColor(getContext().getResources().getColor(R.color.green));
		txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		txt.setLines(1);

	}
}