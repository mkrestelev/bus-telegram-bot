package com.krestelev.antalyabus.service;

import static com.krestelev.antalyabus.config.Constants.*;

import com.krestelev.antalyabus.data.NearestBusesDto.Bus;
import com.krestelev.antalyabus.data.NearestStopsDto.Stop;
import com.krestelev.antalyabus.data.UserRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import com.krestelev.antalyabus.exception.EmptyMessageException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    private static final int MILLIS_IN_SECOND = 60_000;
    private static final int MAX_STOPS_ALLOWED = 6;
    private static final int TRACKING_TIME_LIMIT = 30;

    private final KentKartService kentKartService;
    private final IntervalService intervalService;
    private final ExecutorService executorService;
    private final UserService userService;
    private final MessageFactory messageFactory;
    private final KeyboardFactory keyboardFactory;

    @Value("${bot.username}")
    private String botUsername;

    public Bot(@Value("${bot.token}") String botToken, KentKartService kentKartService,
            IntervalService intervalService, ExecutorService executorService, UserService userService,
            MessageFactory messageFactory, KeyboardFactory keyboardFactory) {

        super(botToken);
        this.kentKartService = kentKartService;
        this.intervalService = intervalService;
        this.executorService = executorService;
        this.userService = userService;
        this.messageFactory = messageFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        userService.registerUser(update);

        if (update.hasCallbackQuery()) {
            handleCallback(update);
        } else if (update.getMessage().hasLocation()) {
            handleNearestStopsRequest(update);
        } else {
            handleCommon(update);
        }
    }

    private void handleNearestStopsRequest(Update update) {
        Long chatId = update.getMessage().getChatId();

        Location location = update.getMessage().getLocation();
        List<Stop> nearestStops = kentKartService.getNearestStops(location.getLatitude(), location.getLongitude());

        var keyboard = keyboardFactory.buildKeyboardWithNearestStops(nearestStops, chatId);
        String message = messageFactory.getSelectStopMessage(chatId);

        sendMessage(chatId, message, keyboard);
    }

    private void handleCallback(Update update) {
        Long chatId = update.getCallbackQuery().getFrom().getId();
        String callbackData = update.getCallbackQuery().getData();
        if (intervalService.getAllowedIntervals().contains(callbackData)) {
            changeTrackingInterval(chatId, callbackData);
        } else if (callbackData.startsWith(LANGUAGE_PREFIX)) {
            changeLanguage(callbackData, chatId);
        } else if (callbackData.startsWith(STOP_PREFIX)) {
            handleNearestBusesRequest(callbackData, chatId);
        } else if (callbackData.startsWith(REFRESH_PREFIX)) {
            handleUserReactionOnRefresh(callbackData, chatId);
        } else if (callbackData.startsWith(OTHER_OPTIONS_PREFIX)) {
            handleUserReactionOnSelectingOtherOptions(callbackData, chatId);
        } else if (callbackData.startsWith(ROUTE_PREFIX)) {
            handleUserReactionOnSelectingParticularBus(callbackData, chatId);
        }
    }

    private void changeTrackingInterval(Long chatId, String callbackData) {
        intervalService.changeInterval(chatId, callbackData);
        sendMessage(chatId, messageFactory.getReplyMessageForChangingInterval(chatId));
    }

    private void changeLanguage(String callbackData, Long chatId) {
        String newLanguage = callbackData.split(UNDERSCORE)[1];
        userService.setUserLanguage(chatId, newLanguage);
        sendMessage(chatId, messageFactory.getReplyMessageForChangingLanguage(chatId));
    }

    private void handleNearestBusesRequest(String callbackData, Long chatId) {
        String stopId = callbackData.split(UNDERSCORE)[1];
        handleGetBusesRequest(chatId, stopId);
    }

    private void handleUserReactionOnRefresh(String callbackData, Long chatId) {
        String[] split = callbackData.split(UNDERSCORE);
        String stopId = split[1];
        String option = split[2];
        if ("refresh".equals(option)) {
            handleGetBusesRequest(chatId, UserRequest.of(chatId, stopId));
        } else {
            sendTrackingOptionsRequest(chatId, stopId);
        }
    }

    private void handleUserReactionOnSelectingOtherOptions(String callbackData, Long chatId) {
        String[] split = callbackData.split(UNDERSCORE);
        String stopId = split[1];
        String option = split[2];
        if ("track".equals(option)) {
            UserRequest userRequest = UserRequest.builder()
                .stopId(stopId)
                .busId("all")
                .observing(true)
                .build();
            handleTrackRequest(chatId, userRequest);
        } else {
            sendSelectRouteMessage(chatId, stopId);
        }
    }

    private void handleUserReactionOnSelectingParticularBus(String callbackData, Long chatId) {
        String[] split = callbackData.split(UNDERSCORE);
        String stopId = split[1];
        String route = split[2];
        UserRequest request = UserRequest.builder()
            .stopId(stopId)
            .busId(route)
            .build();
        handleTrackRequest(chatId, request);
    }

    private void sendTrackingOptionsRequest(Long chatId, String stopId) {
        InlineKeyboardMarkup keyboard = keyboardFactory.buildKeyboardWithTrackingOptions(stopId, chatId);
        sendMessage(chatId, messageFactory.getSelectOptionMessage(chatId), keyboard);
    }

    private void handleCommon(Update update) {
        validateUserInputIsNotEmpty(update);

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String userInput = message.getText();
        String userName = message.getChat().getFirstName();

        switch (userInput) {
            case "/start" -> handleStartCommand(chatId, userName);
            case "/help" -> handleHelpCommand(chatId);
            case "/stops" -> handleGetStopsCommand(chatId);
            case "/interval" -> handleSetIntervalCommand(chatId);
            case "/cancel" -> handleCancelTrackingCommand(chatId);
            case "/nearest" -> handleRequestLocation(chatId);
            case "/language" -> handleChangeLanguageCommand(chatId);
            default -> handleCommon(update.getMessage());
        }
    }

    private static void validateUserInputIsNotEmpty(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            throw new EmptyMessageException("Incoming update is empty");
        }
    }

    private void handleStartCommand(Long chatId, String name) {
        String answer = String.format(messageFactory.getStartMessage(), name);
        InlineKeyboardMarkup keyboard = keyboardFactory.buildKeyboardWithLanguageButtons();
        sendMessage(chatId, answer, keyboard);
    }

    private void handleHelpCommand(Long chatId) {
        sendMessage(chatId, messageFactory.getHelpMessage(chatId));
    }

    private void handleGetStopsCommand(Long chatId) {
        Map<String, String> userStops = userService.getUserStops(chatId);
        if (userStops.isEmpty()) {
            sendMessage(chatId, messageFactory.getShouldAddStopsMessage(chatId));
        }
        var keyboard = keyboardFactory.buildKeyboardWithFavoriteStops(userStops);
        sendMessage(chatId, messageFactory.getSelectStopMessage(chatId), keyboard);
    }

    private void handleRequestLocation(Long chatId) {
        String message = messageFactory.getShareLocationMessage(chatId);
        ReplyKeyboardMarkup keyboard = keyboardFactory.buildLocationSharingKeyboard(chatId);
        sendMessage(chatId, message, keyboard);
    }

    private void handleChangeLanguageCommand(Long chatId) {
        InlineKeyboardMarkup keyboard = keyboardFactory.buildKeyboardWithLanguageButtons();
        sendMessage(chatId, "Select language \\| Выберите язык \\| Dil seçin:", keyboard);
    }

    private void handleSetIntervalCommand(Long chatId) {
        String textToSend = messageFactory.getSetDefaultIntervalMessage(chatId);
        InlineKeyboardMarkup keyboard = intervalService.getKeyboardWithIntervals(chatId);
        sendMessage(chatId, textToSend, keyboard);
    }

    private void handleCancelTrackingCommand(Long chatId) {
        userService.resetTrackingInfo(chatId);
        String message = messageFactory.getTrackingCancelledMessage(chatId);
        sendMessage(chatId, message);
    }

    private void handleCommon(Message message) {
        Long chatId = message.getChatId();
        String userInput = message.getText().toLowerCase();

        if (userInput.startsWith("add ")) {
            addStop(chatId, userInput);
        } else if (userInput.startsWith("delete ") || userInput.startsWith("remove ")) {
            removeStop(chatId, userInput);
        } else {
            handleGetBusesRequest(chatId, userInput);
        }
    }

    private void removeStop(Long chatId, String userInput) {
        String stopId = userInput.split(StringUtils.SPACE)[1];
        boolean deleted = userService.deleteStop(chatId, stopId);
        if (deleted) {
            sendMessage(chatId, String.format(messageFactory.getStopIsDeletedMessage(chatId), stopId));
        } else {
            sendMessage(chatId, String.format(messageFactory.getStopNotFoundMessage(chatId), stopId));
        }
    }

    private void addStop(Long chatId, String userInput) {
        String message;
        if (userService.getUserStops(chatId).size() >= MAX_STOPS_ALLOWED) {
            message = messageFactory.getStopListIsFullMessage(chatId);
        } else {
            String[] split = userInput.split(StringUtils.SPACE);
            String stopId = split[1].trim();
            String stopName = Arrays.stream(split).skip(2).collect(Collectors.joining(StringUtils.SPACE));

            userService.addStop(chatId, stopId, stopName);
            message = String.format(messageFactory.getStopAddedMessage(chatId), stopId);
        }
        sendMessage(chatId, message);
    }

    private void sendSelectRouteMessage(Long chatId, String stopId) {
        List<String> routes = new ArrayList<>(kentKartService.getRoutes(stopId));
        InlineKeyboardMarkup keyboard = keyboardFactory.buildKeyboardWithRoutes(stopId, routes);
        sendMessage(chatId, messageFactory.getSelectBusMessage(chatId), keyboard);
    }

    private void handleGetBusesRequest(Long chatId, String stopId) {
        UserRequest userRequest = UserRequest.builder()
            .userId(chatId)
            .stopId(stopId)
            .build();
        handleGetBusesRequest(chatId, userRequest);
    }

    private void handleGetBusesRequest(long chatId, UserRequest request) {
        List<Bus> buses = kentKartService.getNearestBuses(request);
        InlineKeyboardMarkup keyboard = keyboardFactory.buildKeyboardWithBasicOptions(request.getStopId(), chatId);
        String message;
        if (buses.isEmpty()) {
            message = String.format(messageFactory.getNoBusesErrorMessage(chatId), request.getStopId());
        } else {
            message = getBusesMessage(buses, chatId);
        }
        sendMessage(chatId, message, keyboard);
    }

    private void handleTrackRequest(long chatId, UserRequest request) {
        sendTrackingIsStartedMessage(chatId);
        userService.resetTrackingInfo(chatId);

        Future<?> task;
        if (request.isParticularBusRequested()) {
            task = executorService.submit(() -> observeParticularBus(request, chatId));
        } else {
            task = executorService.submit(() -> observeAllBuses(request, chatId));
        }
        userService.setTrackingTask(chatId, task);
    }

    private void sendTrackingIsStartedMessage(long chatId) {
        String messageTemplate = messageFactory.getTrackingStartedMessage(chatId);
        int userInterval = userService.getInterval(chatId);
        String message = String.format(messageTemplate, userInterval);
        sendMessage(chatId, message);
    }

    @SneakyThrows
    private void observeAllBuses(UserRequest userRequest, long chatId) {
        if (userService.getTrackingTime(chatId) > TRACKING_TIME_LIMIT) {
            userService.resetTrackingInfo(chatId);
            sendMessage(chatId, messageFactory.getTrackingTimeExceededMessage(chatId));
        } else {
            List<Bus> buses = kentKartService.getNearestBuses(userRequest);

            String message = buses.isEmpty()
                ? String.format(messageFactory.getNoBusesErrorMessage(chatId), userRequest.getStopId())
                : getBusesMessage(buses, chatId);
            sendMessage(chatId, message);

            long numberOfMinutesToSleep = intervalService.getInterval(chatId);
            userService.addTrackingTime(chatId, (int) numberOfMinutesToSleep);
            Thread.sleep(numberOfMinutesToSleep * MILLIS_IN_SECOND);
            observeAllBuses(userRequest, chatId);
        }
    }

    @SneakyThrows
    private void observeParticularBus(UserRequest userRequest, long chatId) {
        Optional<Bus> bus = kentKartService.getParticularBus(chatId, userRequest.getStopId(), userRequest.getBusId());

        if (bus.isPresent() && busIsNearTheStop(bus.get(), chatId)) {
            userService.resetTrackingInfo(chatId);
            String message = getTrackingIsFinishedMessage(bus.get(), chatId);
            sendMessage(chatId, message);
        } else if (userService.getTrackingTime(chatId) > TRACKING_TIME_LIMIT) {
            userService.resetTrackingInfo(chatId);
            String message = messageFactory.getTrackingTimeExceededMessage(chatId);
            sendMessage(chatId, message);
        } else {
            long numberOfMinutesToSleep = intervalService.getInterval(chatId);
            userService.addTrackingTime(chatId, (int) numberOfMinutesToSleep);

            String message = bus.isEmpty()
                ? String.format(messageFactory.getNoBusesErrorMessage(chatId), userRequest.getStopId())
                : getBusesMessage(List.of(bus.get()), chatId);
            sendMessage(chatId, message);

            Thread.sleep(numberOfMinutesToSleep * MILLIS_IN_SECOND);
            observeParticularBus(userRequest, chatId);
        }
    }

    private boolean busIsNearTheStop(Bus bus, long chatId) {
        Integer timeDiff = bus.getTimeDiff();
        return timeDiff != null && timeDiff <= intervalService.getInterval(chatId);
    }

    public void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        performSendMessage(sendMessage, chatId, textToSend);
    }

    private void sendMessage(Long chatId, String textToSend, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboard);
        performSendMessage(sendMessage, chatId, textToSend);
    }

    @SneakyThrows
    private void performSendMessage(SendMessage sendMessage, Long chatId, String textToSend) {
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.enableMarkdownV2(true);
        execute(sendMessage);
    }

    private String getBusesMessage(List<Bus> buses, Long chatId) {
        Map<String, List<Bus>> busesByRouteCode = buses.stream()
            .collect(Collectors.groupingBy(Bus::getDisplayRouteCode));

        return busesByRouteCode.entrySet().stream()
            .sorted(getBusComparator())
            .map(entry -> getLineWithInfoForParticularRoute(entry.getKey(), entry.getValue(), chatId))
            .collect(Collectors.joining("\n"));
    }

    private static Comparator<Entry<String, List<Bus>>> getBusComparator() {

        ToIntFunction<Entry<String, List<Bus>>> comparingByTimeDiff = entry ->
            entry.getValue()
                .stream()
                .mapToInt(Bus::getTimeDiff)
                .min()
                .orElse(0);

        ToIntFunction<Entry<String, List<Bus>>> comparingByStopDiff = entry ->
            entry.getValue()
                .stream()
                .mapToInt(Bus::getStopDiff)
                .min()
                .orElse(0);

        return Comparator.comparingInt(comparingByTimeDiff).thenComparingInt(comparingByStopDiff);
    }

    private String getLineWithInfoForParticularRoute(String route, List<Bus> buses, Long chatId) {

        boolean particularBusIsRequestedWithNoTimeDiffForBusYet = buses.stream()
            .map(Bus::getTimeDiff)
            .allMatch(Objects::isNull);

        if (particularBusIsRequestedWithNoTimeDiffForBusYet) {
            return String.format(messageFactory.getGetBusStopDiffMessage(chatId), route, buses.get(0).getStopDiff());
        }

        ArrayList<String> timeDiffsForBus = buses.stream()
            .map(this::markAsNotDepartedIfNeeded)
            .collect(Collectors.toCollection(ArrayList::new));

        String nearestBus = timeDiffsForBus.get(0);
        if (!nearestBus.startsWith("~")) {
            timeDiffsForBus.set(0, "*" + nearestBus + "*");
        }

        return String.format(messageFactory.getGetBusMessage(chatId), route, timeDiffsForBus);
    }

    private String markAsNotDepartedIfNeeded(Bus bus) {
        Integer timeDiff = bus.getTimeDiff();
        if (bus.isNotDeparted()) {
            return "~" + timeDiff + "~";
        }
        return String.valueOf(timeDiff);
    }

    private String getTrackingIsFinishedMessage(Bus bus, Long chatId) {
        return String.format(messageFactory.getBusArrivingMessage(chatId), bus.getDisplayRouteCode());
    }
}
