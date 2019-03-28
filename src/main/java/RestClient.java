import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class RestClient {

    private static String BASIC_AUTHZ_HEADER = "Basic %s";

    public static void main(String[] args) throws UnirestException, InterruptedException {

        // set the trust store
        System.setProperty("javax.net.ssl.trustStore", RestClient.class.getResource("client-truststore.jks").getPath());
        System.setProperty("javax.net.ssl.trustStorePassword","wso2carbon");

        // Application Configuration Parameters
        String clientID = "55ex_QhnZRFC98DeDtMegvcL7nca";
        String clientSecret = "nBx2fZ8PIsJuGdvzEyetcIOHfvca";
        String username = "admin";
        String password = "admin";


        for (int i = 0; i <100; i++ ) {
            testPatch(clientID, clientSecret, username, password);
        }
    }

    private static void testPatch(String clientID,
                                  String clientSecret,
                                  String username,
                                  String password) throws UnirestException, InterruptedException {
        System.out.println("----");
        String basicAuthzHeader = Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes());
        String secToken = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        System.out.println("First Authz Code Flow");
        String code = getAuthzCode(clientID, secToken);
        System.out.println("First Token Request");
        String accessTokenFirst = getAccessToken(basicAuthzHeader, code);
        System.out.println("\nRe-use authorization code request");
        String accessTokenSecond = getAccessToken(basicAuthzHeader, code);
//        Thread.sleep(5);

        System.out.println("\nSecond authz code request");
        String newCode = getAuthzCode(clientID, secToken);
        System.out.println("Second token request");
        String retryFlowToken = getAccessToken(basicAuthzHeader, newCode);

        assert accessTokenFirst != null : "Access Token Request Failed";
        assert accessTokenSecond == null : "Access Token Request Failed";
        assert retryFlowToken != null: "Access Token Request with new authorization code Failed";
        assert !retryFlowToken.equals(accessTokenFirst) : "Revoked Access Token returned for new authz code";
        System.out.println("----");
    }

    private static String getAccessToken(String basicAuthzHeader, String code) throws UnirestException {

        System.out.println("code="+code);
        String tokenEndpoint = "https://localhost:9443/oauth2/token";
//
        // Make the https token request
        HttpResponse<JsonNode> response = Unirest.post(tokenEndpoint)
                .header("authorization", String.format(BASIC_AUTHZ_HEADER, basicAuthzHeader))
                .header("content-type", "application/x-www-form-urlencoded")
                .body("grant_type=authorization_code&&redirect_uri=https://localhost/callback&code="+code)
                .asJson();

        String accessToken = null;
        if (response.getBody().getObject().has("access_token")) {
            accessToken = response.getBody().getObject().get("access_token").toString();
        }
        System.out.println(response.getBody().toString());
        System.out.println("token=" + accessToken);
        return accessToken;
    }

    private static String getAuthzCode(String clientID, String secToken) throws UnirestException {
        String authorizeEndpoint = "https://localhost:9443/oauth2/authorize?response_type=code&scope=openid%20profile&redirect_uri=https://localhost/callback&client_id="+clientID+"&sectoken="+secToken;
        System.out.println(authorizeEndpoint);

        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .disableRedirectHandling()
                .build());
        HttpResponse<String> httpResponseHttpResponse = Unirest.get(authorizeEndpoint).asString();
        String location = httpResponseHttpResponse.getHeaders().getFirst("Location");
        System.out.println(location);

        return location.replace("https://localhost/callback?code=", "");
    }
}
