package org.spmart.ytapbot;

import org.spmart.ytapbot.util.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        Logger logger = Logger.INSTANCE;

        logger.write("YOUTUBE AUDIO PODCASTER IS STARTED!\n");
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            logger.write("Can't start! Check your network connection.\n");
            e.printStackTrace();
        }
    }
}
