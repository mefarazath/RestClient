import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.util.Pair;

public class TestLogoutGracefully {

    public static void main(String[] args) throws UnirestException {

        System.setProperty("javax.net.ssl.trustStore", "/Users/farasath/IS/RestClient/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","wso2carbon");

        Pair appKeys = Utils.createApp();
        String clientId = (String) appKeys.getKey();
        String clientSecret = (String) appKeys.getValue();

        Pair<String, String> response = Utils.getTokenAndIdToken(clientId, clientSecret);
        String idToken = response.getValue();

        // dummy Login
        login(clientId);

        logoutAndRedirectToOp(idToken);
        logoutAndRedirectToCommonPage(idToken);
    }


    private static void logoutAndRedirectToOp(String idToken) throws UnirestException {

        String logoutUrl =
                "https://localhost:9443/oidc/logout?id_token_hint=%s&post_logout_redirect_uri=https://localhost/callback";
        logout(logoutUrl,idToken);
    }

    private static void logoutAndRedirectToCommonPage(String idToken) throws UnirestException {

        String logoutUrl = "https://localhost:9443/oidc/logout?id_token_hint=%s";
        logout(logoutUrl,idToken);
    }

    private static void logout(String logoutUrl, String idToken) throws UnirestException {

        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .disableRedirectHandling()
                .build());

        logoutUrl = String.format(logoutUrl, idToken);
        HttpResponse<String> response = Unirest.get(logoutUrl)
                .asString();

        System.out.println("--- Logout Attempt ---");
        System.out.println(logoutUrl);
        System.out.println(response.getStatus());
        System.out.println(response.getHeaders().getFirst("Location"));
        System.out.println("--- Logout ---");
    }

    private static void login(String clientId) throws UnirestException {


        String loginUrl =
                String.format("https://localhost:9443/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=https://localhost/callback&scope=openid", clientId);
//        HttpResponse<String> response = Unirest.get(String.format(loginUrl, clientId)).asString();
//////
//////        System.out.println("--- Login Attempt ---");
//////        System.out.println(response.getStatus());
//////        System.out.println(response.getHeaders().getFirst("Location"));
//////        System.out.println("--- Login ---");

        System.out.println(loginUrl);
    }
}
