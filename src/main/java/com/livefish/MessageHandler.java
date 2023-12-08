package com.livefish;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Properties;

public class MessageHandler {
    private final Bot bot;
    private final Properties properties;


    public MessageHandler(Bot bot) {
        this.bot = bot;
        this.properties = bot.properties;
    }

    public void handleMessage(Update update) throws TelegramApiException {
        var message = update.getMessage();
        var chatId = String.valueOf(message.getChatId());

        if (message.hasText()) {
            String text = message.getText();
            if (text.equals("/start")) {
                bot.state = State.START_MESSAGE;
                bot.sendMessage(properties.getProperty("start_text"), chatId,
                        KeyboardHandler.createSingleButton(
                                properties.getProperty("create_button_text"),
                                "start_button")
                                //"manage_users")
                );
            }
            if (bot.state == State.QUERY_CHANNEL) {
                bot.lastChannel = parse_channel(text);
                bot.sendMessage(
                        format_channel(properties.getProperty("confirm_channel")), chatId,
                        KeyboardHandler.createKeyboardMarkup(
                                List.of(properties.getProperty("button_channel_yes"),
                                        properties.getProperty("button_channel_no")),
                                List.of("channel_yes", "channel_no"))
                );
            }
        }
        if (bot.state == State.QUERY_GIVEAWAY) {
            if (message.hasPhoto()) {
                bot.lastPhotos = message.getPhoto();
                bot.lastCaption = message.getCaption();
            } else if (message.hasText()) {
                bot.lastPhotos = null;
                bot.lastCaption = message.getText();
            } else {
                bot.sendMessage(properties.getProperty("wrong_giveaway"), chatId, null);
            }
            if (bot.lastChannel == null) {
                bot.giveawayCreationHandler.promptChannel(message);
            } else {
                bot.giveawayCreationHandler.confirmGiveaway(chatId);
            }
        }
    }

    public String format_channel(String str) {
        return str.replaceAll("\\{channel}", "t.me/" + bot.lastChannel);
    }

    public static String parse_channel(String channel) {
        String res;
        if (channel.startsWith("@")) {
            res = channel.substring(1);
        } else if (channel.contains("t.me/")) {
            res = channel.split("t.me/")[1];
        } else {
            res = channel;
        }
        return res;
    }
}
