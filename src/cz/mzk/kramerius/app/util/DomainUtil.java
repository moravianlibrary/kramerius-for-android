package cz.mzk.kramerius.app.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Domain;

public class DomainUtil {

	private static List<Domain> DOMAINS = new ArrayList<Domain>() {

		private static final long serialVersionUID = -8100284677714939192L;

		{
			add(new Domain("Moravská zemská knihovna", "Digitální knihovna MZK", "http", "kramerius.mzk.cz",
					R.drawable.logo_mzk));
			add(new Domain("Jihočeská vědecká knihovna v Českých Budějovicích", "Digitální knihovna", "http", "kramerius.cbvk.cz",
					R.drawable.logo_cbvk));									
			add(new Domain("Národní digitální knihovna", "Digitální knihovna NDK", "http", "krameriusndktest.mzk.cz",
					R.drawable.logo_ndk));
			add(new Domain("Vědecká knihovna v Olomouci", "Digitální knihovna VKOL", "http", "kramerius.kr-olomoucky.cz",
					R.drawable.logo_vkol));
			add(new Domain("Krajská knihovna Karlovy Vary", "Digitální knihovna", "http", "k4.kr-karlovarsky.cz",
					R.drawable.logo_kkkv));		
			add(new Domain("Studijní a vědecká knihovna v Hradci Králové", "Digitální knihovna", "http", "kramerius4.svkhk.cz",
					R.drawable.logo_svkhk));	
			
			add(new Domain("Krajská vědecká knihovna v Liberci", "Digitální knihovna", "http", "kramerius.kvkli.cz",
					R.drawable.ic_launcher));			
			add(new Domain("Městská knihovna v Praze", "Digitální knihovna", "http", "kramerius4.mlp.cz",
					R.drawable.logo_mlp));			
			add(new Domain("Knihovna Akademie věd ČR", "Digitální knihovna KNAV", "http", "kramerius.lib.cas.cz",
					R.drawable.logo_knav));
			
														
			add(new Domain("Národní knihovna", "Digitální knihovna NKP", "http", "kramerius4.nkp.cz",
					R.drawable.logo_nkp));									
			add(new Domain("Národní technická knihovna", "Digitální knihovna NTK", "http", "kramerius.techlib.cz",
					R.drawable.logo_ntk));			
			add(new Domain("Univerzita Karlova v Praze", "Fakulta sociálních věd", "http", "kramerius.fsv.cuni.cz",
					R.drawable.logo_cuni_fsv));													
			add(new Domain("Severočeská vědecká knihovna v Ústí nad Labem", "Digitální knihovna", "http", "kramerius4.svkul.cz",
					R.drawable.logo_svkul));									
			add(new Domain("Středočeská vědecká knihovna v Kladně", "Digitální knihovna", "http", "kramerius.svkkl.cz",
					R.drawable.logo_svkkl));														
			add(new Domain("Knihovna Západočeského muzea v Plzni", "Digitální knihovna ZCM", "http", "kramerius.zcm.cz",
					R.drawable.logo_zcm));														

			add(new Domain("Krajská knihovna Františka Bartoše ve Zlíně", "Digitální knihovna  KFBZ", "http", "dlib.kfbz.cz", R.drawable.logo_kfbz));


			
			
			
			add(new Domain("Česká digitální knihovna", "Digitální knihovna ČDK", "http", "cdk-test.lib.cas.cz",
					R.drawable.logo_cdk));			
			add(new Domain("INCAD", "Test INCAD", "http", "sluzby.incad.cz/vmkramerius", R.drawable.logo_incad));
			add(new Domain("Moravská zemská knihovna", "Docker MZK", "http", "docker.mzk.cz", R.drawable.logo_mzk));
			add(new Domain("Moravská zemská knihovna", "Demo MZK", "http", "krameriusdemo.mzk.cz", R.drawable.logo_mzk));
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
	
	public static Domain getCurrentDomain(Context context) {
		return getDomain(K5Api.getDomain(context));
	}

}
