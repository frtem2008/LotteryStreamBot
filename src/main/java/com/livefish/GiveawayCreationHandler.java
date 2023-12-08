package com.livefish;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Properties;

public class GiveawayCreationHandler {
    private final Bot bot;
    private final Properties properties;

    public GiveawayCreationHandler(Bot bot) {
        this.bot = bot;
        this.properties = bot.properties;
    }

    public void promptChannel(Message message) throws TelegramApiException {
        SendMessage promptChannelLink = new SendMessage();
        promptChannelLink.setText(properties.getProperty("prompt_channel_link"));
        promptChannelLink.setChatId(String.valueOf(message.getChatId()));
        promptChannelLink.setParseMode("MarkdownV2");
        bot.execute(promptChannelLink);
        bot.state = State.QUERY_CHANNEL;
    }

    public Message sendGiveawayToChannel(List<PhotoSize> photos, String caption, String channelLink) throws TelegramApiException {
        if (photos == null) {
            SendMessage message = new SendMessage();
            message.setChatId(channelLink);
            message.setText(caption);
            message.setReplyMarkup(KeyboardHandler.createSingleButton(
                    properties.getProperty("participate_button_text"),
                    "participate"
            ));
            bot.execute(message);
        } else {
            SendPhoto photo = new SendPhoto();
            photo.setPhoto(new InputFile(photos.get(photos.size() - 1).getFileId()));
            photo.setChatId(channelLink);
            photo.setCaption(caption);
            photo.setReplyMarkup(KeyboardHandler.createSingleButton(
                    properties.getProperty("participate_button_text"),
                    "participate"
            ));
            return bot.execute(photo);
        }
    }

    public void confirmGiveaway(String chatId) throws TelegramApiException {
        sendGiveawayToChannel(bot.lastPhotos, bot.lastCaption, chatId);
        bot.sendMessage(properties.getProperty("prompt_right_giveaway"), chatId,
                KeyboardHandler.createKeyboardMarkup(
                        List.of(properties.getProperty("button_giveaway_yes"),
                                properties.getProperty("button_giveaway_no")),
                        List.of("giveaway_yes", "giveaway_no")
                ));
        bot.state = State.CONFIRM_GIVEAWAY;
    }

    public void confirmSend(String chatId) throws TelegramApiException {
        bot.state = State.CONFIRM_SEND;
        bot.sendMessage(properties.getProperty("confirm_send"), chatId,
                KeyboardHandler.createKeyboardMarkup(
                        List.of(properties.getProperty("button_send_yes"),
                                properties.getProperty("button_send_no")),
                        List.of("send_yes", "send_no")
                ));
    }
}
