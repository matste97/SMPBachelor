package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class Main {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        YoutubeAnalyticsFetcher YoutubeVideoIdFetcher = new YoutubeAnalyticsFetcher();
        YoutubeVideoIdFetcher.fetchChannelGenderAgeDemographic();
        System.out.println(YoutubeVideoIdFetcher.getAllVideoIds());

    }

}
