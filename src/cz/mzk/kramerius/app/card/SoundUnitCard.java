package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Item;

public class SoundUnitCard extends Card {

	private Item mItem;
	private Context mContext;
	private OnPopupMenuSelectedListener mOnPopupMenuSelectedListener;

	public SoundUnitCard(Context context, Item item, DisplayImageOptions options) {
		super(context, R.layout.view_card_content);
		mContext = context;
		mItem = item;
		init(options);
	}

	public SoundUnitCard(Context context, int innerLayout, Item item, DisplayImageOptions options) {
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
		header.setTitle(mItem.getTitle());
		header.setPopupMenu(R.menu.card_popup, new CardHeader.OnClickCardHeaderPopupMenuListener() {
			@Override
			public void onMenuItemClick(BaseCard card, MenuItem item) {
				if (mOnPopupMenuSelectedListener != null) {
					if (item.getItemId() == R.id.menu_card_detail) {
						mOnPopupMenuSelectedListener.onPopupDetailsSelected(mItem);
					} else if (item.getItemId() == R.id.menu_card_share) {
						mOnPopupMenuSelectedListener.onPopupShareSelected(mItem);
					} else if (item.getItemId() == R.id.menu_card_open) {
						mOnPopupMenuSelectedListener.onPopupOpenSelected(mItem);
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
		TextView subtitle = (TextView) view.findViewById(R.id.grid_item_author);
		subtitle.setText(mContext.getString(R.string.sound_unit_tracks).toUpperCase() + " 1");
		View lock = view.findViewById(R.id.grid_item_lockIcon);
		if (mItem.isPrivate()) {
			lock.setVisibility(View.VISIBLE);
		} else {
			lock.setVisibility(View.GONE);
		}
		TextView type = (TextView) view.findViewById(R.id.grid_item_type);
		type.setText(mContext.getString(R.string.sound_unit_play));
		ImageView modelIcon = (ImageView) view.findViewById(R.id.grid_item_modeIcon);
		modelIcon.setImageResource(R.drawable.ic_play_green);

	}

}