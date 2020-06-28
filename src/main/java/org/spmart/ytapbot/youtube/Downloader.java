package org.spmart.ytapbot.youtube;

import org.spmart.ytapbot.util.Logger;
import org.spmart.ytapbot.util.ProcessExecutor;

import java.util.List;

public class Downloader {
    private final String youTubeUrl;
    private final String downloadPath;

    private final String FILE_TYPE_KEY = "-f";
    private final String OUTPUT_KEY = "-o";

    private final String ONLY_AUDIO_M4A_OPTION = "bestaudio[ext=m4a]";
    private final String GET_TITLE_OPTION = "--get-title";
    private final String GET_DURATION_OPTION = "--get-duration";

    /**
     * Prepares download. Can download information about audio and audio itself.
     * @param youTubeUrl Valid URL to YouTube video. Should be https://www.youtube.com/watch?v=xxx or https://youtu.be/xxx
     * @param downloadPath Path to output file.
     */
    public Downloader(String youTubeUrl, String downloadPath) {
        this.youTubeUrl = youTubeUrl;
        this.downloadPath = downloadPath;
    }

    /**
     * Downloads an audio.
     * @return True if audio is downloaded. False if the audio download is failed
     */
    public boolean getAudio() {
        Query query = new Query(youTubeUrl);
        query.setOption(FILE_TYPE_KEY, ONLY_AUDIO_M4A_OPTION);
        query.setOption(OUTPUT_KEY, downloadPath);

        String cmdLine = query.toString();

        ProcessExecutor processExecutor = new ProcessExecutor();
        List<String> processOutput = processExecutor.runProcess(cmdLine); // Actually, here downloading is starting

        Logger logger = Logger.INSTANCE;

        if (!isDownloaded(processOutput)) { // if process error code != 0
            logger.write(String.format("Download %s is failed. Youtube-dl output:", youTubeUrl));
            processOutput.forEach(logger::write); // Write all youtube-dl out into a bot log
            return false;
        }
        return true;
    }

    @Deprecated
    public String getTitle() {
        Query query = new Query(youTubeUrl);
        query.setOption(GET_TITLE_OPTION);

        String cmdLine = query.toString();

        ProcessExecutor processExecutor = new ProcessExecutor();
        List<String> processOutput = processExecutor.runProcess(cmdLine);

        if (isDownloaded(processOutput)) {
            return processOutput.get(0);
        } else {
            return "No Title"; // In the end of the Downloader always an exit code
        }
    }

    /**
     * Sends one query to YouTube API to grab all information about audio.
     * @return AudioInfo instance that contains title and duration.
     */
    public AudioInfo getAudioInfo() {
        Query query = new Query(youTubeUrl);
        query.setOption(GET_TITLE_OPTION);
        query.setOption(GET_DURATION_OPTION);

        String cmdLine = query.toString();

        ProcessExecutor processExecutor = new ProcessExecutor();
        List<String> processOutput = processExecutor.runProcess(cmdLine);

        AudioInfo info = new AudioInfo();
        if (isDownloaded(processOutput)) {
           info.setTitle(processOutput.get(0));
           info.setPath(downloadPath);
           info.setDuration(convertDurationToSeconds(processOutput.get(1)));
        }
        return info;
    }

    /**
     * @param duration Duration in HH:MM:SS, MM:SS or SS format.
     * @return Duration in seconds.
     */
    private int convertDurationToSeconds(String duration) {
        String[] durationHms = duration.split(":"); //Hours, minutes, seconds
        return switch (durationHms.length) {
            case 1 -> Integer.parseInt(durationHms[0]);
            case 2 -> Integer.parseInt(durationHms[0]) * 60 + Integer.parseInt(durationHms[1]);
            case 3 -> Integer.parseInt(durationHms[0]) * 3600
                    + Integer.parseInt(durationHms[1]) * 60 + Integer.parseInt(durationHms[2]);
            default -> 0;
        };
    }

    private boolean isDownloaded(List<String> processOutput) {
        return processOutput.get(processOutput.size() - 1).equals("0");
    }
}
