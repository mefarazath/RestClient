import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.util.Pair;
import org.json.JSONObject;

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

//        System.out.println(clientId);
//        System.out.println(clientSecret);
//
//        System.out.println("----");
//        String token = getToken(clientId, clientSecret);
//        System.out.println(token);
//        System.out.println("----");
//
//        doUserInfoCall(token);
//        doTokenRevoke(clientId, clientSecret, token);
//        doUserInfoCall(token);


        System.out.println("---- *** TEST REFRESH FLOW *** ---");
        JSONObject response = getTokenResponse(clientId, clientSecret);
        String initialAccessToken = getAccessToken(response);
        String refreshToken = getRefreshToken(response);

        doUserInfoCall(initialAccessToken);

        JSONObject refreshResponse = doTokenRefresh(clientId, clientSecret, refreshToken);
        String newAccessToken = getAccessToken(refreshResponse);

        doUserInfoCall(initialAccessToken);
        doUserInfoCall(newAccessToken);

        doTokenRevoke(clientId, clientSecret, newAccessToken);
        doUserInfoCall(newAccessToken);
        System.out.println("---- TEST REFRESH FLOW ---");



//        System.out.println("---- *** TEST REFRESH FLOW *** ---");
//        JSONObject response = getTokenResponse(clientId, clientSecret);
//        String initialAccessToken = getAccessToken(response);
//        String initialRefreshToken = getRefreshToken(response);
//
//        doUserInfoCall(initialAccessToken);
//
////        JSONObject refreshResponse = doTokenRefresh(clientId, clientSecret, initialRefreshToken);
////        String newAccessToken = getAccessToken(refreshResponse);
////
////        doUserInfoCall(initialAccessToken);
////        doUserInfoCall(newAccessToken);
//
//        doTokenRevoke(clientId, clientSecret, initialRefreshToken);
//
//        JSONObject secondTokenResponse = getTokenResponse(clientId, clientSecret);
//
//        String accessTokenSecond = getAccessToken(secondTokenResponse);
//        String refreshTokenSecond = getRefreshToken(secondTokenResponse);
//
//        System.out.println("Refresh Tokens");
//        System.out.println(initialRefreshToken);
//        System.out.println(refreshTokenSecond);
//
//
//        System.out.println("Access Tokens");
//        System.out.println(initialAccessToken);
//        System.out.println(accessTokenSecond);
//
//        System.out.println("---- TEST REFRESH FLOW ---");


    }

    private static String getRefreshToken(JSONObject response) {
        return response.getString("refresh_token");
    }

    private static String getAccessToken(JSONObject response) {
        return response.getString("access_token");
    }

    private static JSONObject doTokenRefresh(String clientId,
                                             String clientSecret,
                                             String refreshToken) throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/oauth2/token")
                .header("Authorization", getBasicHeader(clientId, clientSecret))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=refresh_token&scope=openid&refresh_token=" + refreshToken)
                .asJson();

        System.out.println(response.getStatus());
        System.out.println(response.getBody().getObject().toString());

        return response.getBody().getObject();
    }


    private static Pair createApp() throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/api/identity/oauth2/dcr/v1.0/register")
                .header("Authorization", "Basic: YWRtaW46YWRtaW4=")
                .header("Content-Type", "application/json")
                .body("{\"redirect_uris\": [\"http://localhost:8080/playground2/oauth2client\"],\"client_name\": \"" + UUID.randomUUID().toString() +"\",\"grant_types\": [\"password\",\"refresh_token\", \"authorization_code\"]}")
//                .body("{\"token_type_extension\":\"JWT\",\"redirect_uris\": [\"http://localhost:8080/playground2/oauth2client\"],\"client_name\": \"" + UUID.randomUUID().toString() +"\",\"grant_types\": [\"password\", \"authorization_code\"]}")
                .asJson();

        System.out.println(response.getStatus());
        System.out.println(response.getBody());
        return new Pair<>(response.getBody().getObject().get("client_id"), response.getBody().getObject().get("client_secret"));
    }


    private static String getToken(String clientId, String clientSecret) throws UnirestException {

        String basicAuthHeader = getBasicHeader(clientId, clientSecret);

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/oauth2/token")
                .header("Authorization", basicAuthHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=password&username=admin&password=admin&scope=openid")
                .asJson();

        return (String) response.getBody().getObject().get("access_token");
    }

    private static JSONObject getTokenResponse(String clientId, String clientSecret) throws UnirestException {

        String basicAuthHeader = getBasicHeader(clientId, clientSecret);

        HttpResponse<JsonNode> response = Unirest.post("https://localhost:9443/oauth2/token")
                .header("Authorization", basicAuthHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=password&username=admin&password=admin&scope=openid")
                .asJson();

        return response.getBody().getObject();
    }

    private static String getBasicHeader(String clientId, String clientSecret) {
        return String.format("Basic %s",
                Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
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

    private static void doTokenRevoke(String clientId, String clientSecret, String token) throws UnirestException {


        HttpResponse<String> response = Unirest.post("https://localhost:9443/oauth2/revoke")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", getBasicHeader(clientId, clientSecret))
                .body("token_type_hint=access_token&token=" + token)
                .asString();

        System.out.println("--- TOKEN REVOKE  ---");
        System.out.println(response.getStatus());
        System.out.println("Revoked_Token: " + response.getHeaders().getFirst("RevokedAccessToken"));
        System.out.println(response.getBody());
        System.out.println("--- TOKEN REVOKE  ---");
        assert response.getStatus() == 200;
    }
 }
