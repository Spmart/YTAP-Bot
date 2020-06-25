package org.spmart.ytapbot.youtube;

import java.util.HashMap;
import java.util.Map;

public class Query {
    private String youTubeLink;
    private Map<String, String> options;

    public Query(String youTubeLink) {
        this.youTubeLink = youTubeLink;
        options = new HashMap<>();
    }

    public String getYouTubeLink() {
        return youTubeLink;
    }

    public void setYouTubeLink(String youTubeLink) {
        this.youTubeLink = youTubeLink;
    }

    public void setOption(String option, String value) {
        options.put(option, value);
    }

    public void setOption(String option) {
        setOption(option, "");
    }

    @Override
    public String toString() {
        //youtube-dl -f 'bestaudio[ext=m4a]' -o '~/Downloads/123456.%(ext)s' 'http://youtu.be/hTvJoYnpeRQ'
        final String SHELL_BIN = "/bin/bash -c";
        final String YOUTUBE_DL_BIN = "/usr/local/bin/youtube-dl";

        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append(SHELL_BIN);
        //stringBuilder.append(" ");
        stringBuilder.append(YOUTUBE_DL_BIN);
        stringBuilder.append(" ");

        for (String key : options.keySet()) {
            String value = options.get(key);
            stringBuilder.append(String.format("%s ", key));
            if (!value.isEmpty()) { // if value is not empty, then wrap it with '' and append
                stringBuilder.append(String.format("'%s' ", value)); // wrap args like '~/Downloads/123456.%(ext)s'
            }
        }
        stringBuilder.append("'").append(youTubeLink).append("'");

        return stringBuilder.toString();
    }
}
