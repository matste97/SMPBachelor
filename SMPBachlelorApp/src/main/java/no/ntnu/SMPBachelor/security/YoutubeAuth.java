package no.ntnu.SMPBachelor.security;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

import static java.nio.file.Files.exists;

public class YoutubeAuth {


    private static final String EXTERNAL_JSON_DIRECTORY = Paths.get("").toAbsolutePath().getParent() + File.separator +  "Data" + File.separator + "Json";
    private static final String CLIENT_SECRETS_FILENAME = "client_secrets.json";
    private static final Path clientSecretsFilePath = Paths.get(EXTERNAL_JSON_DIRECTORY + File.separator +  CLIENT_SECRETS_FILENAME);
    private static final String APPLICATION_NAME = "Your Application Name";
    private static final String TOKENS_DIRECTORY_PATH = Paths.get("").toAbsolutePath().getParent() + File.separator +  "Data" + File.separator + "tokens";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final Collection<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/youtube.readonly",
            "https://www.googleapis.com/auth/youtube.upload"
    );
    private static final NetHttpTransport httpTransport;
    static {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize HTTP transport", e);
        }
    }



    /**
     * Retrieves Google client secrets from a predefined resource file.
     *
     * @return GoogleClientSecrets containing the OAuth2 client configuration.
     * @throws RuntimeException if there's an error loading the client secrets.
     */
    private static GoogleClientSecrets getSecrets() throws FileNotFoundException {
        InputStream in = new FileInputStream(clientSecretsFilePath.toFile());
        try {
            return GoogleClientSecrets.load(
                    JSON_FACTORY,
                    new InputStreamReader(in)
            );
        } catch (IOException e) {
            System.out.println("Error when trying to load google client secrets...");
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds and configures an instance of GoogleAuthorizationCodeFlow.
     *
     * @param googleClientSecrets The GoogleClientSecrets to be used in the flow.
     * @return An instance of GoogleAuthorizationCodeFlow for OAuth2 flow.
     */
    private static GoogleAuthorizationCodeFlow buildGoogleFlow(
            GoogleClientSecrets googleClientSecrets
    ) throws IOException {
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));
        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                googleClientSecrets,
                SCOPES
        ).setAccessType("offline")
                .setDataStoreFactory(dataStoreFactory)
                .build();
    }

    /**
     * Checks if the client secret exists
     * @return boolean, true if exists.
     */
    public static boolean secretExists() {
        return exists(Paths.get(String.valueOf(clientSecretsFilePath)));
    }

    /**
     * Gets ands returns the authorization URL to authorize with the Google API
     * @return String, url for authorization
     */
    public static String getAuthorizationUrl() throws IOException {

        GoogleClientSecrets googleClientSecrets = getSecrets();
        GoogleAuthorizationCodeFlow codeFlow = buildGoogleFlow(googleClientSecrets);
        return codeFlow.newAuthorizationUrl()
                .setRedirectUri(googleClientSecrets.getDetails().getRedirectUris()
                        .get(0)).build();
    }
    /**
     * This method loads credentials required for authentication.
     * @return Credential generated from the token response.
     */
    public static Credential loadCredentials() throws IOException {
            GoogleAuthorizationCodeFlow flow = buildGoogleFlow(getSecrets());
            return flow.loadCredential("user_id");
    }

    /**
     * Exchanges an authorization code for a token response.
     * @param code The authorization code to exchange for a token.
     * @throws IOException if the token exchange fails.
     */

    public void authorize(String code) throws IOException {
        GoogleClientSecrets googleClientSecrets = getSecrets();
        GoogleAuthorizationCodeFlow codeFlow = buildGoogleFlow(googleClientSecrets);

        if (code != null) {
            // Exchange authorization code for credentials
            TokenResponse tokenResponse = codeFlow.newTokenRequest(code)
                    .setRedirectUri(googleClientSecrets.getDetails().getRedirectUris().get(0))
                    .execute();
            codeFlow.createAndStoreCredential(tokenResponse, "user_id");
        }
    }


    /**
     * Return YouTube service
     */

    public static YouTube getService() throws IOException {
        Credential credential =  loadCredentials();
        if (credential == null) {
            return null;
        }

        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    /**
     * Return YouTube Analytics service
     */

    public static YouTubeAnalytics getAnalyticsService() throws IOException {
            // Load credentials
            Credential credential = loadCredentials();
        if (credential == null) {
            return null;
        }
            // Build and return the YouTubeAnalytics service
            return new YouTubeAnalytics.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }



