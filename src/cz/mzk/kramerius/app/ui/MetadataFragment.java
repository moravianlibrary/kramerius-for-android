package cz.mzk.kramerius.app.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Metadata;

public class MetadataFragment extends BaseFragment {

	private static final String TAG = MetadataFragment.class.getName();

	
	private ViewGroup mContainer;
	private TextView mName;
	private TextView mTitle;
	private TextView mSubtitle;
	private TextView mPublisherName;
	private TextView mAuthorName;
	private TextView mAuthorDate;
	private TextView mIssn;
	private TextView mNote;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_metadata, container, false);

		mContainer = (ViewGroup) view.findViewById(R.id.metadata_container);
		mName = (TextView) view.findViewById(R.id.metadata_name);
		mTitle = (TextView) view.findViewById(R.id.metadata_title);
		mSubtitle = (TextView) view.findViewById(R.id.metadata_subtitle);
		mPublisherName = (TextView) view.findViewById(R.id.metadata_publisher);
		mAuthorName = (TextView) view.findViewById(R.id.metadata_authorName);
		mAuthorDate = (TextView) view.findViewById(R.id.metadata_authorDate);
		mIssn = (TextView) view.findViewById(R.id.metadata_issn);
		mNote = (TextView) view.findViewById(R.id.metadata_note);
		mTitle.setText("");
		mSubtitle.setText("");
		mName.setText("");
		mAuthorName.setText("");
		mAuthorDate.setText("");
		mPublisherName.setText("");
		mNote.setText("");
		mIssn.setText("");
		mContainer.setVisibility(View.GONE);
		inflateLoader((ViewGroup) view, inflater);
		return view;
	}

	public void assignPid(String pid) {
		new getMetadataTask(getActivity()).execute(pid);
	}

	private void populateMetadata(Metadata metadata) {
		mTitle.setText(metadata.getTitle());
		mSubtitle.setText(metadata.getSubtitle());
		mName.setText(metadata.getTitle());
		mAuthorName.setText(metadata.getAuthorName());
		mAuthorDate.setText(metadata.getAuthorDate());
		mPublisherName.setText(metadata.getPublisher());
		mNote.setText(metadata.getNote());
		mIssn.setText(metadata.getIssn());
	}

	class getMetadataTask extends AsyncTask<String, Void, Metadata> {

		private Context tContext;

		public getMetadataTask(Context context) {
			tContext = context;
		}
		
		@Override
		protected void onPreExecute() {	
			mContainer.setVisibility(View.GONE);
			startLoaderAnimation();
		}

		@Override
		protected Metadata doInBackground(String... params) {
			String pid = params[0];
			return K5Connector.getInstance().getModsMetadata(tContext, pid);
		}

		@Override
		protected void onPostExecute(Metadata metadata) {
			if (metadata != null) {
				populateMetadata(metadata);
			}
			stopLoaderAnimation();
			mContainer.setVisibility(View.VISIBLE);
		}

	}

}
