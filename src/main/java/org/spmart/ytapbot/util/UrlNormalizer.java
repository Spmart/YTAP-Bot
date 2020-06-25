package org.spmart.ytapbot.util;

public class UrlNormalizer {
    public String deleteArgsFromYouTubeUrl(String url) {
        return url.split("&")[0];
    }
}
