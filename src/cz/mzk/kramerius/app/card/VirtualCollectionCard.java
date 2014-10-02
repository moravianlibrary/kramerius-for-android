package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import android.content.Context;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ModelUtil;

public class VirtualCollectionCard extends Card {

	private Item mItem;

	public VirtualCollectionCard(Context context, Item item) {
		super(context, R.layout.view_card_vc_content);
		mContext = context;
		mItem = item;
		init();
	}

	public VirtualCollectionCard(Context context, int innerLayout, Item item, DisplayImageOptions options) {
		super(context, innerLayout);
		mContext = context;
		mItem = item;
		init();
	}

	public Item getItem() {
		return mItem;
	}

	private void init() {		
		MainCardHeader header = new MainCardHeader(getContext());
		header.setTitle(mItem.getTitle());
		addCardHeader(header);
	}
	

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {

	}

	class MainCardHeader extends CardHeader {

		public MainCardHeader(Context context) {
			super(context);
		}

		@Override
		public void setupInnerViewElements(ViewGroup parent, View view) {
			super.setupInnerViewElements(parent, view);
			TextView txt = (TextView) view.findViewById(R.id.card_header_inner_simple_title);
			txt.setTextColor(getContext().getResources().getColor(R.color.grey));
			txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			txt.setLines(1);

		}
	}

}