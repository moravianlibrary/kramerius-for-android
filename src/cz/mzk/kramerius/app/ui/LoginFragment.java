package cz.mzk.kramerius.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.User;
import cz.mzk.kramerius.app.util.Analytics;

public class LoginFragment extends Fragment {

	private static final String TAG = LoginFragment.class.getName();

	private EditText mLogin;
	private EditText mPassword;
	private Button mConfirm;
	private TextView mMessage;
	private LoginListener mLoginListener;

	
	public interface LoginListener {
		public void onLoginSuccess();
	}
	
	public void setLoginListener(LoginListener loginListener) {
		mLoginListener = loginListener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login, container, false);
	//	ScreenUtil.setInsets(getActivity(), view);
		mLogin = (EditText) view.findViewById(R.id.login_userName);
		mPassword = (EditText) view.findViewById(R.id.login_userPassword);
		mConfirm = (Button) view.findViewById(R.id.login_confirm);
		mConfirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onLogin();
			}
		});
		mMessage = (TextView) view.findViewById(R.id.login_message);
		mMessage.setVisibility(View.GONE);
		return view;
	}

	private void onLogin() {
		String userName = mLogin.getText().toString();
		String password = mPassword.getText().toString();
		if (userName == null || userName.isEmpty() || password == null || password.isEmpty()) {
			message("Musíte nejdříve vyplnit uživatelské jméno a heslo", true);
		} else {
			new LoginTask(getActivity()).execute(userName, password);
		}
	}

	private void message(String message, boolean incorrect) {
		mMessage.setText(message);
		mMessage.setVisibility(View.VISIBLE);
		if (incorrect) {
			mMessage.setTextColor(getActivity().getResources().getColor(R.color.red));
		} else {
			mMessage.setTextColor(getActivity().getResources().getColor(R.color.color_primary));
		}
	}

	private void onLoginSuccess(User user) {
		message("Přihlášení proběhlo úspěšně", false);
		K5Api.storeUser(getActivity(), user.getLogin(), user.getPassword());
		if(mLoginListener != null) {
			mLoginListener.onLoginSuccess();
		}		
	}

	private void onLoginFailed() {
		message("Přihlášení se nezdařilo", true);
	}

	class LoginTask extends AsyncTask<String, Void, User> {

		private Context tContext;

		public LoginTask(Context context) {
			tContext = context;
		}

		@Override
		protected User doInBackground(String... params) {
			return K5Connector.getInstance().getUserInfo(tContext, params[0], params[1]);
		}

		@Override
		protected void onPostExecute(User user) {
			if (user == null) {
				onLoginFailed();
			} else {
				onLoginSuccess(user);
			}
		}
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    Analytics.sendScreenView(getActivity(), R.string.ga_appview_login);
	}		
	

}
