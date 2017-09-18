package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

/**
 * Allows java.net.URLConnection to use self signed certs
 * <p>
 * While this isn't usually a good idea in production, there are many, many times you need this in dev
 * Adds a static method that modifies the default config in java to allow through what are essentially
 * "invalid" certs.
 * <p>
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
        TrustManager[] trustAllCerts = new TrustManager[] {
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
        HttpsURLConnection.setDefaultSSLSocketFactory(new SSLSocketFactoryFacade(sc.getSocketFactory()));

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

  /**
   * This class does not DO anything it is here to work around a bug in the JDK, see:
   * https://stackoverflow.com/questions/30817934/extended-server-name-sni-extension-not-sent-with-jdk1-8-0-but-send-with-jdk1-7
   * http://www.oracle.com/technetwork/java/javase/2col/8u141-bugfixes-3720387.html
   * https://bugs.openjdk.java.net/browse/JDK-8144566
   */
  static class SSLSocketFactoryFacade extends SSLSocketFactory {

    SSLSocketFactory sslsf;

    public SSLSocketFactoryFacade(SSLSocketFactory sslsf) {
      this.sslsf = sslsf;
    }

    @Override
    public String[] getDefaultCipherSuites() {
      return sslsf.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
      return sslsf.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
      return sslsf.createSocket(socket, s, i, b);
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
      return sslsf.createSocket(s, i);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException {
      return sslsf.createSocket(s, i, inetAddress, i1);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
      return createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
      return createSocket(inetAddress, i, inetAddress1, i1);
    }
  }

}
