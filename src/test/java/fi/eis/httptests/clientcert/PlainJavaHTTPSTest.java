package fi.eis.httptests.clientcert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;

public class PlainJavaHTTPSTest {

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("javax.net.ssl.keyStore",getAbsPath("clientkeystore-samepass.jks"));
        System.setProperty("javax.net.ssl.keyStorePassword", "keystorepass");
    }

    static String getAbsPath(String fileName) throws Exception {
        return new File(PlainJavaHTTPSTest.class.getResource(fileName).toURI()).getAbsolutePath();
    }
    @Test
    public void testPlainJavaHTTPS() throws Exception {
        String httpsURL = "https://slsh.iki.fi/client-certificate/protected/";
        URL myUrl = new URL(httpsURL);
        HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
        try (InputStream is = conn.getInputStream()) {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                System.out.println(inputLine);
            }
        }
    }
}
