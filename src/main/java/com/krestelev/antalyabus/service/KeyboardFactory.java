package com.krestelev.antalyabus.service;

import static com.krestelev.antalyabus.config.Constants.*;
import static com.krestelev.antalyabus.data.NearestStopsDto.*;

import com.krestelev.antalyabus.data.Language;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
@RequiredArgsConstructor
public class KeyboardFactory {

    private final MessageFactory messageFactory;
    private final UserService userService;

    public InlineKeyboardMarkup buildKeyboardWithRoutes(String stopId, List<String> routes) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < routes.size(); i++) {
            String route = routes.get(i);
            row.add(createButton(route, ROUTE_PREFIX + stopId + UNDERSCORE + route));

            if ((i + 1) == routes.size()) {
                buttons.add(row);
                break;
            }

            if (count == 4) {
                buttons.add(row);
                row = new ArrayList<>();
                count = 0;
            } else {
                count++;
            }
        }

        return initializeKeyboard(buttons);
    }

    public InlineKeyboardMarkup buildKeyboardWithBasicOptions(String stopId, long chatId) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        String refreshMessage = messageFactory.getRefreshButtonMessage(chatId);
        row.add(createButton(refreshMessage, REFRESH_PREFIX + stopId + UNDERSCORE + "refresh"));

        String trackingOptionsMessage = messageFactory.getTrackingOptionsButtonMessage(chatId);
        row.add(createButton(trackingOptionsMessage, REFRESH_PREFIX + stopId + UNDERSCORE + "other"));

        return initializeKeyboard(List.of(row));
    }

    public InlineKeyboardMarkup buildKeyboardWithTrackingOptions(String stopId, long chatId) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        String trackAllBusesMessage = messageFactory.getTrackButtonMessage(chatId);
        row1.add(createButton(trackAllBusesMessage, OTHER_OPTIONS_PREFIX + stopId + UNDERSCORE + "track"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        String otherBusesMessage = messageFactory.getMoreBusesButtonMessage(chatId);
        row2.add(createButton(otherBusesMessage,OTHER_OPTIONS_PREFIX + stopId + UNDERSCORE + "other-buses"));

        return initializeKeyboard(List.of(row1, row2));
    }

    public InlineKeyboardMarkup buildKeyboardWithFavoriteStops(Map<String, String> stops) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (var entry : stops.entrySet()) {
            String stopId = entry.getKey();
            String stopName = entry.getValue();
            String buttonText = StringUtils.isEmpty(stopName) ? stopId : String.format("%s (%s)", stopId, stopName);
            buttons.add(List.of(createButton(buttonText, STOP_PREFIX + stopId)));
        }
        return initializeKeyboard(buttons);
    }

    public InlineKeyboardMarkup buildKeyboardWithNearestStops(List<Stop> stops, Long chatId) {
        Language language = userService.getUserLanguage(chatId);
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Stop stop : stops) {
            int distanceBetweenUserAndStop = convertToMeters(stop.getDistance());
            String buttonText = String.format("%s (%s) - %s %s", stop.getStopId(), stop.getStopName(),
                distanceBetweenUserAndStop, messageFactory.getMetersButtonMessage(language));
            String callbackData = STOP_PREFIX + stop.getStopId();
            buttons.add(List.of(createButton(buttonText, callbackData)));
        }
        return initializeKeyboard(buttons);
    }

    public ReplyKeyboardMarkup buildLocationSharingKeyboard(Long chatId) {
        KeyboardButton button = KeyboardButton.builder()
                .requestLocation(true)
                .text(messageFactory.getShareLocationButtonMessage(userService.getUserLanguage(chatId)))
                .build();
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        KeyboardRow keyboardRow = new KeyboardRow(1);
        keyboardRow.add(button);
        keyboard.setKeyboard(List.of(keyboardRow));
        return keyboard;
    }

    public InlineKeyboardMarkup buildKeyboardWithLanguageButtons() {
        InlineKeyboardButton engButton = createButton("English", "language_ENG");
        InlineKeyboardButton rusButton = createButton("Русский", "language_RUS");
        InlineKeyboardButton turButton = createButton("Türkçe", "language_TUR");
        List<InlineKeyboardButton> row = List.of(engButton, rusButton, turButton);
        return initializeKeyboard(List.of(row));
    }

    private static InlineKeyboardMarkup initializeKeyboard(List<List<InlineKeyboardButton>> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public int convertToMeters(double distanceInKilometers) {
        return (int) (distanceInKilometers * 1000);
    }

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
