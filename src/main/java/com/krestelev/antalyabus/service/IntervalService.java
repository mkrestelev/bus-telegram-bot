package com.krestelev.antalyabus.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class IntervalService {

    private static final Map<String, Integer> ALLOWED_INTERVALS = Map.of("1_min", 1, "2_min", 2, "3_min", 3);

    private final UserService userService;
    private final MessageFactory messageFactory;

    public InlineKeyboardMarkup getKeyboardWithIntervals(Long chatId) {
        String minutesOnButtonMessage = messageFactory.getMinutesOnButtonMessage(chatId);
        var buttons = List.of(List.of(
            createButton(String.format("1 %s", minutesOnButtonMessage), "1_min"),
            createButton(String.format("2 %s", minutesOnButtonMessage), "2_min"),
            createButton(String.format("3 %s", minutesOnButtonMessage), "3_min")));
        return completeKeyboardCreation(buttons);
    }

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private static InlineKeyboardMarkup completeKeyboardCreation( List<List<InlineKeyboardButton>> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public Set<String> getAllowedIntervals() {
        return ALLOWED_INTERVALS.keySet();
    }

    public void changeInterval(Long chatId, String interval) {
        userService.setInterval(chatId, ALLOWED_INTERVALS.get(interval));
    }

    public int getInterval(Long chatId) {
        return userService.getInterval(chatId);
    }
}
