package com.example.agribandhu.network;

import com.example.agribandhu.network.ApiClient;
import com.example.agribandhu.network.ApiService;
import com.example.agribandhu.network.models.QuestionRequest;
import com.example.agribandhu.network.models.AnswerResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AskAPI {

    public interface AIResponse {
        void onSuccess(String reply);
        void onFailure(String error);
    }

    public static void sendToServer(String question, AIResponse callback) {

        ApiService api = ApiClient.getClient().create(ApiService.class);

        QuestionRequest body = new QuestionRequest(question);

        api.askAI(body).enqueue(new Callback<AnswerResponse>() {
            @Override
            public void onResponse(Call<AnswerResponse> call, Response<AnswerResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getReply());
                } else {
                    callback.onFailure("Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AnswerResponse> call, Throwable t) {
                callback.onFailure("Network Error: " + t.getMessage());
            }
        });
    }
}
