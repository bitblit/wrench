package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * Allows java.net.URLConnection to use self signed certs
 *
 * While this isn't usually a good idea in production, there are many, many times you need this in dev
 * Adds a static method that modifies the default config in java to allow through what are essentially
 * "invalid" certs.
 *
 * Created by cweiss1271 on 2/3/16.
 */
public class AllowSelfSignedHttps {
    private static final Logger LOG = LoggerFactory.getLogger(AllowSelfSignedHttps.class);
    private static boolean ADJUSTMENT_APPLIED = false;

    private AllowSelfSignedHttps() {
        // Prevent instantiation
        super();
    }

    public static void allowSelfSignedHttpsCertificates() {
        if (!ADJUSTMENT_APPLIED) {
            LOG.warn("Allowing self-signed certificates - DO NOT USE IN PRODUCTION - allows MITM attacks");
            try {
    /*
     *  fix for
     *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
     *       sun.security.validator.ValidatorException:
     *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
     *               unable to find valid certification path to requested target
     */
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }


                        }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                ADJUSTMENT_APPLIED = true;
            } catch (Exception e) {
                LOG.warn("Error trying to allow https certificates", e);
            }
        } else {
            // Do nothing, already ran
        }
    }
}
