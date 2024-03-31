package com.krestelev.antalyabus.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UserInfo {
    private volatile int interval = 2;
    private volatile int trackingTime;
    private Language language = Language.ENG;
    private Map<String, String> stops = new HashMap<>();
    @JsonIgnore
    private Future<?> task;
}
