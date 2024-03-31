package com.krestelev.antalyabus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krestelev.antalyabus.data.Language;
import com.krestelev.antalyabus.data.UserInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String STATE_FILE = "./state.json";

    private final ObjectMapper objectMapper;
    private final Map<Long, UserInfo> userInfos = loadState();

    public void registerUser(Update update) {
        long chatId = update.hasCallbackQuery()
            ? update.getCallbackQuery().getMessage().getChatId()
            : update.getMessage().getChatId();
        userInfos.putIfAbsent(chatId, new UserInfo());
    }

    public int getInterval(Long chatId) {
        return userInfos.get(chatId).getInterval();
    }

    public void setInterval(Long chatId, int newValue) {
        userInfos.get(chatId).setInterval(newValue);
    }

    public void resetTrackingInfo(Long chatId) {
        refreshTrackingTime(chatId);
        UserInfo userInfo = userInfos.get(chatId);
        if (userInfo.getTask() != null) {
            userInfo.getTask().cancel(true);
            userInfo.setTask(null);
        }
    }

    public void addStop(Long chatId, String stopId, String stopName) {
        getUserStops(chatId).put(stopId, stopName);
    }

    public boolean deleteStop(Long chatId, String stopId) {
        return getUserStops(chatId).remove(stopId) != null;

    }

    public Map<String, String> getUserStops(Long chatId) {
        return userInfos.get(chatId).getStops();
    }

    public void setTrackingTask(Long chatId, Future<?> task) {
        userInfos.get(chatId).setTask(task);
    }

    public Language getUserLanguage(Long chatId) {
        return userInfos.get(chatId).getLanguage();
    }

    public void setUserLanguage(Long chatId, String language) {
        userInfos.get(chatId).setLanguage(Language.valueOf(language));
    }

    public int getTrackingTime(Long chatId) {
        return userInfos.get(chatId).getTrackingTime();
    }

    public void addTrackingTime(Long chatId, int timeToAdd) {
        int trackingTime = userInfos.computeIfAbsent(chatId, valueToPut -> new UserInfo()).getTrackingTime();
        userInfos.get(chatId).setTrackingTime(trackingTime + timeToAdd);
    }

    public void refreshTrackingTime(Long chatId) {
        userInfos.get(chatId).setTrackingTime(0);
    }

    @PostConstruct
    private Map<Long, UserInfo> loadState() {
        try {
            String json = FileUtils.readFileToString(ResourceUtils.getFile(STATE_FILE), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(json, new TypeReference<>() {});
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    @PreDestroy
    @Scheduled(cron = "1 0 0 * * *")
    public void saveState() {
        try {
            String value = objectMapper.writeValueAsString(userInfos);
            FileUtils.writeStringToFile(ResourceUtils.getFile(STATE_FILE), value, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error occurred while writing state to file, reason: {}", e.getMessage());
        }
    }
}
