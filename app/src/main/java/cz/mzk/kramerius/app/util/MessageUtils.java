package cz.mzk.kramerius.app.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cz.mzk.kramerius.app.BaseFragment.onWarningButtonClickedListener;
import cz.mzk.kramerius.app.R;

public class MessageUtils {

    public static void inflateMessage(Context context, final ViewGroup vg, String message, String buttonText,
                                      final onWarningButtonClickedListener callback, final boolean hideAfterClick) {
        if (vg == null || context == null) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.view_warning, vg, false);
        TextView text = (TextView) view.findViewById(R.id.warning_message);
        Button button = (Button) view.findViewById(R.id.warning_button);
        text.setText(message);
        if (buttonText == null || callback == null) {
            button.setVisibility(View.GONE);
        } else {
            button.setText(buttonText);
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (hideAfterClick) {
                        vg.removeView(view);
                    }
                    callback.onWarningButtonClicked();
                }
            });
        }
        vg.addView(view);
    }

}
