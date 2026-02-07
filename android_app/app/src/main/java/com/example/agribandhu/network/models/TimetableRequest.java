package com.example.agribandhu.network.models;

import java.util.List;

public class TimetableRequest {
    private String crop;
    private String start_date;
    private List<String> weather;

    public TimetableRequest(String crop, String start_date, List<String> weather) {
        this.crop = crop;
        this.start_date = start_date;
        this.weather = weather;
    }

    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public String getStart_date() { return start_date; }
    public void setStart_date(String start_date) { this.start_date = start_date; }

    public List<String> getWeather() { return weather; }
    public void setWeather(List<String> weather) { this.weather = weather; }
}
