package com.example.agribandhu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.agribandhu.network.ApiClient;
import com.example.agribandhu.network.ApiService;
import com.example.agribandhu.network.models.QuestionRequest;
import com.example.agribandhu.network.models.AnswerResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Ai_chat extends AppCompatActivity {

    private EditText etPrompt;
    private Button btnSend;
    private TextView tvResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        etPrompt = findViewById(R.id.etPrompt);
        btnSend = findViewById(R.id.btnSend);
        tvResponse = findViewById(R.id.tvResponse);

        btnSend.setOnClickListener(v -> {
            String prompt = etPrompt.getText().toString().trim();
            if (!prompt.isEmpty()) {
                tvResponse.setText("⏳ Waiting for AgriBandhu AI...");

                sendToBackend(prompt);
            }
        });
    }

    private void sendToBackend(String question) {

        ApiService api = ApiClient.getClient().create(ApiService.class);
        QuestionRequest req = new QuestionRequest(question);

        api.askAI(req).enqueue(new Callback<AnswerResponse>() {
            @Override
            public void onResponse(Call<AnswerResponse> call, Response<AnswerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String reply = response.body().getReply();

                    runOnUiThread(() -> tvResponse.setText(reply));
                } else {
                    runOnUiThread(() -> tvResponse.setText("❌ Server error"));
                }
            }

            @Override
            public void onFailure(Call<AnswerResponse> call, Throwable t) {
                runOnUiThread(() -> tvResponse.setText("⚠️ Failed: " + t.getMessage()));
            }
        });
    }
}
