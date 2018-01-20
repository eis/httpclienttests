package fi.eis.httptests.fileupload;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class FileUploadTest {

    public CloseableHttpResponse fileUpload(
            String url, String formFileFieldName, String fileName) throws IOException, URISyntaxException {
        File file = new File(this.getClass().getResource(fileName).toURI());

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(formFileFieldName, file, ContentType.APPLICATION_OCTET_STREAM, fileName);

        HttpEntity multipart = builder.build();

        httpPost.setEntity(multipart);

        return client.execute(httpPost);
    }

    @Test
    public void fileUpload() throws Exception {
        CloseableHttpResponse response = fileUpload(
                "https://slsh.iki.fi/files",  "tiedosto", "clientkeystore.jks");
        System.out.println(response.getStatusLine());
        System.out.println("");
        Arrays.stream(response.getAllHeaders()).forEach(System.out::println);
    }
}
