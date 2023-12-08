package com.livefish;

import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.Map;

public class GiveawayHandler {
    public final Bot bot;
    public final GiveawayCreationHandler giveawayCreationHandler;

    public final HashMap<User, Integer> users;

    public GiveawayHandler(Bot bot) {
        this.bot = bot;
        this.giveawayCreationHandler = bot.giveawayCreationHandler;
        this.users = new HashMap<>();
    }

    public User findByNum(int num) {
        var userId = users.entrySet().stream().filter(pair -> pair.getValue() == num).findFirst();
        return userId.map(Map.Entry::getKey).orElse(null);
    }

    public void addUser(User user) {
        users.put(user, users.size());
    }

}
