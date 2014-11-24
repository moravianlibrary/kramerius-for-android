package cz.mzk.kramerius.app.util;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Domain;

public class DomainUtil {

	private static List<Domain> DOMAINS = new ArrayList<Domain>() {

		private static final long serialVersionUID = -8100284677714939192L;

		{
			add(new Domain("Moravská zemská knihovna", "Digitální knihovna MZK", "http", "kramerius.mzk.cz",
					R.drawable.logo_mzk));
			add(new Domain("Národní knihovna", "Digitální knihovna NKP", "http", "kramerius4.nkp.cz",
					R.drawable.logo_nkp));
			add(new Domain("Národní digitální knihovna", "Digitální knihovna NDK", "http", "krameriusndktest.mzk.cz",
					R.drawable.logo_ndk));
			add(new Domain("Knihovna Akademie věd ČR", "Digitální knihovna KNAV", "http", "kramerius.lib.cas.cz",
					R.drawable.logo_knav));
			add(new Domain("Česká digitální knihovna", "Digitální knihovna ČDK", "http", "cdk-test.lib.cas.cz",
					R.drawable.logo_cdk));
			add(new Domain("Národní technická knihovna", "Digitální knihovna NTK", "http", "kramerius.techlib.cz",
					R.drawable.logo_ntk));
			add(new Domain("Moravská zemská knihovna", "Docker MZK", "http", "docker.mzk.cz", R.drawable.logo_mzk));
		}
	};

	public static List<Domain> getDomains() {
		return DOMAINS;
	}

	public static Domain getDomain(String domain) {
		for (Domain d : DOMAINS) {
			if (d.getDomain().equals(domain)) {
				return d;
			}
		}
		return null;
	}

}
