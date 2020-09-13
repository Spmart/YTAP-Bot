package org.spmart.ytapbot.youtube;

public class AudioInfo {
    private String title;
    private int duration;
    private String path;
    private boolean isAvailable;

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
        this(title, duration, path, false);
    }
    /**
     * Info about audio that received from youtube-dl.
     * @param title Audio title.
     * @param duration Audio duration in seconds.
     * @param path Path to audio file. Could be relative or absolute.
     * @param isAvailable Is available to download. "False" by default.
     */
    public AudioInfo(String title, int duration, String path, boolean isAvailable) {
        this.title = title;
        this.duration = duration;
        this.path = path;
        this.isAvailable = isAvailable;
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

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailability(boolean available) {
        this.isAvailable = available;
    }
}