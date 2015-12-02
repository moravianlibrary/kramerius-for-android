package cz.mzk.kramerius.app.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Domain;

public class DomainUtil {

    private static List<Domain> DOMAINS = new ArrayList<Domain>() {
        private static final long serialVersionUID = -8100284677714939192L;
        private static final int LOGO_DEFAULT = R.mipmap.ic_launcher;

        {

            add(new Domain(true, "Moravská zemská knihovna", "http", "kramerius.mzk.cz", R.drawable.logo_mzk));
            add(new Domain(true, "Národní digitální knihovna", "http", "krameriusndktest.mzk.cz", R.drawable.logo_ndk));
            add(new Domain(true, "Jihočeská vědecká knihovna v Českých Budějovicích", "http", "kramerius.cbvk.cz",
                    R.drawable.logo_cbvk));
            add(new Domain(true, "Vědecká knihovna v Olomouci", "http", "kramerius.kr-olomoucky.cz",
                    R.drawable.logo_vkol));
            add(new Domain(true, "Studijní a vědecká knihovna v Hradci Králové", "http", "kramerius4.svkhk.cz",
                    R.drawable.logo_svkhk));
            add(new Domain(true, "Městská knihovna Česká Třebová", "http", "k5.digiknihovna.cz",
                    R.drawable.logo_mkct));
            add(new Domain(true, "Severočeská vědecká knihovna v Ústí nad Labem", "http", "kramerius4.svkul.cz",
                    R.drawable.logo_svkul));
            add(new Domain(true, "Krajská knihovna Františka Bartoše ve Zlíně", "http", "dlib.kfbz.cz",
                    R.drawable.logo_kfbz));

            add(new Domain(false, "Krajská knihovna Karlovy Vary", "http", "k4.kr-karlovarsky.cz", R.drawable.logo_kkkv));
            add(new Domain(false, "Knihovna Akademie věd ČR", "http", "kramerius.lib.cas.cz", R.drawable.logo_knav));
            add(new Domain(false, "Knihovna Západočeského muzea v Plzni", "http", "kramerius.zcm.cz",
                    R.drawable.logo_zcm));
            add(new Domain(false, "Univerzita Karlova v Praze - Fakulta sociálních věd", "http",
                    "kramerius.fsv.cuni.cz", R.drawable.logo_cuni_fsv));
            add(new Domain(false, "Městská knihovna v Praze", "http", "kramerius4.mlp.cz", R.drawable.logo_mlp));
            // TODO: 26.10.15 Doplnit logo
            add(new Domain(false, "Krajská vědecká knihovna v Liberci", "http", "kramerius.kvkli.cz", LOGO_DEFAULT));
            add(new Domain(false, "Národní knihovna", "http", "kramerius4.nkp.cz", R.drawable.logo_nkp));
            add(new Domain(false, "Národní technická knihovna", "http", "kramerius.techlib.cz", R.drawable.logo_ntk));
            add(new Domain(false, "Středočeská vědecká knihovna v Kladně", "http", "kramerius.svkkl.cz",
                    R.drawable.logo_svkkl));
            add(new Domain(false, "Česká digitální knihovna", "http", "cdk-test.lib.cas.cz", R.drawable.logo_cdk));
            // add(new Domain("INCAD", "Test INCAD", "http", "sluzby.incad.cz/vmkramerius", R.drawable.logo_incad));
            add(new Domain(false, "Moravská zemská knihovna - Docker", "https", "docker.mzk.cz (https)", R.drawable.logo_mzk));
            add(new Domain(false, "Moravská zemská knihovna - Demo", "http", "krameriusdemo.mzk.cz",
                    R.drawable.logo_mzk));
        }
    };

    private static List<Domain> getDomains() {
        return DOMAINS;
    }

    public static List<Domain> getAllDomains() {
        return DOMAINS;
    }

    public static List<Domain> getUnlockedDomains() {
        List<Domain> list = new ArrayList<Domain>();
        for (Domain domain : getDomains()) {
            if (domain.isUnlocked()) {
                list.add(domain);
            }
        }
        return list;
    }

    public static List<Domain> getDomains(boolean all) {
        if (all) {
            return getAllDomains();
        } else {
            return getUnlockedDomains();
        }
    }

    public static Domain getDomain(String domain) {
        for (Domain d : getDomains()) {
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
