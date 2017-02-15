import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.Base64;

public class RestClient {

    private static String BASIC_AUTHZ_HEADER = "Basic %s";

    public static void main(String[] args) throws UnirestException {

        // set the trust store
        System.setProperty("javax.net.ssl.trustStore", RestClient.class.getResource("mytruststore.jks").getPath());
        System.setProperty("javax.net.ssl.trustStorePassword","wso2carbon");

        // Application Configuration Parameters
        String clientID = "uJR3hrQ52Mfgr0TNEbGMSLRnYYsa";
        String clientSecret = "L2HNO1sG6Gjy9PRzH9EbnvW3VQAa";
        String basicAuthzHeader = Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes());

        String tokenEndpoint = "https://idp.wso2.com:9443/oauth2/token";

        // Make the https token request
        HttpResponse<String> response = Unirest.post(tokenEndpoint)
                .header("authorization", String.format(BASIC_AUTHZ_HEADER, basicAuthzHeader))
                .header("content-type", "application/x-www-form-urlencoded")
                .body("grant_type=password&username=admin&password=admin&scope=1234")
                .asString();

        System.out.println(response.getBody());
    }
}
