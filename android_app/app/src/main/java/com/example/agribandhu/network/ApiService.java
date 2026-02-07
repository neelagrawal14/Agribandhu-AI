package com.example.agribandhu.network;

import com.example.agribandhu.network.models.QuestionRequest;
import com.example.agribandhu.network.models.AnswerResponse;
import com.example.agribandhu.network.models.TimetableRequest;
import com.example.agribandhu.network.models.TimetableResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("ask")
    Call<AnswerResponse> askAI(@Body QuestionRequest request);
    @POST("ai_timetable")
    Call<TimetableResponse> generateTimetable(@Body TimetableRequest request);
}
