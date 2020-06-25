package org.spmart.ytapbot.util;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlValidator {
    private static final String youTubeExp = "http(?:s?):\\/\\/(?:www\\.)?youtu(?:be\\.com\\/watch\\?v=|\\.be\\/)([\\w\\-\\_]*)(&(amp;)?[\\w\\?\u200C\u200B=]*)?";

    public boolean isUrl(String url) {
        try {
            URL u = new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public boolean isValidYouTubeVideoUrl(String url) {
        return url.matches(youTubeExp);
    }
}
