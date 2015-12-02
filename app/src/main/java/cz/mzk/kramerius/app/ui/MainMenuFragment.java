package cz.mzk.kramerius.app.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.DomainUtil;
import cz.mzk.kramerius.app.view.MenuItemWidget;

public class MainMenuFragment extends Fragment implements OnClickListener {


    public static final String CURRENT_MENU_ITEM_KEY = "key_current_menu_item";

    public static final int MENU_NONE = -1;
    public static final int MENU_HOME = 0;
    public static final int MENU_VIRTUAL_COLLECTION = 1;
    public static final int MENU_SEARCH = 2;
    public static final int MENU_RECENT = 3;
    public static final int MENU_ABOUT = 4;
    public static final int MENU_HELP = 5;
    public static final int MENU_SETTINGS = 6;

    private MainMenuListener mCallback;

    private MenuItemWidget mMenuHome;
    private MenuItemWidget mMenuVirtual;
    private MenuItemWidget mMenuRecent;
    private MenuItemWidget mMenuSearch;
    private MenuItemWidget mMenuHelp;
    private MenuItemWidget mMenuSettings;
    private List<MenuItemWidget> mMenuItems;

    private TextView mDomainTitle;
    private TextView mDomainUrl;
    private ImageView mDomainLogo;
    private View mDomainContainer;

    private int mCurrentMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentMenuItem = savedInstanceState.getInt(CURRENT_MENU_ITEM_KEY, MENU_HOME);
        } else {
            mCurrentMenuItem = MENU_HOME;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        mMenuItems = new ArrayList<MenuItemWidget>();
        mMenuHome = (MenuItemWidget) view.findViewById(R.id.menu_home);
        mMenuHome.setOnClickListener(this);
        //mMenuHome.setSelected(true);
        mMenuVirtual = (MenuItemWidget) view.findViewById(R.id.menu_virtual);
        mMenuVirtual.setOnClickListener(this);
        mMenuSearch = (MenuItemWidget) view.findViewById(R.id.menu_search);
        mMenuSearch.setOnClickListener(this);
        mMenuRecent = (MenuItemWidget) view.findViewById(R.id.menu_recent);
        mMenuRecent.setOnClickListener(this);
        mMenuHelp = (MenuItemWidget) view.findViewById(R.id.menu_help);
        mMenuHelp.setOnClickListener(this);
        mMenuSettings = (MenuItemWidget) view.findViewById(R.id.menu_settings);
        mMenuSettings.setOnClickListener(this);

        mMenuItems.add(mMenuHome);
        mMenuItems.add(mMenuVirtual);
        mMenuItems.add(mMenuSearch);
        mMenuItems.add(mMenuRecent);
        //mMenuItems.add(mMenuSettings);
        //mMenuItems.add(mMenuHelp);

        mDomainTitle = (TextView) view.findViewById(R.id.menu_domain_title);
        mDomainUrl = (TextView) view.findViewById(R.id.menu_domain_url);
        mDomainLogo = (ImageView) view.findViewById(R.id.menu_domain_logo);
        mDomainContainer = view.findViewById(R.id.menu_domain_container);
        mDomainContainer.setOnClickListener(this);
        fillDomain();
        setActiveMenuItem(mCurrentMenuItem);
        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_MENU_ITEM_KEY, mCurrentMenuItem);
        super.onSaveInstanceState(outState);
    }


    private void fillDomain() {
        Domain domain = DomainUtil.getDomain(K5Api.getDomain(getActivity()));
        if (domain == null) {
            return;
        }
        mDomainTitle.setText(domain.getTitle());
        mDomainUrl.setText(domain.getDomain());
        mDomainLogo.setImageResource(domain.getLogo());

    }

    private void selectItem(View selectedItem) {
        if (mMenuItems == null) {
            return;
        }
        for (MenuItemWidget item : mMenuItems) {
            item.setSelected(item == selectedItem);
        }
    }

    public void setActiveMenuItem(int index) {
        mCurrentMenuItem = index;
        selectItem(getItem(index));
    }

    private MenuItemWidget getItem(int index) {
        switch (index) {
            case MENU_HELP:
                return mMenuHelp;
            case MENU_HOME:
                return mMenuHome;
            case MENU_RECENT:
                return mMenuRecent;
            case MENU_SEARCH:
                return mMenuSearch;
            case MENU_SETTINGS:
                return mMenuSettings;
            case MENU_VIRTUAL_COLLECTION:
                return mMenuVirtual;
            default:
                return null;
        }
    }

    public void setCallback(MainMenuListener callback) {
        mCallback = callback;
    }

    public interface MainMenuListener {
        public void onLogin();

        public void onHome();

        public void onSettings();

        public void onVirtualCollections();

        public void onSearch();

        public void onHelp();

        public void onAbout();

        public void onRecent();

    }

    @Override
    public void onClick(View v) {
        if (mCallback == null) {
            return;
        }
        if (v == mMenuHome) {
            Analytics.sendEvent(getActivity(), "main_menu", "action", "Home");
            setActiveMenuItem(MENU_HOME);
            mCallback.onHome();
        } else if (v == mMenuHelp) {
            Analytics.sendEvent(getActivity(), "main_menu", "action", "Help");
            mCallback.onHelp();
        } else if (v == mMenuRecent) {
            Analytics.sendEvent(getActivity(), "main_menu", "action", "Recent");
            setActiveMenuItem(MENU_RECENT);
            mCallback.onRecent();
        } else if (v == mMenuSearch) {
            Analytics.sendEvent(getActivity(), "main_menu", "action", "Search");
            setActiveMenuItem(MENU_SEARCH);
            mCallback.onSearch();
        } else if (v == mMenuSettings) {
            Analytics.sendEvent(getActivity(), "main_menu", "action", "Setting");
            mCallback.onSettings();
        } else if (v == mMenuVirtual) {
            Analytics.sendEvent(getActivity(), "main_menu", "action", "Collections");
            setActiveMenuItem(MENU_VIRTUAL_COLLECTION);
            mCallback.onVirtualCollections();
        } else if (v == mDomainContainer) {
            Analytics.sendEvent(getActivity(), "main_menu", "action", "Domain");
            onSelectDomain();
        }
    }

    private void onSelectDomain() {
        Intent intent = new Intent(getActivity(), DomainActivity.class);
        startActivity(intent);
    }

}
