package cz.mzk.kramerius.app.ui;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ModelUtil;

public class LauncherActivity extends Activity {

	public static final String TAG = LauncherActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			String pid = null;
			Uri data = getIntent().getData();
			if (data != null) {
				List<String> pathSegments = data.getPathSegments();
				if (pathSegments.size() >= 3 && "handle".equals(pathSegments.get(1))) {
					pid = pathSegments.get(2);
				} else {
					pid = data.getQueryParameter("pid");
				}
			}
			if (pid != null) {
				String domain = data.getHost();
				String protocol = data.getScheme();
				K5Api.setDomain(this, domain, protocol);
				goToDocument(pid);
				return;
			}
		}
		finish();
	}

	private void goToDocument(String pid) {
		new ResolveReferencedDocumentTask().execute(pid);
	}

	class ResolveReferencedDocumentTask extends AsyncTask<String, Void, Item> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Item doInBackground(String... params) {
			return K5ConnectorFactory.getConnector().getItem(LauncherActivity.this, params[0]);
		}

		@Override
		protected void onPostExecute(Item item) {
			if (LauncherActivity.this == null || item == null) {
				return;
			}
			ModelUtil.startActivityByModel(LauncherActivity.this, item);
			finish();
		}

	}

}
