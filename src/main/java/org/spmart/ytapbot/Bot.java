package org.spmart.ytapbot;

import org.spmart.ytapbot.util.UrlNormalizer;
import org.spmart.ytapbot.util.UrlValidator;
import org.spmart.ytapbot.util.Logger;

import org.spmart.ytapbot.youtube.AudioInfo;
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
import java.nio.file.Paths;

public class Bot extends TelegramLongPollingBot {

    private static final String PATH_TO_TOKEN = "token.txt";  // Token from @BotFather
    private static final String PATH_TO_NAME = "name.txt";  // Bot username, for ex: ytap_bot

    private static final String START_MESSAGE = "Hello friend! Send me a YouTube link! I'll return you an audio from it";

    public void onUpdateReceived(Update update) {
        processMessage(update);
    }

    public String getBotUsername() {
        return getTextFromFile(PATH_TO_NAME);
    }

    public String getBotToken() {
        return getTextFromFile(PATH_TO_TOKEN);
    }

    private void processMessage(Update update) {
        new Thread(() -> {
            // We check if the update has a message and the message has text
            if (update.hasMessage() && update.getMessage().hasText()) {
                Long chatId = update.getMessage().getChatId();
                Integer messageId = update.getMessage().getMessageId();  // is unique, incremental
                String inMessageText = update.getMessage().getText();

                UrlValidator validator = new UrlValidator();
                UrlNormalizer normalizer = new UrlNormalizer();

                // logic: try to cut args only in url, we should't touch plain text
                if (validator.isUrl(inMessageText) &&
                        validator.isValidYouTubeVideoUrl(normalizer.deleteArgsFromYouTubeUrl(inMessageText))) {
                    send(chatId, "It's a YouTube link! Downloading...");
                    AudioInfo info = downloadAudio(messageId, normalizer.deleteArgsFromYouTubeUrl(inMessageText));
                    send(chatId, info);
                } else if (inMessageText.equals("/start")) {
                    send(chatId, START_MESSAGE);
                } else {
                    send(chatId, "It's NOT a YouTube link! Try again.");
                }
            }
        }).start();
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

    private AudioInfo downloadAudio(int messageId, String url) {
        String audioFilePath = String.format("./audio/%s.m4a", messageId);
        Downloader downloader = new Downloader(url, audioFilePath);
        //String audioTitle = downloader.getTitle();
        downloader.getAudio();
        return downloader.getAudioInfo();
    }

    private void send(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId).setText(text);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            Logger.INSTANCE.write(String.format("Error! Can't send a message! %s", e.getMessage()));
        }
    }

    private void send(long chatId, AudioInfo info) {
        File audioFile = new File(info.getPath());
        if (audioFile.exists()) {
            SendAudio audio = new SendAudio();
            audio
                    .setTitle(info.getTitle())
                    .setCaption(info.getTitle())
                    .setChatId(chatId)
                    .setAudio(audioFile);
            try {
                execute(audio);
            } catch (TelegramApiException e) {
                Logger.INSTANCE.write(String.format("Error! Can't send an audio! %s", e.getMessage()));
            }
        } else {
            send(chatId, "Can't download the audio :( It's a broken link or not a video.");
        }
    }

    @Deprecated
    private void send(SendMessage message) {
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            Logger.INSTANCE.write(String.format("Error! Can't send a message! %s", e.getMessage()));
        }
    }

    @Deprecated
    private void send(SendAudio audio) {
        try {
            execute(audio);
        } catch (TelegramApiException e) {
            Logger.INSTANCE.write(String.format("Error! Can't send an audio! %s", e.getMessage()));
        }
    }

    @Deprecated
    private String getFileNameFromTitle(String title) {
        return title.replaceAll("\\W", "-");
    }
}