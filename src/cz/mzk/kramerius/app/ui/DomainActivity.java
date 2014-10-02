package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import com.google.analytics.tracking.android.EasyTracker;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.card.DomainCard;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.util.CardUtils;

public class DomainActivity extends BaseActivity {

	public static final String TAG = DomainActivity.class.getName();

	private CardListView mList;

	private CardArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_domain);

		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Zdroj dat");
		mList = (CardListView) findViewById(R.id.card_list);
		populate();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void populate() {
		String currentDomain = K5Api.getDomain(this); // TODO
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Domain domain : getTestDomainList()) {
			DomainCard card = new DomainCard(this, domain);
			card.setOnClickListener(new OnCardClickListener() {
				@Override
				public void onClick(Card card, View view) {
					onDomainSelected(((DomainCard) card).getDomain());
				}
			});
			cards.add(card);
		}
		mAdapter = new CardArrayAdapter(this, cards);
		CardUtils.setAnimationAdapter(mAdapter, mList);
	}

	private List<Domain> getTestDomainList() {
		List<Domain> list = new ArrayList<Domain>();
		list.add(new Domain("Moravská zemská knihovna", "Digitální knihovna MZK", "http", "kramerius.mzk.cz",
				R.drawable.logo_mzk));
		list.add(new Domain("Národní knihovna", "Digitální knihovna NKP", "http", "kramerius4.nkp.cz",
				R.drawable.logo_nkp));
		list.add(new Domain("Národní digitální knihovna", "Digitální knihovna NDK", "http", "krameriusndktest.mzk.cz",
				R.drawable.logo_ndk));
		list.add(new Domain("Knihovna Akademie věd ČR", "Digitální knihovna KNAV", "http", "kramerius.lib.cas.cz",
				R.drawable.logo_knav));

		// list.add(new Domain("Vědecká knihovna v Olomouci",
		// "Digitální knihovna VKP", "http", "kramerius.vkp.cz",
		// R.drawable.logo_vkol));
		// list.add(new Domain("Knihovna Akademie věd ČR",
		// "Digitální knihovna KNAV", "http", "cdk-test.lib.cas.cz",
		// R.drawable.logo_knav));
		// list.add(new Domain("Národní technická knihovna",
		// "Digitální knihovna NTK", "http", "kramerius.ntk.cz",
		// R.drawable.logo_ntk));
		list.add(new Domain("Moravská zemská knihovna", "Docker MZK", "http", "docker.mzk.cz", R.drawable.logo_mzk));
		// list.add(new Domain("Moravská zemská knihovna", "Test MZK", "http",
		// "krameriustest.mzk.cz", R.drawable.logo_mzk));
		// list.add(new Domain("Moravská zemská knihovna", "Demo MZK", "http",
		// "krameriusdemo.mzk.cz", R.drawable.logo_mzk));

		return list;
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
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putString(getString(R.string.pref_domain_key), domain.getDomain())
				.putString(getString(R.string.pref_protocol_key), domain.getProtocol()).commit();
		K5Connector.getInstance().restart();
		Intent intent = new Intent(DomainActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
