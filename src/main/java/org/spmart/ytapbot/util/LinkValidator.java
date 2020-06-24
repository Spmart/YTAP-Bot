package org.spmart.ytapbot.util;

public class LinkValidator {
    private static final String youTubeExp = "http(?:s?):\\/\\/(?:www\\.)?youtu(?:be\\.com\\/watch\\?v=|\\.be\\/)([\\w\\-\\_]*)(&(amp;)?[\\w\\?\u200C\u200B=]*)?";

    public boolean isYouTubeLink(String link) {
        return link.matches(youTubeExp);
    }
}
