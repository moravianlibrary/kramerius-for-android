package cz.mzk.kramerius.app.api;

public class K5ConnectorFactory {

    private static final String LOG_TAG = K5ConnectorFactory.class.getName();

    private static K5ConnectorImplHttpUrlConnection INSTANCE;

    public static K5ConnectorImplHttpUrlConnection getConnector() {
        if (INSTANCE == null) {
            INSTANCE = new K5ConnectorImplHttpUrlConnection();
        }
        return INSTANCE;
    }

}
