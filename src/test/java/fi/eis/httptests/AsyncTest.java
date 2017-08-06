package fi.eis.httptests;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Future;

import static fi.eis.httptests.MyClientCertTest.sslContext;

public class AsyncTest {
    @BeforeClass
    public static void setUp() {
        System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager","DEBUG");
    }
    @Test
    @Ignore
    public void poolAsyncRequestsNoClientCert() throws Exception {
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm =
                new PoolingNHttpClientConnectionManager(ioReactor);
        CloseableHttpAsyncClient client =
                HttpAsyncClients.custom().setConnectionManager(cm).build();
        client.start();

        String[] toGet = {
                "https://www.google.com/",
                "https://slsh.iki.fi/client-certificate/",
                "https://www.yahoo.com/"
        };

        for (int i = 0; i < 15; i++)
        {
            String url = toGet[i%(toGet.length)];
            System.out.printf("[%d] Before execute of %s%n", i, url);
            System.out.printf("[%d] Stats: %s%n",i, cm.getTotalStats());
            HttpGet get = new HttpGet(url);
            Future<HttpResponse> response = (client.execute(get, null));
            response.get(); //wait for the response
            System.out.printf("[%d] After execute of %s%n",i , url);
            System.out.printf("[%d] Stats: %s%n",i, cm.getTotalStats());
        }
    }
    
    @Test
    public void poolAsyncRequestsClientCert() throws Exception {
        SSLContext sslContext = sslContext();
        DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
        SchemeIOSessionStrategy reuseStrategy = new SSLIOSessionStrategy(sslContext, null, null, hostnameVerifier);

        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm =
                new MyPoolingNHttpClientConnectionManager(ioReactor,
                        RegistryBuilder.<SchemeIOSessionStrategy>create().register("http", NoopIOSessionStrategy.INSTANCE).register("https", reuseStrategy).build());
        CloseableHttpAsyncClient client =
                HttpAsyncClients.custom().setSSLContext(sslContext).setConnectionManager(cm).build();
        client.start();

        String[] toGet = {
                "https://www.google.com/",
                "https://slsh.iki.fi/client-certificate/",
                "https://slsh.iki.fi/client-certificate/protected/",
                "https://www.yahoo.com/"
        };

        for (int i = 0; i < 15; i++)
        {
        //int i = 0;
            String url = toGet[i%(toGet.length)];
            System.out.printf("[%d] %s before execute of %s%n", i, cm.getTotalStats(), url);
            HttpGet get = new HttpGet(url);
            Future<HttpResponse> response = (client.execute(get, null));
            System.out.printf("[%d] %s before waiting for execute of %s%n", i, cm.getTotalStats(), url);
            response.get(); //wait for the response
            System.out.printf("[%d] %s after execute of %s%n", i, cm.getTotalStats(), url);
        }
    }
}
