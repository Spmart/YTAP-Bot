package org.spmart.ytapbot.youtube;

public class AudioInfo {
    private String title;
    private int duration;
    private String path;

    /**
     * Info about audio that received from youtube-dl.
     */
    public AudioInfo() {
        this("No title");
    }

    public AudioInfo(String title) {
        this(title, 0);
    }

    /**
     * Info about audio that received from youtube-dl.
     * @param title Audio title.
     * @param duration Audio duration in seconds.
     */
    public AudioInfo(String title, int duration) {
        this(title, duration, "");
    }

    /**
     * Info about audio that received from youtube-dl.
     * @param title Audio title.
     * @param duration Audio duration in seconds.
     * @param path Path to audio file. Could be relative or absolute.
     */
    public AudioInfo(String title, int duration, String path) {
        this.title = title;
        this.duration = duration;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}