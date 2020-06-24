package org.spmart.ytapbot.youtube;

public class VideoInfo {
    private String title;
    private int duration;

    public VideoInfo() {
        this("No title", 0);
    }

    public VideoInfo(String title, int duration) {
        this.title = title;
        this.duration = duration;
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