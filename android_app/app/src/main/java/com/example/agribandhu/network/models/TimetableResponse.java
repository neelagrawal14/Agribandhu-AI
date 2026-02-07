package com.example.agribandhu.network.models;

import java.util.List;

public class TimetableResponse {
    private String crop;
    private String start_date;
    private List<String> weather;
    private List<String> schedule; // or Map<String,String> if more detailed

    public String getCrop() { return crop; }
    public String getStart_date() { return start_date; }
    public List<String> getWeather() { return weather; }
    public List<String> getSchedule() { return schedule; }
}
