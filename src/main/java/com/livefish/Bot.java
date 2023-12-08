package com.livefish;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

enum State {
    START_MESSAGE,
    QUERY_GIVEAWAY,
    QUERY_CHANNEL,
    CONFIRM_GIVEAWAY,
    CONFIRM_SEND,
}

public class Bot extends TelegramLongPollingBot {
    public final Properties properties;
    public State state = State.START_MESSAGE;
    public List<PhotoSize> lastPhotos;
    public String lastCaption;
    public String lastChannel = null;

    public final KeyboardHandler keyboardHandler;
    public final MessageHandler messageHandler;
    public final GiveawayCreationHandler giveawayCreationHandler;
    public final GiveawayHandler giveawayHandler;

    public Message lastMessage;
    @Override
    public String getBotUsername() {
        return "Lottery stream bot";
    }

    public Bot() throws IOException {
        this.properties = new Properties();
        try {
            FileReader stream = new FileReader("src/main/resources/bot.props");
            System.out.println("Failed to load java props, searching current dir");
            properties.load(stream);
        } catch (FileNotFoundException e) {
            FileReader stream = new FileReader("./bot.props");
            properties.load(stream);
        }

        /* Creation order matters here! */
        messageHandler = new MessageHandler(this);
        giveawayCreationHandler = new GiveawayCreationHandler(this);
        giveawayHandler = new GiveawayHandler(this);
        keyboardHandler = new KeyboardHandler(this);

        System.out.println("Properties loaded, bot started successfully!");
    }

    @Override
    public String getBotToken() {
        return properties.getProperty("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                keyboardHandler.handleKeyboard(update);
            }
            if (update.hasMessage()) {
                messageHandler.handleMessage(update);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String text, String chatId, InlineKeyboardMarkup markup) throws TelegramApiException {
        SendMessage send = new SendMessage(chatId, messageHandler.format_channel(text));
        send.setReplyMarkup(markup);
        System.out.println(send);
        lastMessage = execute(send);
    }

}




