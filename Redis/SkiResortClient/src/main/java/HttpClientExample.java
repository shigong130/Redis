import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

    public class HttpClientExample {

        private final CloseableHttpClient httpClient = HttpClients.createDefault();
        private void close() throws IOException {
            httpClient.close();
        }

        public String sendGet(String url) throws Exception {

            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {

                // Get HttpResponse Status
                //System.out.println(response.getStatusLine().toString());

                HttpEntity entity = response.getEntity();
                Header headers = entity.getContentType();
                //System.out.println(headers);

                if (entity != null) {
                    // return it as a String
                    String result = EntityUtils.toString(entity);
                    return result;
                }

            }

            throw new Exception("failed");
        }

}
