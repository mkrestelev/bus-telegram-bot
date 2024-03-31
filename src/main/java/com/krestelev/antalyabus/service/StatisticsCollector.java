package com.krestelev.antalyabus.service;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Aspect
@Component
public class StatisticsCollector {

    private static final String STATISTICS_FILE = "./statistics.txt";

    private final Set<Long> uniqueUsers = new HashSet<>();
    private int numberOfRequests = 0;

    @Before(value = "execution(* * .registerUser(..))")
    public void collectStatistics(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Update update = (Update) args[0];

        long chatId = update.hasCallbackQuery()
            ? update.getCallbackQuery().getMessage().getChatId()
            : update.getMessage().getChatId();

        synchronized (this) {
            uniqueUsers.add(chatId);
            numberOfRequests++;
        }
    }

    @PreDestroy
    @Scheduled(cron = "1 0 0 * * *")
    public void saveState() {
        try {
            String date = LocalDate.now(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ISO_LOCAL_DATE);
            String dayLog = String.format("%s: number of unique users - %s, number of unique requests - %s%n",
                date, uniqueUsers.size(), numberOfRequests);
            FileUtils.writeStringToFile(ResourceUtils.getFile(STATISTICS_FILE), dayLog, StandardCharsets.UTF_8, true);
            uniqueUsers.clear();
            numberOfRequests = 0;
        } catch (IOException e) {
            log.error("Error occurred while writing statistics to file, reason: {}", e.getMessage());
        }
    }
}
