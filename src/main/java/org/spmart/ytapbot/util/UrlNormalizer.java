package org.spmart.ytapbot.util;

public class UrlNormalizer {
    /**
     * @param url Valid URL.
     * @return Url without arguments after "&" char.
     */
    public String deleteArgsFromYouTubeUrl(String url) {
        return url.split("&")[0];
    }
}
