package com.example.agribandhu.network.models;

import com.google.gson.annotations.SerializedName;

public class AnswerResponse {

    @SerializedName("reply")  // <- matches FastAPI field
    private String reply;

    public String getReply() {
        return reply;
    }
}
