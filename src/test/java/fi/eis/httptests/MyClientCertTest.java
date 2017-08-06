package fi.eis.httptests;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MyClientCertTest {

    @BeforeClass
    public static void setUp() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "DEBUG");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl.conn.PoolingHttpClientConnectionManager", "DEBUG");
        //System.setProperty("javax.net.debug","all");
    }

    private static final String KEYSTOREPATH = "/clientkeystore.p12"; // or .jks
    private static final String KEYSTOREPASS = "keystorepass";
    private static final String KEYPASS = "keypass";

    static KeyStore readStore() throws Exception {
        try (InputStream keyStoreStream = MyClientCertTest.class.getResourceAsStream(KEYSTOREPATH)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12"); // or JKS
            keyStore.load(keyStoreStream, KEYSTOREPASS.toCharArray());
            return keyStore;
        }
    }

    static SSLContext sslContext() throws Exception {
        return SSLContexts.custom()
                .loadKeyMaterial(readStore(), KEYPASS.toCharArray())
                .build();
    }

    @Test
    public void readKeyStore() throws Exception {
        assertNotNull(readStore());
    }

    @Test
    public void performClientRequest() throws Exception {

        HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext()).build();
        HttpResponse response = httpClient.execute(new HttpGet("https://slsh.iki.fi/client-certificate/protected/"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        EntityUtils.consume(entity);
    }

    @Test
    @Ignore("code that does not work")
    public void performClientRequestWithConnectionPool() throws Exception {

        PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager(); // this way it doesn't know about ssl context -> fails
        HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext()).setConnectionManager(cm).build();
        HttpResponse response = httpClient.execute(new HttpGet("https://slsh.iki.fi/client-certificate/protected/"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        EntityUtils.consume(entity);
    }
    @Test
    public void performClientRequestWithConnectionPool2() throws Exception {
        SSLContext sslContext = sslContext();
        DefaultHostnameVerifier hostnameVerifierCopy = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
        SSLConnectionSocketFactory sslSocketFactoryCopy = new SSLConnectionSocketFactory(
                sslContext, null, null, hostnameVerifierCopy);

        PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactoryCopy)
                        .build());

        HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).setConnectionManager(cm).build();
        HttpResponse response = httpClient.execute(new HttpGet("https://slsh.iki.fi/client-certificate/protected/"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        EntityUtils.consume(entity);
    }

    @Test
    public void performAsyncClientRequest() throws Exception {
        CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom().setSSLContext(sslContext())
                .build();
        httpClient.start();
        Future<HttpResponse> futureResponse = httpClient.execute(new HttpGet("https://slsh.iki.fi/client-certificate/protected/"), null);
        HttpResponse response = futureResponse.get();
        assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        EntityUtils.consume(entity);
        httpClient.close();
    }
    @Test
    public void performAsyncClientRequestWithConnectionPool() throws Exception {
        SSLContext sslContext = sslContext();
        DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
        SchemeIOSessionStrategy reuseStrategy = new SSLIOSessionStrategy(sslContext, null, null, hostnameVerifier);

        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm =
                new PoolingNHttpClientConnectionManager(ioReactor,
                        RegistryBuilder.<SchemeIOSessionStrategy>create().register("http", NoopIOSessionStrategy.INSTANCE).register("https", reuseStrategy).build());
        CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom().setConnectionManager(cm).setSSLContext(sslContext())
                .build();
        httpClient.start();
        Future<HttpResponse> futureResponse = httpClient.execute(new HttpGet("https://slsh.iki.fi/client-certificate/protected/"), null);
        HttpResponse response = futureResponse.get();
        assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        EntityUtils.consume(entity);
        httpClient.close();
    }
}
