package cz.mzk.kramerius.app.card;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Domain;

public class DomainCard extends Card {

	private Domain mDomain;
	private Context mContext;

	public DomainCard(Context context, Domain domain) {
		super(context, R.layout.view_card_domain_content);
		mContext = context;
		mDomain = domain;
		init();
	}

	public Domain getDomain() {
		return mDomain;
	}

	private void init() {
		MainCardHeader header = new MainCardHeader(getContext(), 17, R.color.grey, 1);
		header.setTitle(mDomain.getTitle());
		addCardHeader(header);
		CardThumbnail thumbnail = new CardThumbnail(mContext);
		thumbnail.setDrawableResource(mDomain.getLogo());
		addCardThumbnail(thumbnail);
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		TextView description = (TextView) view.findViewById(R.id.domain_description);
		description.setText(mDomain.getSubtitle());
		TextView domain = (TextView) view.findViewById(R.id.domain_url);
		domain.setText(mDomain.getDomain());
	}

}