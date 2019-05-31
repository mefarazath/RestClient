import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.util.Pair;

import java.util.Base64;
import java.util.UUID;

public class TestJwtTokenValidation {

    public static void main(String[] args) throws UnirestException {

        System.setProperty("javax.net.ssl.trustStore", "/Users/farasath/IS/RestClient/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","wso2carbon");

        // register an app
        Pair app = createApp();
        String clientId = (String) app.getKey();
        String clientSecret = (String) app.getValue();

        System.out.println(clientId);
        System.out.println(clientSecret);

        System.out.println("----");
        String token = getToken(clientId, clientSecret);
        System.out.println(token);
        System.out.println("----");


        doUserInfoCall(token);

        // validate the JWT token

    }


    private static Pair createApp() throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/api/identity/oauth2/dcr/v1.0/register")
                .header("Authorization", "Basic: YWRtaW46YWRtaW4=")
                .header("Content-Type", "application/json")
                .body("{\"redirect_uris\": [\"http://localhost:8080/playground2/oauth2client\"],\"client_name\": \"" + UUID.randomUUID().toString() +"\",\"grant_types\": [\"password\", \"authorization_code\"]}")
//                .body("{\"token_type_extension\":\"JWT\",\"redirect_uris\": [\"http://localhost:8080/playground2/oauth2client\"],\"client_name\": \"" + UUID.randomUUID().toString() +"\",\"grant_types\": [\"password\", \"authorization_code\"]}")
                .asJson();

        System.out.println(response.getStatus());
        System.out.println(response.getBody());
        return new Pair<>(response.getBody().getObject().get("client_id"), response.getBody().getObject().get("client_secret"));
    }


    private static String getToken(String clientId, String clientSecret) throws UnirestException {

        String basicAuthHeader = String.format("Basic %s",
                Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/oauth2/token")
                .header("Authorization", basicAuthHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=password&username=admin&password=admin&scope=openid")
                .asJson();

        return (String) response.getBody().getObject().get("access_token");
    }

    private static void doUserInfoCall(String token) throws UnirestException {
        HttpResponse<String> response = Unirest.get("https://localhost:9443/oauth2/userinfo?schema=openid")
                .header("Authorization", "Bearer " + token)
                .header("cache-control", "no-cache")
                .header("Postman-Token", "c6f3189c-7c68-48cf-ba35-dc6144eb8ff9")
                .asString();

        System.out.println("--- USER INFO ---");
        System.out.println(response.getStatus());
        System.out.println(response.getBody());
        System.out.println("--- USER INFO ---");
        assert response.getStatus() == 200;
    }
 }
