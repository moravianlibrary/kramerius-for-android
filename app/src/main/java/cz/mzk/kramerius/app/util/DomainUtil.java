package cz.mzk.kramerius.app.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.KrameriusApplication;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Domain;

public class DomainUtil {
/*
    private static List<Domain> DOMAINS = new ArrayList<Domain>() {
        private static final long serialVersionUID = -8100284677714939192L;
        private static final int LOGO_DEFAULT = R.mipmap.ic_launcher;

        {

            add(new Domain(true, "Moravská zemská knihovna", "http", "kramerius.mzk.cz", R.drawable.logo_mzk));
            add(new Domain(true, "Národní digitální knihovna", "http", "krameriusndktest.mzk.cz", R.drawable.logo_ndk));
            add(new Domain(true, "Jihočeská vědecká knihovna v Českých Budějovicích", "http", "kramerius.cbvk.cz", R.drawable.logo_cbvk));
            add(new Domain(true, "Vědecká knihovna v Olomouci", "http", "kramerius.kr-olomoucky.cz", R.drawable.logo_vkol));
            add(new Domain(true, "Studijní a vědecká knihovna v Hradci Králové", "http", "kramerius4.svkhk.cz", R.drawable.logo_svkhk));
            add(new Domain(true, "Městská knihovna Česká Třebová", "http", "k5.digiknihovna.cz", R.drawable.logo_mkct));
            add(new Domain(true, "Severočeská vědecká knihovna v Ústí nad Labem", "http", "kramerius.svkul.cz", R.drawable.logo_svkul));
            add(new Domain(true, "Krajská knihovna Františka Bartoše ve Zlíně", "http", "dlib.kfbz.cz", R.drawable.logo_kfbz));

            add(new Domain(true, "Digitální studovna Ministerstva obrany ČR", "http", "kramerius.army.cz", R.drawable.logo_dsmo));
            add(new Domain(true, "Národní technická knihovna", "http", "kramerius.techlib.cz", R.drawable.logo_ntk));
            add(new Domain(true, "Národní filmový archiv", "http", "library.nfa.cz", R.drawable.logo_nfa));
            add(new Domain(true, "Krajská knihovna Karlovy Vary", "http", "k4.kr-karlovarsky.cz", R.drawable.logo_kkkv));
            add(new Domain(true, "Univerzita Karlova v Praze - Fakulta sociálních věd", "http", "kramerius.fsv.cuni.cz", R.drawable.logo_cuni_fsv));
            add(new Domain(true, "Knihovna Akademie věd ČR", "http", "kramerius.lib.cas.cz", R.drawable.logo_knav));
            add(new Domain(true, "Krajská vědecká knihovna v Liberci", "http", "kramerius.kvkli.cz", R.drawable.logo_kvkli));
            add(new Domain(true, "Knihovna Antonína Švehly", "http", "kramerius.uzei.cz", R.drawable.logo_uzei));

            add(new Domain(false, "Moravskoslezská vědecká knihovna v Ostravě", "http", "camea.svkos.cz", R.drawable.logo_svkos));
            add(new Domain(false, "Mendelova univerzita v Brně", "http", "kramerius4.mendelu.cz", R.drawable.logo_mendelu));
            add(new Domain(false, "Židovské muzeum v Praze", "http", "kramerius4.jewishmuseum.cz", R.drawable.logo_zmp));
            add(new Domain(false, "Národní muzeum", "http", "kramerius.nm.cz", R.drawable.logo_nm));
            add(new Domain(false, "Slezská univerzita v Opavě", "http", "kramerius.slu.cz", R.drawable.logo_slu));
            add(new Domain(false, "Lesnický a myslivecký digitální archiv", "http", "lmda.silvarium.cz", LOGO_DEFAULT));
            add(new Domain(false, "Univerzitná knižnica v Bratislave", "http", "pc139.ulib.sk", LOGO_DEFAULT));
            add(new Domain(false, "Výzkumný ústav geodetický, topografický a kartografický", "http", "knihovna-test.vugtk.cz", LOGO_DEFAULT));
            add(new Domain(false, "Studijní a vědecká knihovna Plzeňského kraje", "http", "k4.svkpl.cz", R.drawable.logo_svkpk));
            add(new Domain(false, "Knihovna Západočeského muzea v Plzni", "http", "kramerius.zcm.cz", R.drawable.logo_zcm));
            add(new Domain(false, "Městská knihovna v Praze", "http", "kramerius4.mlp.cz", R.drawable.logo_mlp));
            add(new Domain(false, "Národní knihovna", "http", "kramerius4.nkp.cz", R.drawable.logo_nkp));
            add(new Domain(false, "Středočeská vědecká knihovna v Kladně", "http", "kramerius.svkkl.cz", R.drawable.logo_svkkl));
            add(new Domain(false, "Česká digitální knihovna", "http", "cdk.lib.cas.cz", R.drawable.logo_cdk));

            add(new Domain(false, "Moravská zemská knihovna - Docker (https)", "https", "docker.mzk.cz", R.drawable.logo_mzk));
            add(new Domain(false, "Moravská zemská knihovna - Demo", "http", "krameriusdemo.mzk.cz", R.drawable.logo_mzk));
        }
    };
*/
    private static List<Domain> getDomains() {
        //return DOMAINS;
        return KrameriusApplication.getInstance().getLibraries();
    }

//    public static List<Domain> getAllDomains() {
//        return DOMAINS;
//    }

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
            return getDomains();
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
