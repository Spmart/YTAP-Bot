package org.spmart.ytapbot;

import org.spmart.ytapbot.util.UrlNormalizer;
import org.spmart.ytapbot.util.UrlValidator;
import org.spmart.ytapbot.util.Logger;

import org.spmart.ytapbot.youtube.AudioInfo;
import org.spmart.ytapbot.youtube.AudioSlicer;
import org.spmart.ytapbot.youtube.Downloader;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private static final String PATH_TO_TOKEN = "token.txt";  // Token from @BotFather
    private static final String PATH_TO_NAME = "name.txt";  // Bot username, for ex: ytap_bot
    private static final int MAX_AUDIO_DURATION = 18000;  // 5 hours in seconds
    private static final int MAX_AUDIO_CHUNK_DURATION = 3000; // 50 minutes in seconds

    private static final String START_MESSAGE = "Hello friend! Send me a YouTube link! I'll return you an audio from it.";

    /**
     * If bot receiving a message, starts new thread to process it.
     * @param update Update from Telegram Bot API.
     */
    public void onUpdateReceived(Update update) {
        new Thread(() -> processMessage(update)).start();
    }

    public String getBotUsername() {
        return getTextFromFile(PATH_TO_NAME);
    }

    public String getBotToken() {
        return getTextFromFile(PATH_TO_TOKEN);
    }

    /**
     * Process the message that user sent to the bot
     * @param update Update from Telegram Bot API.
     */
    private void processMessage(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            Integer messageId = update.getMessage().getMessageId();  // is unique, incremental
            String inMessageText = update.getMessage().getText();

            UrlValidator validator = new UrlValidator();
            UrlNormalizer normalizer = new UrlNormalizer();

            // logic: try to cut args only in url, we shouldn't touch plain text
            if (validator.isUrl(inMessageText) &&
                    validator.isValidYouTubeVideoUrl(normalizer.deleteArgsFromYouTubeUrl(inMessageText))) {

                String normalizedUrl = normalizer.deleteArgsFromYouTubeUrl(inMessageText);
                String audioPath = String.format("./audio/%s.m4a", messageId);
                Downloader downloader = new Downloader(normalizedUrl, audioPath);
                AudioInfo info = downloader.getAudioInfo();

                if (!info.isAvailable() && info.getDuration() <= 0) {  // Streams are not available for us, but sometimes stream contains info with zero in video duration
                    send(chatId, "Seems like you send a link to stream that's not finished yet. " +
                            "If so, try again later when the broadcast is over.");
                } else if (!info.isAvailable()) {
                    send(chatId, "Audio is not available. Possible reasons:\n " +
                            "1. Broken link.\n " +
                            "2. YouTube marked this video as unacceptable for some users.\n " +
                            "3. Private video.\n " +
                            "4. This is a link to stream that's not finished yet. If so, try again later.");  // Some streams are not provide any info at all
                } else if (info.getDuration() > MAX_AUDIO_DURATION) {
                    send(chatId, "It's too long video! My maximum is 5 hours.");
                } else if (info.getDuration() > MAX_AUDIO_CHUNK_DURATION) {
                    send(chatId, "Downloading and slicing for parts...");
                    downloader.getAudio();
                    AudioSlicer slicer = new AudioSlicer(info);
                    List<AudioInfo> partsInfo = slicer.getAudioParts(MAX_AUDIO_CHUNK_DURATION);
                    sendAll(chatId, partsInfo);
                    deleteAudio(info);  // cleanup
                    deleteAudio(partsInfo);
                } else {
                    send(chatId, "Downloading...");
                    downloader.getAudio();
                    send(chatId, info);
                    deleteAudio(info);  // cleanup
                }

            } else if (inMessageText.equals("/start")) {
                send(chatId, START_MESSAGE);
            } else {
                send(chatId, "It's NOT a YouTube link! Try again.");
            }
        }
    }

    private String getTextFromFile(String path) {
        String content = "";
        try {
            content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.INSTANCE.write(String.format("File not found! %s", e.getMessage()));
        }
        return content.trim();  // Trim string for spaces or new lines
    }

    /**
     * Sends a text message to user.
     * @param chatId Unique ID that identifies the chat to the user.
     * @param text Text message.
     */
    private void send(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId).setText(text);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            Logger.INSTANCE.write(String.format("Error! Can't send a message! %s", e.getMessage()));
        }
    }

    /**
     * Sends a message with an audio record.
     * @param chatId Unique ID that identifies the chat to the user.
     * @param info AudioInfo object that contains title, duration, caption and audio path.
     */
    private void send(long chatId, AudioInfo info) {
        File audioFile = new File(info.getPath());
        if (audioFile.exists()) {
            SendAudio audio = new SendAudio();
            audio
                    .setTitle(info.getTitle())
                    .setDuration(info.getDuration())
                    .setCaption(info.getTitle())
                    .setChatId(chatId)
                    .setAudio(audioFile);
            try {
                execute(audio);
            } catch (TelegramApiException e) {
                Logger.INSTANCE.write(String.format("Error! Can't send an audio! %s", e.getMessage()));
                send(chatId, "Can't send an audio. May be, file is bigger than 50 MB");  // Double check. If Telegram API stops upload
            }
        } else {
            send(chatId, "Error downloading audio. This case will be reported to bot developer.");
            Logger.INSTANCE.write("Download error. Most likely, audio is removed before sending.");  // This error shouldn't appear in other cases. Streams are handled earlier. I need more data.
            Logger.INSTANCE.write("Audio: " + info.getTitle() + " " + info.getPath());
        }
    }

    /**
     * Sends all audio. Each audio in a separate message.
     * @param chatId Unique ID that identifies the chat to the user.
     * @param infos Collection with AudioInfo objects that contains title, duration, caption and audio path.
     */
    private void sendAll(long chatId, List<AudioInfo> infos) {
        for (AudioInfo info : infos) {
            send(chatId, info);
        }
    }


    /**
     * Removes audio from HDD.
     * @param info AudioInfo object that contains title, duration, caption and audio path.
     */
    private void deleteAudio(AudioInfo info) {
        Path pathToAudio = Path.of(info.getPath());
        try {
            Files.deleteIfExists(pathToAudio);
        } catch (IOException e) {
            Logger.INSTANCE.write(String.format("Delete error! File is not exist or busy. %s", e.getMessage()));
        }
    }


    /**
     * Removes all audio parts from HDD.
     * @param infos Collection with AudioInfo objects that contains title, duration, caption and audio path.
     */
    private void deleteAudio(List<AudioInfo> infos) {
        for (AudioInfo info : infos) {
            deleteAudio(info);
        }
    }
}
