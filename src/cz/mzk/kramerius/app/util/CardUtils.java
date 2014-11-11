package cz.mzk.kramerius.app.util;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingRightInAnimationAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.card.GridCard;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.model.Item;

public class CardUtils {

	public static final int ANIM_SCALE_IN = 0;
	public static final int ANIM_ALPHA = 1;
	public static final int ANIM_SWING_LEFT = 2;
	public static final int ANIM_SWING_RIGHT = 3;

	private static AnimationAdapter getAnimationAdapter(BaseAdapter adapter, int animType) {
		switch (animType) {
		case ANIM_SCALE_IN:
			return new ScaleInAnimationAdapter(adapter);
		case ANIM_ALPHA:
			return new AlphaInAnimationAdapter(adapter);
		case ANIM_SWING_LEFT:
			return new SwingLeftInAnimationAdapter(adapter);
		case ANIM_SWING_RIGHT:
			return new SwingRightInAnimationAdapter(adapter);
		default:
			return null;
		}
	}

	public static void setAnimationAdapter(CardGridArrayAdapter adapter, CardGridView view, int animType) {		
		AnimationAdapter animationAdapter = getAnimationAdapter(adapter, animType);
		if (animationAdapter == null) {
			return;
		}
		animationAdapter.setAbsListView(view);
		view.setExternalAdapter(animationAdapter, adapter);
	}

	public static void setAnimationAdapter(CardGridArrayAdapter adapter, CardGridView view) {
		setAnimationAdapter(adapter, view, ANIM_SCALE_IN);
	}

	public static void setAnimationAdapter(CardArrayAdapter adapter, CardListView view, int animType) {
		AnimationAdapter animationAdapter = getAnimationAdapter(adapter, animType);
		if (animationAdapter == null) {
			return;
		}
		animationAdapter.setAbsListView(view);
		view.setExternalAdapter(animationAdapter, adapter);
	}

	public static void setAnimationAdapter(CardArrayAdapter adapter, CardListView view) {
		setAnimationAdapter(adapter, view, ANIM_SCALE_IN);
	}

	public static DisplayImageOptions initUniversalImageLoaderLibrary(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context.getApplicationContext()).build();
		DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.img_empty_loader)
				// .displayer(new FadeInBitmapDisplayer(500, true, false,
				// false))
				.cacheInMemory(true).cacheOnDisk(true).showImageForEmptyUri(R.drawable.img_empty)
				.showImageOnFail(R.drawable.img_empty).build();
		ImageLoader.getInstance().init(config);
		return options;
	}

	public static CardGridArrayAdapter createAdapter(Context context, List<Item> items,
			final OnItemSelectedListener listener, OnPopupMenuSelectedListener popupListener,
			DisplayImageOptions options) {
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Item item : items) {

			GridCard card = new GridCard(context, item, options);
			card.setOnPopupMenuSelectedListener(popupListener);
			card.setOnClickListener(new OnCardClickListener() {
				@Override
				public void onClick(Card card, View view) {
					if (listener != null) {
						listener.onItemSelected(((GridCard) card).getItem());
					}
				}
			});
			cards.add(card);
		}
		return new CardGridArrayAdapter(context, cards);
	}
}