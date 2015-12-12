package cz.mzk.kramerius.app.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.KrameriusApplication;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.card.DomainCard;
import cz.mzk.kramerius.app.card.DomainCard.OnDomainPopupListener;
import cz.mzk.kramerius.app.data.KrameriusContract;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.DomainUtil;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.Card.OnLongCardClickListener;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class DomainActivity extends BaseActivity implements OnDomainPopupListener {

    public static final String TAG = DomainActivity.class.getName();

    private CardListView mList;

    private CardArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domain);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.domain_title);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mList = (CardListView) findViewById(R.id.card_list);
        populate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 999:
                refreshLibraries();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populate() {
        String currentDomain = K5Api.getDomain(this); // TODO
        ArrayList<Card> cards = new ArrayList<Card>();
        boolean allDomains = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_all_sources), false);
        for (Domain domain : DomainUtil.getDomains(allDomains)) {
            DomainCard card = new DomainCard(this, domain);
            card.setOnDomainPopupListener(this);
            card.setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    Domain domain = ((DomainCard) card).getDomain();
                    Analytics.sendEvent(DomainActivity.this, "domain", domain.getDomain(), "from_click");
                    onDomainSelected(domain);
                }
            });
            card.setOnLongClickListener(new OnLongCardClickListener() {
                @Override
                public boolean onLongClick(Card card, View view) {
                    Domain domain = ((DomainCard) card).getDomain();
                    Analytics.sendEvent(DomainActivity.this, "domain_detail", domain.getDomain(), "from_popup");
                    onDomainDetail(domain);
                    return false;
                }
            });
            cards.add(card);
        }
        mAdapter = new CardArrayAdapter(this, cards);
        CardUtils.setAnimationAdapter(mAdapter, mList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean allDomains = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_all_sources), false);
        if(allDomains) {
            MenuItem item = menu.add(Menu.NONE, 999, Menu.NONE, "Obnovit");
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            item.setIcon(android.R.drawable.ic_menu_add);
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    private void onDomainSelected(Domain domain) {
        K5Api.setDomain(this, domain);
        Intent intent = new Intent(DomainActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void onDomainDetail(Domain domain) {
        Intent intent = new Intent(DomainActivity.this, DomainDetailActivity.class);
        intent.putExtra(BaseActivity.EXTRA_DOMAIN, domain.getDomain());
        startActivity(intent);
    }

    @Override
    public void onDomainPopupOpen(Domain domain) {
        Analytics.sendEvent(this, "domain", domain.getDomain(), "from_popup");
        onDomainSelected(domain);
    }

    @Override
    public void onDomainPopupContent(Domain domain) {
        Analytics.sendEvent(this, "domain_detail", domain.getDomain(), "from_popup");
        onDomainDetail(domain);
    }


    private void refreshLibraries() {
        new GetLibrariesTask().execute();
    }


    class GetLibrariesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return K5ConnectorFactory.getConnector().reloadTestLibraries(DomainActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                onLibrariesReloaded();
            }
        }
    }

    private void onLibrariesReloaded() {
        KrameriusApplication.getInstance().reloadLibraries();
        populate();
    }


}
