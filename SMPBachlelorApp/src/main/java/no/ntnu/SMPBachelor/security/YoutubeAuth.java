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


    private static final String EXTERNAL_JSON_DIRECTORY = Paths.get("").toAbsolutePath().getParent() + File.separator +  "Data" + File.separator + "Json";;
    private static final String CLIENT_SECRETS_FILENAME = "client_secrets.json";
    private static final Path clientSecretsFilePath = Paths.get(EXTERNAL_JSON_DIRECTORY + CLIENT_SECRETS_FILENAME);
    private static final String APPLICATION_NAME = "Your Application Name";
    private static final String TOKENS_DIRECTORY_PATH = Paths.get("").toAbsolutePath().getParent() + File.separator +  "Data" + File.separator + "tokens";;
    private static final String TOKEN_SECRETS_FILENAME = "token.json";

    private static final Path tokenSecretsFilePath = Paths.get(TOKENS_DIRECTORY_PATH + TOKEN_SECRETS_FILENAME);
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
                .setDataStoreFactory(dataStoreFactory) // Set the data store factory
                .build();
    }

    public static boolean secretExists() {
        return exists(Paths.get(String.valueOf(clientSecretsFilePath)));
    }

    public static String getAuthorizationUrl() throws IOException {

        GoogleClientSecrets googleClientSecrets = getSecrets();
        GoogleAuthorizationCodeFlow codeFlow = buildGoogleFlow(googleClientSecrets);
        return codeFlow.newAuthorizationUrl()
                .setRedirectUri(googleClientSecrets.getDetails().getRedirectUris()
                        .get(0)).build();
    }

    public static Credential loadCredentials() throws IOException {
        java.io.File file = new java.io.File(TOKENS_DIRECTORY_PATH, "token.json");
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            GoogleAuthorizationCodeFlow flow = buildGoogleFlow(getSecrets());
            return flow.loadCredential("user_id");
        }
        return null;
    }


    public static void saveCredentials(Credential credential) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(String.valueOf(tokenSecretsFilePath)))) {
            outputStream.writeObject(credential.getAccessToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public Credential authorize(String code) throws IOException {
        GoogleClientSecrets googleClientSecrets = getSecrets();
        GoogleAuthorizationCodeFlow codeFlow = buildGoogleFlow(googleClientSecrets);

        if (code != null) {
            // Exchange authorization code for credentials
            TokenResponse tokenResponse = codeFlow.newTokenRequest(code)
                    .setRedirectUri(googleClientSecrets.getDetails().getRedirectUris().get(0))
                    .execute();

            // Store the credential
            Credential credential = codeFlow.createAndStoreCredential(tokenResponse, "user_id");
            return credential;
        } else {
            // Load stored credential
            return loadCredentials();
        }
    }





    public static YouTube getService() throws IOException {
        Credential credential =  YoutubeAuth.loadCredentials();
        if (credential == null) {
            throw new IllegalStateException("Authorization required.");
        }

        if (credential.getExpiresInSeconds() <= 0) {
            System.out.println("HERE EXPIRE RUNS");
            credential.refreshToken();
            YoutubeAuth.saveCredentials(credential);
        }
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static YouTubeAnalytics getAnalyticsService() throws IOException {

            // Load credentials
            Credential credential = YoutubeAuth.loadCredentials();
        if (credential == null) {
            throw new IllegalStateException("Authorization required.");
        }




            if (credential.getExpiresInSeconds() <= 0) {
                credential.refreshToken();
                YoutubeAuth.saveCredentials(credential);
            }

            // Build and return the YouTubeAnalytics service
            return new YouTubeAnalytics.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }



