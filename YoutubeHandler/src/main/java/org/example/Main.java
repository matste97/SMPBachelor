package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class Main {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        YoutubeAnalyticsFetcher YoutubeVideoIdFetcher = new YoutubeAnalyticsFetcher();
//          YoutubeVideoIdFetcher.getChannelGenderAgeDemographic();
//          System.out.println(YoutubeVideoIdFetcher.getLatestVideoIds());
//          System.out.println(YoutubeVideoIdFetcher.getVideoTotalViews("J5ZvB9Yo95o"));
//          YoutubeVideoIdFetcher.getVideoAgeAndGenderData("J5ZvB9Yo95o");
//          System.out.println(YoutubeVideoIdFetcher.getVideoTitle("KB-nyB9LGOw"));
        YoutubeVideoIdFetcher.saveLatestVideosInfoToJSON();
    }

}
