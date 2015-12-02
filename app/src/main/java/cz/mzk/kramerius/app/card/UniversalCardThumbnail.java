package cz.mzk.kramerius.app.card;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import it.gmariotti.cardslib.library.internal.CardThumbnail;

public class UniversalCardThumbnail extends CardThumbnail {

    private String mUrl;
    private DisplayImageOptions mImageOptions;

    public UniversalCardThumbnail(Context context, String url, DisplayImageOptions imageOptions) {
        super(context);
        mUrl = url;
        mImageOptions = imageOptions;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View viewImage) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
        imageLoader.displayImage(mUrl, (ImageView) viewImage, mImageOptions);

    }
}