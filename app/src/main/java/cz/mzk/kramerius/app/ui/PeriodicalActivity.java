package cz.mzk.kramerius.app.ui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ModelUtil;

public class PeriodicalActivity extends BaseActivity implements OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_periodical);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String pid = getIntent().getExtras().getString(EXTRA_PID);
        PeriodicalFragment fragment = PeriodicalFragment.newInstance(pid);
        fragment.setOnItemSelectedListener(this);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.periodical_fragment_container, fragment).commit();
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

    @Override
    public void onItemSelected(Item item) {
        Intent intent = null;
        if (ModelUtil.PERIODICAL_VOLUME.equals(item.getModel())) {
            intent = new Intent(PeriodicalActivity.this, PeriodicalActivity.class);
        } else if (ModelUtil.PERIODICAL_ITEM.equals(item.getModel())) {
            intent = new Intent(PeriodicalActivity.this, PageActivity.class);
        } else {
            return;
        }
        intent.putExtra(EXTRA_PID, item.getPid());
        startActivity(intent);

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

}