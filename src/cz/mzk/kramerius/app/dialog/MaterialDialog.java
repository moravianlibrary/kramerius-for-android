package cz.mzk.kramerius.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class MaterialDialog extends AlertDialog implements View.OnClickListener {

	private Context mContext;
	private View mView;
	private TextView mPositiveButton;
	private TextView mNegativeButton;
	private onActionButtonClickedListener mCallback;

	MaterialDialog(Builder builder) {
		super(builder.context);

		Typeface typefaceMedium = Typeface
				.createFromAsset(getContext().getResources().getAssets(), "Roboto-Medium.ttf");
		Typeface typefaceRegular = Typeface.createFromAsset(getContext().getResources().getAssets(),
				"Roboto-Regular.ttf");
		mContext = builder.context;
		mCallback = builder.callback;
		mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_basic, null);

		TextView title = (TextView) mView.findViewById(R.id.title);
		TextView content = (TextView) mView.findViewById(R.id.content);

		content.setText(builder.content);
		content.setVisibility(View.VISIBLE);
		content.setTypeface(typefaceRegular);
		if (builder.title == null || builder.title.isEmpty()) {
			title.setVisibility(View.GONE);
		} else {
			title.setText(builder.title);
			title.setTypeface(typefaceMedium);
		}

		mPositiveButton = (TextView) mView.findViewById(R.id.buttonPositive);
		if (builder.positiveText == null || builder.positiveText.isEmpty()) {
			mPositiveButton.setVisibility(View.GONE);
		} else {
			mPositiveButton.setTypeface(typefaceMedium);
			mPositiveButton.setText(builder.positiveText);
			mPositiveButton.setOnClickListener(this);
		}
		mNegativeButton = (TextView) mView.findViewById(R.id.buttonNegative);
		if (builder.negativeText == null || builder.negativeText.isEmpty()) {
			mNegativeButton.setVisibility(View.GONE);
		} else {			
			mNegativeButton.setTypeface(typefaceMedium);
			mNegativeButton.setText(builder.negativeText);
			mNegativeButton.setOnClickListener(this);
		}
		setView(mView);
	}

	@Override
	public final void onClick(View v) {
		if (v == mPositiveButton) {
			if (mCallback != null) {
				mCallback.onPositiveButtonClicked();
			}
		} else if (v == mNegativeButton) {
			if (mCallback != null) {
				mCallback.onNegativeButtonClicked();
			}
		}
		dismiss();
	}

	public interface onActionButtonClickedListener {
		public void onPositiveButtonClicked();

		public void onNegativeButtonClicked();
	}

	public static class Builder {

		protected Context context;
		protected String title;
		protected String content;
		protected String positiveText;
		protected String negativeText;
		protected View customView;
		protected onActionButtonClickedListener callback;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder title(int titleRes) {
			title(context.getString(titleRes));
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder content(int contentRes) {
			content(context.getString(contentRes));
			return this;
		}

		public Builder content(String content) {
			this.content = content;
			return this;
		}

		public Builder positiveText(int postiveRes) {
			positiveText(context.getString(postiveRes));
			return this;
		}

		public Builder positiveText(String message) {
			this.positiveText = message;
			return this;
		}

		public Builder negativeText(int negativeRes) {
			negativeText(context.getString(negativeRes));
			return this;
		}

		public Builder negativeText(String message) {
			this.negativeText = message;
			return this;
		}

		public Builder callback(onActionButtonClickedListener callback) {
			this.callback = callback;
			return this;
		}

		public MaterialDialog build() {
			return new MaterialDialog(this);
		}

	}

}
