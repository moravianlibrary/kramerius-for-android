package cz.mzk.kramerius.app.card;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Item;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

public class RecentCard extends Card {

    private Context mContext;
    private OnPopupMenuSelectedListener mOnPopupMenuSelectedListener;

    private String title;
    private String subtitle;
    private String pid;
    private String parentPid;
    private String model;
    private long timestamp;

    public RecentCard(Context context) {
        super(context, R.layout.view_card_content);
        mContext = context;
    }

    public RecentCard(Context context, int innerLayout) {
        super(context, innerLayout);
        mContext = context;
    }

    public RecentCard popupListener(OnPopupMenuSelectedListener listener) {
        mOnPopupMenuSelectedListener = listener;
        return this;
    }

    public RecentCard model(String model) {
        this.model = model;
        return this;
    }

    public RecentCard title(String title) {
        this.title = title;
        return this;
    }

    public RecentCard subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public RecentCard pid(String pid) {
        this.pid = pid;
        return this;
    }

    public RecentCard parentPid(String parentPid) {
        this.parentPid = parentPid;
        return this;
    }

    public RecentCard timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Item getItem() {
        Item item = new Item();
        item.setPid(pid);
        item.setTitle(title);
        item.setModel(model);
        item.setRootPid(parentPid);
        return item;
    }

    public RecentCard build(DisplayImageOptions options) {
        MainCardHeader header = new MainCardHeader(getContext(), 2);
        header.setButtonOverflowVisible(true);
        header.setTitle(title);
        header.setPopupMenu(R.menu.card_popup, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard card, MenuItem item) {
                if (mOnPopupMenuSelectedListener != null) {
                    if (item.getItemId() == R.id.menu_card_detail) {
                        mOnPopupMenuSelectedListener.onPopupDetailsSelected(getItem());
                    } else if (item.getItemId() == R.id.menu_card_share) {
                        mOnPopupMenuSelectedListener.onPopupShareSelected(getItem());
                    } else if (item.getItemId() == R.id.menu_card_open) {
                        mOnPopupMenuSelectedListener.onPopupOpenSelected(getItem());
                    }
                }
            }
        });
        addCardHeader(header);
        String url = K5Api.getThumbnailPath(mContext, parentPid);
        UniversalCardThumbnail cardThumbnail = new UniversalCardThumbnail(mContext, url, options);
        cardThumbnail.setExternalUsage(true);
        addCardThumbnail(cardThumbnail);
        return this;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView subtitleView = (TextView) view.findViewById(R.id.grid_item_author);
        subtitleView.setText(subtitle);
        View lock = view.findViewById(R.id.grid_item_lockIcon);
        // if (mItem.isPrivate()) {
        // lock.setVisibility(View.VISIBLE);
        // } else {
        // lock.setVisibility(View.GONE);
        // }
        TextView TimestampView = (TextView) view.findViewById(R.id.grid_item_type);

        Date date = new Date(timestamp);
        SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
        TimestampView.setText(dt.format(date));

        ImageView icon = (ImageView) view.findViewById(R.id.grid_item_modeIcon);
        icon.setImageResource(R.drawable.ic_recent2_green);
    }

}