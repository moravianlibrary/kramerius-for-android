package cz.mzk.kramerius.app.util;

import android.content.Context;
import android.content.Intent;

import cz.mzk.kramerius.app.api.K5Api;

public class ShareUtils {

    public static void openShareIntent(Context context, String pid) {
        if (context == null || pid == null) {
            return;
        }
        String url = K5Api.getPersistentUrl(context, pid);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

}
