package org.spmart.ytapbot.youtube;

import org.spmart.ytapbot.util.ProcessExecutor;

import java.util.ArrayList;
import java.util.List;

public class AudioSlicer {
    private final AudioInfo originalAudio;

    private final String SLICE_CMD = "ffmpeg -hide_banner -i '%s' -reset_timestamps 1 -f segment -segment_time %d -c copy '%s'";  // -reset_timestamps is necessary
    private final String PART_NAME_FORMAT = "_part%01d.m4a";  // 1234_part0.m4a, 1234_part1.m4a ... etc

    /**
     * Slices an audio on parts (fragments) specified length.
     * @param audioInfo AudioInfo object that contains title, duration, caption and audio path.
     */
    public AudioSlicer(AudioInfo audioInfo) {
        originalAudio = audioInfo;
    }

    /**
     * Returns a list of AudioInfo's. Every item contains info about audio fragment.
     * @param fragmentDuration Specified audio fragment length. Last fragment length calculates automatically.
     * @return A list of AudioInfo's.
     */
    public List<AudioInfo> getAudioParts(int fragmentDuration) {
        final String OPENING_FOR_WRITING_EXP = ".*Opening '.*' for writing$";

        ProcessExecutor processExecutor = new ProcessExecutor();
        String originalAudioPath = originalAudio.getPath();
        List<String> processOutput = processExecutor.runProcess(String.format(SLICE_CMD, originalAudioPath, fragmentDuration, cutExtension(originalAudioPath)));

        List<String> fragmentPaths = new ArrayList<>();
        for (String str : processOutput) {
            if (str.matches(OPENING_FOR_WRITING_EXP)) {  // parsing ffmpeg out strings like: "[segment @ 0x7ff3ca808200] Opening '/Users/kai/Downloads/12341.m4a' for writing"
                fragmentPaths.add(substringBetween(str, "'", "'"));  // grab path to fragment
            }
        }
        
        int lastFragmentDuration = originalAudio.getDuration() - ((fragmentPaths.size() - 1) * fragmentDuration);  // calc n-1 fragments length and find duration of last audio
        List<AudioInfo> fragments = new ArrayList<>();
        for (int partNumber = 0; partNumber < fragmentPaths.size(); partNumber++) {
            String fragmentName = originalAudio.getTitle() + String.format(" Part %d", partNumber);
            if (partNumber == fragmentPaths.size() - 1) {
                fragments.add(new AudioInfo(fragmentName, lastFragmentDuration, fragmentPaths.get(partNumber), true));  // if last path in list, we should set calculated duration
            } else {
                fragments.add(new AudioInfo(fragmentName, fragmentDuration, fragmentPaths.get(partNumber), true));  // if not last element, use default fragment duration
            }
        }
        
        return fragments;
    }

    private String cutExtension(String audioPath) {
        return audioPath.substring(0, audioPath.length() - 4) + PART_NAME_FORMAT;
    }

    private String substringBetween(String str, String open, String close) {  // stolen, it's shame (a little)
        final int INDEX_NOT_FOUND = -1;

        final int start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            final int end = str.indexOf(close, start + open.length());
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + open.length(), end);
            }
        }
        return "";
    }
}

