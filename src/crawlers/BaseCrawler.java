package crawlers;

import com.cybozu.labs.langdetect.LangDetectException;
import crawlerUtils.Filter;
import crawlerUtils.LangDetect;
import dbUtils.DatabaseUtils;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import model.Metrics;

public class BaseCrawler {

    // declare variables
    protected boolean danish = false;
    protected final Metrics METRICS;

    protected String URL;
    protected String area;
    protected String jobCategory;
    protected String foundAt;
    protected LangDetect languageDetector;
    protected Filter filter;
    protected DatabaseUtils dbUtils;

    // constructor
    public BaseCrawler(String URL, String area, String jobCategory, String foundAt, LangDetect languageDetector) {

        this.URL = URL;
        this.area = area;
        this.jobCategory = jobCategory;
        this.foundAt = foundAt;
        this.languageDetector = languageDetector;

        filter = new Filter();
        METRICS = new Metrics(foundAt);
        dbUtils = new DatabaseUtils();

        try {
            setTrustAllCerts();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            METRICS.incrementExceptions();
        }

    }

// methods
    // TODO: finalize and settle one one Manager. Make tests and choose
        private void setTrustAllCerts() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
        };

        try {
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSLv3");

            } catch (NoSuchAlgorithmException e) {
                METRICS.incrementExceptions();
                e.printStackTrace(System.out);
            }

            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory factory = sslContext.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(factory);
        } catch (KeyManagementException e) {
            METRICS.incrementExceptions();
            e.printStackTrace(System.out);
        }
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
        
//    private void setTrustAllCerts() throws Exception {
//        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                return null;
//            }
//
//            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
//            }
//
//            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
//            }
//        }};
//
//        // Install the all-trusting trust manager
//        try {
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//                public boolean verify(String urlHostName, SSLSession session) {
//                    return true;
//                }
//            });
//        } catch (Exception e) {
//            // We can not recover from this exception.
//            e.printStackTrace(System.out);
//            METRICS.incrementExceptions();
//        }
//    }
    
    protected boolean isEnglish(String text) {
        boolean english = false;
        try {
            english = languageDetector.detect(text).equalsIgnoreCase("en");
        } catch (LangDetectException e) {
            METRICS.incrementExceptions();
            // e.printStackTrace(System.out);
        }

        return english;
    }

    protected String sanitizeUrl(String msg) throws UnsupportedEncodingException {
        String decoded = java.net.URLDecoder.decode(msg, "UTF-8");
        return decoded;
    }
}
