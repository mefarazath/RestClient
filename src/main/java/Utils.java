import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.util.Pair;

import java.util.Base64;
import java.util.UUID;

public class Utils {

    public static Pair createApp() throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/api/identity/oauth2/dcr/v1.1/register")
                .header("Authorization", "Basic: YWRtaW46YWRtaW4=")
                .header("Content-Type", "application/json")
                .body("{\"redirect_uris\": [\"https://localhost/callback\"],\"client_name\": \"" + UUID.randomUUID().toString() +"\",\"grant_types\": [\"password\", \"authorization_code\"]}").asJson();

        printResponseDetails(response);
        return new Pair<>(response.getBody().getObject().get("client_id"), response.getBody().getObject().get("client_secret"));
    }

    private static void printResponseDetails(HttpResponse<JsonNode> response) {
        System.out.println(response.getStatus());
        System.out.println(response.getBody());
    }


    public static String getToken(String clientId, String clientSecret) throws UnirestException {

        String basicAuthHeader = String.format("Basic %s",
                Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/oauth2/token")
                .header("Authorization", basicAuthHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=password&username=admin&password=admin&scope=openid")
                .asJson();

        printResponseDetails(response);
        return (String) response.getBody().getObject().get("access_token");
    }

    public static Pair<String, String> getTokenAndIdToken(String clientId, String clientSecret) throws UnirestException {

        String basicAuthHeader = String.format("Basic %s",
                Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/oauth2/token")
                .header("Authorization", basicAuthHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=password&username=admin&password=admin&scope=openid")
                .asJson();

        printResponseDetails(response);
        String access_token = (String) response.getBody().getObject().get("access_token");
        String idToken = (String) response.getBody().getObject().get("id_token");

        return new Pair<>(access_token, idToken);
    }

    public static void doUserInfoCall(String token) throws UnirestException {
        HttpResponse<String> response = Unirest.get("https://localhost:9443/oauth2/userinfo?schema=openid")
                .header("Authorization", "Bearer " + token)
                .asString();

        System.out.println("--- USER INFO ---");
        System.out.println(response.getStatus());
        System.out.println(response.getBody());
        System.out.println("--- USER INFO ---");
        assert response.getStatus() == 200;
    }


}
