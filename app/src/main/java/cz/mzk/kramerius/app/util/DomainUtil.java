package cz.mzk.kramerius.app.util;

import android.content.Context;

import java.util.List;

import cz.mzk.kramerius.app.KrameriusApplication;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Domain;

public class DomainUtil {

    public static List<Domain> getDomains() {
        return KrameriusApplication.getInstance().getLibraries();
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
