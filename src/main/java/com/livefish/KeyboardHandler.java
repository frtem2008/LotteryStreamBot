package com.livefish;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KeyboardHandler {
    private final Bot bot;
    private final GiveawayCreationHandler giveawayCreationHandler;
    private final Properties properties;

    private Message userMenuMsg;
    private final GiveawayHandler giveawayHandler;
    private int page = 0;

    public KeyboardHandler(Bot bot) {
        this.bot = bot;
        this.giveawayCreationHandler = bot.giveawayCreationHandler;
        this.properties = bot.properties;
        this.giveawayHandler = bot.giveawayHandler;
    }

    public void handleKeyboard(Update update) throws TelegramApiException {
        var query = update.getCallbackQuery();
        var callbackData = query.getData();
        var message = query.getMessage();
        var chatId = String.valueOf(message.getChatId());

        switch (callbackData) {
            case "start_button" -> {
                bot.state = State.QUERY_GIVEAWAY;
                EditMessageText send = new EditMessageText();
                send.setMessageId(message.getMessageId());
                send.setText(properties.getProperty("add_lottery_text"));
                send.setChatId(chatId);
                bot.execute(send);
            }

            case "channel_yes" -> {
                EditMessageText send = new EditMessageText();
                send.setMessageId(message.getMessageId());
                send.setText(bot.messageHandler.format_channel(properties.getProperty("channel_confirmed")));
                send.setChatId(chatId);
                bot.execute(send);
                giveawayCreationHandler.confirmGiveaway(chatId);
            }

            case "channel_no" -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(message.getMessageId());
                deleteMessage.setChatId(chatId);
                bot.execute(deleteMessage);
                giveawayCreationHandler.promptChannel(message);
            }

            case "giveaway_yes" -> {
                giveawayCreationHandler.confirmSend(chatId);
            }

            case "giveaway_no" -> {
                bot.state = State.QUERY_GIVEAWAY;
                bot.sendMessage(properties.getProperty("change_lottery"), chatId, null);
            }

            case "send_yes" -> {
                try {
                    giveawayChannelId = bot.lastChannel;
                    giveawayCreationHandler.sendGiveawayToChannel(
                            bot.lastPhotos, bot.lastCaption, "@" + bot.lastChannel);
                    bot.sendMessage(properties.getProperty("giveaway_sent"), chatId,
                            createSingleButton(properties.getProperty("button_manage_users"), "manage_users"));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    bot.sendMessage(
                            bot.messageHandler.format_channel(properties.getProperty("giveaway_send_error")), chatId,
                            createKeyboardMarkup(
                                    List.of(
                                            properties.getProperty("button_change_channel"),
                                            properties.getProperty("button_try_again")
                                    ),
                                    List.of("change_channel", "send_yes")
                            )
                    );
                }
            }

            case "send_no" -> {
                bot.sendMessage(properties.getProperty("giveaway_cancelled"), chatId,
                        createSingleButton(
                                properties.getProperty("create_button_text"),
                                "start_button")
                );
            }

            case "change_channel" -> {
                giveawayCreationHandler.promptChannel(message);
            }

            case "manage_users" -> {
                bot.sendMessage(properties.getProperty("choose_winner"), chatId,
                        createUserPage(0)
                );
                userMenuMsg = bot.lastMessage;
            }

            case "page_right" -> {
                page++;
                EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
                edit.setChatId(chatId);
                edit.setMessageId(message.getMessageId());
                edit.setReplyMarkup(createUserPage(page));
                bot.execute(edit);
            }

            case "page_left" -> {
                page--;
                EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
                edit.setChatId(chatId);
                edit.setMessageId(message.getMessageId());
                edit.setReplyMarkup(createUserPage(page));
                bot.execute(edit);
            }

            case "participate" -> {
                System.out.println("Participate from " + query.getFrom());
                giveawayHandler.addUser(query.getFrom());
                EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
                edit.setChatId(String.valueOf(userMenuMsg.getChatId()));
                edit.setMessageId(userMenuMsg.getMessageId());
                edit.setReplyMarkup(createUserPage(page));
                bot.execute(edit);
            }

            case "confirm" -> {
                bot.sendMessage(properties.getProperty("winner"), chatId, null);
            }

            default -> {
                if (callbackData.startsWith("user")) {
                    String username = callbackData.split("\\|")[1];
                    System.out.println("USERNAME: " + username);
                    bot.sendMessage(properties.getProperty("confirm_winner").replaceAll("\\{user}", username),
                        chatId,
                        createSingleButton(properties.getProperty("confirm"), "confirm")
                    );

                } else {
                    // TODO: 11/25/23 CHECK FOR
                    System.err.println("Unnown callback): " + callbackData);
                }
            }
        }
    }
    public InlineKeyboardMarkup createUserPage(int pageNum) {
        List<List<InlineKeyboardButton>> kb = new ArrayList<>();

        int users_per_page = Integer.parseInt(properties.getProperty("users_per_page"));
        int begin_index = pageNum * users_per_page;
        int max_pages = (int) Math.floor((double) giveawayHandler.users.size() / users_per_page);


        for (int i = 0; i < users_per_page; i++) {
            ArrayList<InlineKeyboardButton> buttonList = new ArrayList<>();
            User user = giveawayHandler.findByNum(i + begin_index);
            if (user == null) {
                break;
            }
            var button = new InlineKeyboardButton();
            if (user.getUserName() == null) {
                button.setText(user.getFirstName() + " " + user.getLastName());
            } else {
                button.setText(user.getUserName());
            }
            button.setCallbackData("user|" + button.getText());
            buttonList.add(button);
            kb.add(buttonList);
        }
        kb.add(getControlButtons(pageNum, max_pages));

        return new InlineKeyboardMarkup(kb);
    }

    private static ArrayList<InlineKeyboardButton> getControlButtons(int pageNum, int max_pages) {
        ArrayList<InlineKeyboardButton> controlsList = new ArrayList<>();

        var back = new InlineKeyboardButton();
        if (pageNum > 0) {
            back.setText("<");
            back.setCallbackData("page_left");
        } else {
            back.setText("🚫");
            back.setCallbackData("ignored");
        }
        controlsList.add(back);
        var page_index = new InlineKeyboardButton();
        page_index.setText((pageNum + 1) + "/" + (max_pages + 1));
        page_index.setCallbackData("ignored");
        controlsList.add(page_index);

        var forward = new InlineKeyboardButton();
        if (pageNum >= max_pages) {
            forward.setText("🚫");
            forward.setCallbackData("ignored");
        } else {
            forward.setText(">");
            forward.setCallbackData("page_right");
        }

        controlsList.add(forward);
        return controlsList;
    }

    public static InlineKeyboardMarkup createKeyboardMarkup(List<String> texts, List<String> callbacks) {
        List<List<InlineKeyboardButton>> kb = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            var button = new InlineKeyboardButton();
            button.setText(texts.get(i));
            button.setCallbackData(callbacks.get(i));
            ArrayList<InlineKeyboardButton> buttonList = new ArrayList<>();
            buttonList.add(button);
            kb.add(buttonList);
        }

        return new InlineKeyboardMarkup(kb);
    }

    public static InlineKeyboardMarkup createSingleButton(String text, String callbackData) {
        return createKeyboardMarkup(List.of(text), List.of(callbackData));
    }
}
