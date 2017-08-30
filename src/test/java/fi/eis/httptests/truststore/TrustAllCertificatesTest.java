package fi.eis.httptests.truststore;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TrustAllCertificatesTest {
    final String expiredCertSite = "https://expired.badssl.com/";
    final String selfSignedCertSite = "https://self-signed.badssl.com/";
    final String wrongHostCertSite = "https://wrong.host.badssl.com/";

    static final TrustStrategy trustSelfSignedStrategy = new TrustSelfSignedStrategy();
    static final TrustStrategy trustAllStrategy = new TrustStrategy(){
        public boolean isTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            return true;
        }
    };

    @Test
    public void testSelfSignedOnSelfSignedUsingCode() throws Exception {
        doGet(selfSignedCertSite, trustSelfSignedStrategy);
    }
    @Test(expected = SSLHandshakeException.class)
    public void testExpiredOnSelfSignedUsingCode() throws Exception {
        doGet(expiredCertSite, trustSelfSignedStrategy);
    }
    @Test(expected = SSLPeerUnverifiedException.class)
    public void testWrongHostOnSelfSignedUsingCode() throws Exception {
        doGet(wrongHostCertSite, trustSelfSignedStrategy);
    }

    @Test
    public void testSelfSignedOnTrustAllUsingCode() throws Exception {
        doGet(selfSignedCertSite, trustAllStrategy);
    }
    @Test
    public void testExpiredOnTrustAllUsingCode() throws Exception {
        doGet(expiredCertSite, trustAllStrategy);
    }
    @Test(expected = SSLPeerUnverifiedException.class)
    public void testWrongHostOnTrustAllUsingCode() throws Exception {
        doGet(wrongHostCertSite, trustAllStrategy);
    }

    @Test
    public void testSelfSignedOnAllowAllUsingCode() throws Exception {
        doGet(selfSignedCertSite, trustAllStrategy, NoopHostnameVerifier.INSTANCE);
    }
    @Test
    public void testExpiredOnAllowAllUsingCode() throws Exception {
        doGet(expiredCertSite, trustAllStrategy, NoopHostnameVerifier.INSTANCE);
    }
    @Test
    public void testWrongHostOnAllowAllUsingCode() throws Exception {
        doGet(expiredCertSite, trustAllStrategy, NoopHostnameVerifier.INSTANCE);
    }

    public void doGet(String url, TrustStrategy trustStrategy, HostnameVerifier hostnameVerifier) throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(trustStrategy);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                sslsf).setSSLHostnameVerifier(hostnameVerifier).build();

        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
    }
    public void doGet(String url, TrustStrategy trustStrategy) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(trustStrategy);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();

        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
    }
}
