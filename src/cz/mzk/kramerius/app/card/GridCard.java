package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ModelUtil;

public class GridCard extends Card {

	private Item mItem;
	private Context mContext;
	private OnPopupMenuSelectedListener mOnPopupMenuSelectedListener;

	public GridCard(Context context, Item item, DisplayImageOptions options) {
		super(context, R.layout.view_card_content);
		mContext = context;
		mItem = item;
		init(options);
	}

	public GridCard(Context context, int innerLayout, Item item, DisplayImageOptions options) {
		super(context, innerLayout);
		mContext = context;
		mItem = item;
		init(options);
	}

	public void setOnPopupMenuSelectedListener(OnPopupMenuSelectedListener listener) {
		mOnPopupMenuSelectedListener = listener;
	}

	public Item getItem() {
		return mItem;
	}

	private void init(DisplayImageOptions options) {
		MainCardHeader header = new MainCardHeader(getContext(), 2);
		header.setButtonOverflowVisible(true);
		if(ModelUtil.PAGE.equals(mItem.getModel())) {
			header.setTitle(mItem.getRootTitle());
		} else {
			header.setTitle(mItem.getTitle());
		}
		
		header.setPopupMenu(R.menu.card_popup, new CardHeader.OnClickCardHeaderPopupMenuListener() {
			@Override
			public void onMenuItemClick(BaseCard card, MenuItem item) {
				if (mOnPopupMenuSelectedListener != null) {
					if (item.getItemId() == R.id.menu_card_detail) {
						mOnPopupMenuSelectedListener.onPopupDetailsSelectd(mItem);
					} else if (item.getItemId() == R.id.menu_card_share) {
						mOnPopupMenuSelectedListener.onPopupShareSelectd(mItem);
					} else if (item.getItemId() == R.id.menu_card_open) {
						mOnPopupMenuSelectedListener.onPopupOpenSelectd(mItem);
					}
				}
			}
		});
		addCardHeader(header);
		String url = K5Api.getThumbnailPath(mContext, mItem.getPid());
		UniversalCardThumbnail cardThumbnail = new UniversalCardThumbnail(mContext, url, options);
		cardThumbnail.setExternalUsage(true);
		addCardThumbnail(cardThumbnail);
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		TextView author = (TextView) view.findViewById(R.id.grid_item_author);
		if(ModelUtil.PAGE.equals(mItem.getModel())) {
			author.setText(mItem.getTitle());
		} else {
			author.setText(mItem.getAuthor());
		}				
		View lock = view.findViewById(R.id.grid_item_lockIcon);
		if (mItem.isPrivate()) {
			lock.setVisibility(View.VISIBLE);
		} else {
			lock.setVisibility(View.GONE);
		}
		TextView type = (TextView) view.findViewById(R.id.grid_item_type);
		type.setText(mContext.getString(ModelUtil.getLabel(mItem.getModel())));
		ImageView modelIcon = (ImageView) view.findViewById(R.id.grid_item_modeIcon);
		int modelIconRes = ModelUtil.getIcon(mItem.getModel());
		modelIcon.setImageResource(modelIconRes);

	}

}