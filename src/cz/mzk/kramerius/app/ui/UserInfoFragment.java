package cz.mzk.kramerius.app.ui;

import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.User;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class UserInfoFragment extends Fragment implements OnClickListener {

	private static final String TAG = UserInfoFragment.class.getName();

	private TextView mUserName;
	private TextView mName;
	private Button mLogOut;
	private Button mChangePassword;
	private LinearLayout mRoles;
	private LinearLayout mRights;
	private UserInfoListener mCallback;

	public interface UserInfoListener {
		public void onLogOut();
	}

	public void setCallback(UserInfoListener callback) {
		mCallback = callback;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_user_info, container, false);
		ScreenUtil.setInsets(getActivity(), view);
		mUserName = (TextView) view.findViewById(R.id.userInfo_userName);
		mName = (TextView) view.findViewById(R.id.userInfo_name);
		mRoles = (LinearLayout) view.findViewById(R.id.userInfo_roles);
		mRights = (LinearLayout) view.findViewById(R.id.userInfo_rights);
		mLogOut = (Button) view.findViewById(R.id.userInfo_signoff);
		mLogOut.setOnClickListener(this);
		mChangePassword = (Button) view.findViewById(R.id.userInfo_changePassword);
		mChangePassword.setOnClickListener(this);
		new GetUserInfoTask(getActivity()).execute();
		new GetUserRightsTask(getActivity()).execute();
		return view;
	}

	class GetUserInfoTask extends AsyncTask<Void, Void, User> {

		private Context tContext;

		public GetUserInfoTask(Context context) {
			tContext = context;
		}

		@Override
		protected User doInBackground(Void... params) {
			return K5Connector.getInstance().getUserInfo(tContext);
		}

		@Override
		protected void onPostExecute(User user) {
			populateUser(user);
		}
	}
	
	class GetUserRightsTask extends AsyncTask<Void, Void, Map<String, Boolean>> {

		private Context tContext;

		public GetUserRightsTask(Context context) {
			tContext = context;
		}

		@Override
		protected Map<String, Boolean> doInBackground(Void... params) {
			return K5Connector.getInstance().getUserRights(tContext);
		}

		@Override
		protected void onPostExecute(Map<String, Boolean> result) {
			populateUserRights(result);
		}
	}	

	private void populateUser(User user) {
		if (getActivity() == null) {
			return;
		}
		
		if (user == null) {
			return;
		}
		mName.setText(user.getFirstName() + " " + user.getSurname());
		mUserName.setText(user.getLogin());

		for (String role : user.getRoles()) {
			TextView tv = new TextView(getActivity());
			tv.setText(role);
			tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tv.setTextColor(getActivity().getResources().getColor(R.color.grey));
			tv.setTypeface(null, Typeface.ITALIC);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			mRoles.addView(tv);
		}

	}
	
	private void populateUserRights(Map<String, Boolean> rights) {
		if (getActivity() == null) {
			return;
		}
		
		if (rights == null) {
			return;
		}
		

		for (String right : rights.keySet()) {
			TextView tv = new TextView(getActivity());
			tv.setText(right + ": " + (rights.get(right) ? "Ano" : "Ne"));
			tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tv.setTextColor(getActivity().getResources().getColor(R.color.grey));
			tv.setTypeface(null, Typeface.ITALIC);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			mRights.addView(tv);
		}

	}	

	@Override
	public void onClick(View v) {
		if (v == mLogOut) {
			if (mCallback != null) {
				mCallback.onLogOut();
			}
		}
	}

}
