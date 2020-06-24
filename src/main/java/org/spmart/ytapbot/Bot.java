package org.spmart.ytapbot;

import org.spmart.ytapbot.util.LinkValidator;
import org.spmart.ytapbot.util.Logger;

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
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            Integer messageId = update.getMessage().getMessageId();  // is unique, incremental
            String inMessageText = update.getMessage().getText();

            LinkValidator validator = new LinkValidator();
            if (inMessageText.equals("/start")) {
                // Actually, I should use AbilityBot here, but other commands I don't need.
                SendMessage message = new SendMessage();
                message.setChatId(chatId).setText(START_MESSAGE);
                send(message);
            } else if (validator.isYouTubeLink(inMessageText)) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId).setText("It's a YouTube link! Downloading...");
                send(message);

                String audioFilePath = String.format("./audio/%s.m4a", messageId);
                Downloader downloader = new Downloader(inMessageText, audioFilePath);
                //Downloader downloader = new Downloader(inMessageText); // Leads to dirt with Downloader class
                String audioTitle = downloader.getTitle();
                //String audioFilePath = String.format("./audio/%s.m4a", audioTitle); // Special chars in filename may broke paths
                //downloader.setDownloadPath(audioFilePath); // If download path isn't set, it leads to an error with audio saving
                downloader.getAudio();

                File audioFile = new File(audioFilePath);
                if (audioFile.exists()) {
                    SendAudio audio = new SendAudio();
                    audio
                            .setTitle(audioTitle)
                            .setCaption(audioTitle)
                            .setChatId(chatId)
                            .setAudio(audioFile);
                    send(audio);
                } else {
                    message.setChatId(chatId).setText("Can't download the audio :( It's a broken link or not a video.");
                    send(message);
                }
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(chatId).setText("It's NOT a YouTube link! Try again.");
                send(message);
            }
        }
    }

    public String getBotUsername() {
        return getTextFromFile(PATH_TO_NAME);
    }

    public String getBotToken() {
        return getTextFromFile(PATH_TO_TOKEN);
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

    @Deprecated
    private String getFileNameFromTitle(String title) {
        return title.replaceAll("\\W","-");
    }

    private void send(SendMessage message) {
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            Logger.INSTANCE.write(String.format("Error! Can't send a message! %s", e.getMessage()));
        }
    }

    private void send(SendAudio audio) {
        try {
            execute(audio);
        } catch (TelegramApiException e) {
            Logger.INSTANCE.write(String.format("Error! Can't send an audio! %s", e.getMessage()));
        }
    }
    //TODO: ffprobe -i <file> -show_entries format=duration -v quiet -of csv="p=0"
}