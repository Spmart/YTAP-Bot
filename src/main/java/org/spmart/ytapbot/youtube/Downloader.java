package org.spmart.ytapbot.youtube;

import org.spmart.ytapbot.util.Logger;
import org.spmart.ytapbot.util.ProcessExecutor;

import java.util.List;

public class Downloader {
    private final String youTubeLink;
    private final String downloadPath;

    private final String FILE_TYPE_KEY = "-f";
    private final String OUTPUT_KEY = "-o";

    private final String ONLY_AUDIO_M4A_OPTION = "bestaudio[ext=m4a]";
    private final String GET_TITLE_OPTION = "--get-title";
    private final String GET_DURATION_OPTION = "--get-duration";

    public Downloader(String youTubeLink, String downloadPath) {
        this.youTubeLink = youTubeLink;
        this.downloadPath = downloadPath;
    }

// For download audio only I can use: youtube-dl -f 'bestaudio[ext=m4a]' -o '~/Downloads/123456.%(ext)s' 'http://youtu.be/hTvJoYnpeRQ'

    public boolean getAudio() {
        Query query = new Query(youTubeLink);
        query.setOption(FILE_TYPE_KEY, ONLY_AUDIO_M4A_OPTION);
        query.setOption(OUTPUT_KEY, downloadPath);

        String cmdLine = query.toString();

        ProcessExecutor processExecutor = new ProcessExecutor();
        List<String> processOutput = processExecutor.runProcess(cmdLine); // Actually, here downloading is starting

        Logger logger = Logger.INSTANCE;

        if (!isDownloaded(processOutput)) { // if process error code != 0
            logger.write(String.format("Download %s is failed. Youtube-dl output:", youTubeLink));
            processOutput.forEach(logger::write); // Write all youtube-dl out into a bot log
            return false;
        }
        return true;
    }

    @Deprecated
    public String getTitle() {
        Query query = new Query(youTubeLink);
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

    public AudioInfo getAudioInfo() {
        Query query = new Query(youTubeLink);
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

    private int convertDurationToSeconds(String duration) {  // TODO: Implement this
        return 0;
    }

    private boolean isDownloaded(List<String> processOutput) {
        return processOutput.get(processOutput.size() - 1).equals("0");
    }
}
