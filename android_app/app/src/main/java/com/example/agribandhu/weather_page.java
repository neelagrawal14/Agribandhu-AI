package com.example.agribandhu;

import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.view.Gravity;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

public class weather_page extends AppCompatActivity {

    private EditText etCity, etDate;
    private Button btnGetWeather;
    private LinearLayout inputSection, weatherResultSection, hourlyForecastContainer;
    private TextView tvCity, tvTemp, tvCondition, tvHumidity, tvWind, tvSunrise, tvSunset, tvFeelsLike, tvPressure;
    private VideoView weatherVideoView;

    private static final String VISUAL_CROSSING_API_KEY = "CZ8VZ5PCCXU7RHHSYR4GT2QK9";
    private static final String LOCATIONIQ_API_KEY = "pk.84838c131bfac08c74fb98c5c37aee00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        setContentView(R.layout.activity_weather_page);

        // Input & sections
        etCity = findViewById(R.id.etCity);
        etDate = findViewById(R.id.etDate);
        btnGetWeather = findViewById(R.id.btnGetWeather);
        inputSection = findViewById(R.id.inputSection);
        weatherResultSection = findViewById(R.id.weatherResultSection);
        hourlyForecastContainer = findViewById(R.id.hourlyForecastContainer);
        weatherVideoView = findViewById(R.id.weatherVideoView);

        // TextViews
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvCondition = findViewById(R.id.tvCondition);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvPressure = findViewById(R.id.tvPressure);

        weatherResultSection.setVisibility(View.GONE);

        btnGetWeather.setOnClickListener(v -> {
            String city = etCity.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            if (city.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Enter city and date (YYYY-MM-DD)", Toast.LENGTH_SHORT).show();
                return;
            }
            inputSection.setVisibility(View.GONE);
            weatherResultSection.setVisibility(View.VISIBLE);
            weatherVideoView.setVisibility(View.VISIBLE);

            getCoordinates(city, date);
        });
    }

    private void getCoordinates(String city, String date) {
        OkHttpClient client = new OkHttpClient();
        try {
            String encodedCity = URLEncoder.encode(city, "UTF-8");
            String url = "https://us1.locationiq.com/v1/search.php?key=" + LOCATIONIQ_API_KEY + "&q=" + encodedCity + "&format=json";

            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(weather_page.this, "Geocoding error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String res = response.body().string();
                            JSONArray arr = new JSONArray(res);
                            JSONObject loc = arr.getJSONObject(0);
                            String lat = loc.getString("lat");
                            String lon = loc.getString("lon");
                            String coordinates = lat + "," + lon;

                            runOnUiThread(() ->
                                    Toast.makeText(weather_page.this, "Coordinates: " + coordinates, Toast.LENGTH_SHORT).show()
                            );

                            getWeather(coordinates, date);

                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(weather_page.this, "Geocoding parse error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(weather_page.this, "Geocoding response failed", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "City encoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getWeather(String coordinates, String date) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
                + coordinates + "/" + date + "/" + date
                + "?unitGroup=metric&key=" + VISUAL_CROSSING_API_KEY + "&include=hours&contentType=json";

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(weather_page.this, "Weather fetch error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String res = response.body().string();
                        JSONObject json = new JSONObject(res);
                        JSONObject day = json.getJSONArray("days").getJSONObject(0);

                        runOnUiThread(() -> {
                            try {
                                // Show user-entered city instead of API-resolved address
                                tvCity.setText(etCity.getText().toString().trim());

                                tvTemp.setText(day.optDouble("temp", 0) + "°C");
                                tvCondition.setText(day.optString("conditions", "Unknown"));
                                tvHumidity.setText(day.optDouble("humidity", 0) + "%");
                                tvWind.setText(day.optDouble("windspeed", 0) + " kph");
                                tvSunrise.setText(day.optString("sunrise", "-"));
                                tvSunset.setText(day.optString("sunset", "-"));
                                tvFeelsLike.setText(day.optDouble("feelslike", 0) + "°C");
                                tvPressure.setText(day.optDouble("pressure", 0) + " hPa");

                                // Show hourly forecast safely
                                JSONArray hours = day.optJSONArray("hours");
                                if (hours != null) showHourlyForecast(hours);

                                // Video loop
                                int videoResId = R.raw.sunny;
                                String cond = day.optString("conditions", "").toLowerCase();
                                if (cond.contains("sunny")) videoResId = R.raw.sunny;
                                else if (cond.contains("rain")) videoResId = R.raw.rainy;
                                else if (cond.contains("cloud")) videoResId = R.raw.cloudy;
                                else if (cond.contains("snow")) videoResId = R.raw.snow;

                                weatherVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videoResId));
                                weatherVideoView.start();
                                weatherVideoView.setOnCompletionListener(mp -> weatherVideoView.start());

                            } catch (Exception ignored) {}
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(weather_page.this, "Weather parse error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(weather_page.this, "Weather response failed", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void showHourlyForecast(JSONArray hourlyArray) {
        if (hourlyArray == null) return;
        hourlyForecastContainer.removeAllViews();

        for (int i = 0; i < hourlyArray.length(); i += 3) { // show every 3rd hour
            try {
                JSONObject hourObj = hourlyArray.getJSONObject(i);
                String datetime = hourObj.optString("datetime", "");
                String time = datetime.contains("T") ? datetime.split("T")[1] : datetime;
                String temp = hourObj.optDouble("temp", 0) + "°C";
                String cond = hourObj.optString("conditions", "Unknown");

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(25, 15, 25, 15);
                card.setBackgroundResource(R.drawable.card);
                card.setElevation(6f);
                card.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(12, 0, 12, 0);
                card.setLayoutParams(params);

                TextView tvTime = new TextView(this);
                tvTime.setText(time);
                tvTime.setTextColor(getColor(android.R.color.white));
                tvTime.setTextSize(14f);
                tvTime.setGravity(Gravity.CENTER);

                TextView tvTempHr = new TextView(this);
                tvTempHr.setText(temp);
                tvTempHr.setTextColor(getColor(android.R.color.white));
                tvTempHr.setTextSize(18f);
                tvTempHr.setGravity(Gravity.CENTER);

                TextView tvCondHr = new TextView(this);
                tvCondHr.setText(cond);
                tvCondHr.setTextColor(getColor(android.R.color.darker_gray));
                tvCondHr.setTextSize(12f);
                tvCondHr.setGravity(Gravity.CENTER);

                card.addView(tvTime);
                card.addView(tvTempHr);
                card.addView(tvCondHr);

                hourlyForecastContainer.addView(card);

            } catch (Exception ignored) {}
        }
    }
}
