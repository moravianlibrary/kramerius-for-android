package cz.mzk.kramerius.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class MenuItemWidget extends LinearLayout {
	private TextView mTitleView;
	private ImageView mIconView;

	private int mIcon = 0;
	private int mIconSelected = 0;

	public MenuItemWidget(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.widget_menu_item, this);
	}

	public MenuItemWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context, attrs);
	}

	public MenuItemWidget(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs);
		initViews(context, attrs);
	}

	private void initViews(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MenuItemView, 0, 0);

		String title = "";
		try {
			title = a.getString(R.styleable.MenuItemView_menuTitle);
			mIcon = a.getResourceId(R.styleable.MenuItemView_menuIcon, 0);
			mIconSelected = a.getResourceId(R.styleable.MenuItemView_menuIconSelected, 0);
		} finally {
			a.recycle();
		}

		LayoutInflater.from(context).inflate(R.layout.widget_menu_item, this);

		mTitleView = (TextView) this.findViewById(R.id.menu_item_title);
		mTitleView.setText(title);
		mIconView = (ImageView) this.findViewById(R.id.menu_item_icon);
		if (mIcon != 0) {
			mIconView.setImageResource(mIcon);
		}
	}

	public void setSelected(boolean selected) {
		if (selected) {
			mTitleView.setTextColor(getResources().getColor(R.color.color_primary));
			if (mIconSelected != 0) {
				mIconView.setImageResource(mIconSelected);
			}
		} else {
			mTitleView.setTextColor(getResources().getColor(R.color.menu_item_noraml));
			if (mIcon != 0) {
				mIconView.setImageResource(mIcon);
			}
		}
	}

}