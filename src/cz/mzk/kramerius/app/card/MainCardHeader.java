package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.CardHeader;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class MainCardHeader extends CardHeader {

	private int mSize;
	private int mColorResource;
	private int mLines;

	public MainCardHeader(Context context, int size, int color, int lines) {
		super(context);
		mSize = size;
		mColorResource = color;
		mLines = lines;
	}

	public MainCardHeader(Context context, int lines) {
		super(context);
		mSize = 15;
		mColorResource = R.color.grey;
		mLines = lines;
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		super.setupInnerViewElements(parent, view);
		TextView txt = (TextView) view.findViewById(R.id.card_header_inner_simple_title);
		txt.setTextColor(getContext().getResources().getColor(mColorResource));
		txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mSize);
		txt.setLines(mLines);

	}
}