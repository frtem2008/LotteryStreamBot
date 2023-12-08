package com.livefish;

import org.apache.log4j.*;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TelegramApiException, IOException {
        initLogger();
        System.out.println("Bot jar build test");
        Bot lotteryBot = new Bot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(lotteryBot);
    }

    private static void initLogger() {
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();

        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);

        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile("Log.log");
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.ALL);
        fa.setAppend(false);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);
        //repeat with all other desired appenders
    }
}






// рецепт окрошки квас для окрошки хлебный	1.5	л
                 // мясо отварное (говядина)	300	г
                 // огурцы 	3-4	шт.
                 // картофель 	4	шт.
                 // яйца 	4	шт.
                 // лук зелёный 	10-15	г
                 // зелень укропа 	10	г
                 //зелень петрушки 	10	г
                 //сметана 	2	стакана
                 //соль (по вкусу)	0.5	ч. ложки
                 // сахар (по вкусу)	0.25	ч. ложки
                 // горчица (соус)	0.5	ч. ложки