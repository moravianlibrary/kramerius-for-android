package cz.mzk.kramerius.app.card;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.util.CardUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

public class DomainCard extends Card {

    private Domain mDomain;
    private Context mContext;
    private OnDomainPopupListener mOnDomainPopupListener;

    public DomainCard(Context context, Domain domain) {
        super(context, R.layout.view_card_domain_content);
        DisplayImageOptions options = CardUtils.initUniversalImageLoaderLibrary(context);
        mContext = context;
        mDomain = domain;
        init(options);
    }

    public void setOnDomainPopupListener(OnDomainPopupListener onDomainPopupListener) {
        mOnDomainPopupListener = onDomainPopupListener;
    }

    public Domain getDomain() {
        return mDomain;
    }

    public interface OnDomainPopupListener {
        public void onDomainPopupOpen(Domain domain);

        public void onDomainPopupContent(Domain domain);
    }

    private void init(DisplayImageOptions options) {
        //MainCardHeader header = new MainCardHeader(getContext(), 17, R.color.grey, 1);
        MainCardHeader header = new MainCardHeader(getContext(), 2);
        header.setTitle(mDomain.getTitle());
        header.setPopupMenu(R.menu.domain_card_popup, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard card, MenuItem item) {
                if (mOnDomainPopupListener != null) {
                    if (item.getItemId() == R.id.menu_domain_card_open) {
                        mOnDomainPopupListener.onDomainPopupOpen(mDomain);
                    } else if (item.getItemId() == R.id.menu_domain_card_content) {
                        mOnDomainPopupListener.onDomainPopupContent(mDomain);
                    }
                }
            }
        });

        addCardHeader(header);
//        CardThumbnail thumbnail = new CardThumbnail(mContext);
//        thumbnail.setDrawableResource(mDomain.getLogo());
//        addCardThumbnail(thumbnail);
        UniversalCardThumbnail cardThumbnail = new UniversalCardThumbnail(mContext, mDomain.getLogo(), options);
        cardThumbnail.setExternalUsage(true);
        addCardThumbnail(cardThumbnail);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //TextView description = (TextView) view.findViewById(R.id.domain_description);
        //description.setText(mDomain.getSubtitle());
        TextView domain = (TextView) view.findViewById(R.id.domain_url);
        domain.setText(mDomain.getDomain());
    }

}