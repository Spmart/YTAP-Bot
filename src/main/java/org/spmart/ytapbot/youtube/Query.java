package org.spmart.ytapbot.youtube;

import java.util.HashMap;
import java.util.Map;

public class Query {
    private String youTubeUrl;
    private Map<String, String> options;

    /**
     * Builds a query to YouTube, using youtube-dl.
     * @param youTubeUrl Valid URL to YouTube video. Should be https://www.youtube.com/watch?v=xxx or https://youtu.be/xxx
     */
    public Query(String youTubeUrl) {
        this.youTubeUrl = youTubeUrl;
        options = new HashMap<>();
    }

    public String getYouTubeUrl() {
        return youTubeUrl;
    }

    public void setYouTubeUrl(String youTubeUrl) {
        this.youTubeUrl = youTubeUrl;
    }

    /**
     * Sets a pair of option-value. Example: -f bestaudio.
     * @param option Key in format -f or --foo
     * @param value Value, can be a string. Format depends from option itself.
     */
    public void setOption(String option, String value) {
        options.put(option, value);
    }

    /**
     * Sets a single option.
     * @param option It's can be a key like -f or --foo.
     */
    public void setOption(String option) {
        setOption(option, "");
    }

    /**
     * @return Ready to execute bash command for YouTube query.
     */
    @Override
    public String toString() {
        final String YOUTUBE_DL_BIN = "/usr/local/bin/youtube-dl";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(YOUTUBE_DL_BIN);
        stringBuilder.append(" ");

        for (String key : options.keySet()) {
            String value = options.get(key);
            stringBuilder.append(String.format("%s ", key));
            if (!value.isEmpty()) { // if value is not empty, then wrap it with '' and append
                stringBuilder.append(String.format("'%s' ", value)); // wrap args like '~/Downloads/123456.%(ext)s'
            }
        }
        stringBuilder.append("'").append(youTubeUrl).append("'");

        /*
        in result we can get something like that:
        youtube-dl -f 'bestaudio[ext=m4a]' -o '~/Downloads/123456.%(ext)s' 'http://youtu.be/hTvJoYnpeRQ'
         */

        return stringBuilder.toString();
    }
}
