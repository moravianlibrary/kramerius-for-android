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
import cz.mzk.kramerius.app.util.ModelUtil;

public class PeriodicalCard extends Card {

	private Item mItem;
	private Context mContext;
	private OnPopupMenuSelectedListener mOnPopupMenuSelectedListener;

	public PeriodicalCard(Context context, Item item, DisplayImageOptions options) {
		super(context, R.layout.view_card_content);
		mContext = context;
		mItem = item;
		init(options);
	}

	public PeriodicalCard(Context context, int innerLayout, Item item, DisplayImageOptions options) {
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
		MainCardHeader header = new MainCardHeader(getContext(), 1);
		header.setButtonOverflowVisible(true);

		if (ModelUtil.PERIODICAL_VOLUME.equals(mItem.getModel())) {
			String volumeNumber = mItem.getVolumeNumber();
			if (volumeNumber == null || volumeNumber.isEmpty()) {
				volumeNumber = "neuveden";
			}
			header.setTitle("Ročník " + volumeNumber);
		} else if (ModelUtil.PERIODICAL_ITEM.equals(mItem.getModel())) {
			String number = mItem.getIssueNumber();
			if (number == null || number.isEmpty()) {
				number = mItem.getPartNumber();
			}
			if (number == null || number.isEmpty()) {
				number = "neuvedeno";
			}
			header.setTitle("Číslo " + number);
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
		TextView mainView = (TextView) view.findViewById(R.id.grid_item_author);
		TextView labelView = (TextView) view.findViewById(R.id.grid_item_type);
		ImageView iconView = (ImageView) view.findViewById(R.id.grid_item_modeIcon);
		if (ModelUtil.PERIODICAL_VOLUME.equals(mItem.getModel())) {
			String year = mItem.getYear();
			if (year == null || year.isEmpty()) {
				year = "neuveden";
			}
			mainView.setText("ROK VYDÁNÍ: " + year);
			labelView.setText("Zobrazit čísla");
			iconView.setImageResource(R.drawable.ic_attach_green);
		} else if (ModelUtil.PERIODICAL_ITEM.equals(mItem.getModel())) {
			String date = mItem.getPeriodicalItemDate();
			if (date == null || date.isEmpty()) {
				date = "neuvedeno";
			}
			mainView.setText("DATUM VYDÁNÍ: " + date);
			labelView.setText("Otevřít číslo");
			iconView.setImageResource(R.drawable.ic_book_green);
		}

		View lock = view.findViewById(R.id.grid_item_lockIcon);
		if (mItem.isPrivate()) {
			lock.setVisibility(View.VISIBLE);
		} else {
			lock.setVisibility(View.GONE);
		}
	}

}