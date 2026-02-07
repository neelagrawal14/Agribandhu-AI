package com.example.agribandhu;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;   // <-- added
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class AIChatbot extends AppCompatActivity {

    private EditText etArea, etVegetable;
    private Button btnFetchWeather, btnGenerateAI,btnStartProgress;
    private TextView tvAITimetable;
    private LinearLayout weatherSection, aiResultSection, weatherContainer;
    private DatabaseReference dbRef;
    private JSONArray lastGeneratedTimetable;   // store AI result
    private String userEmail;

    private String cityName, vegetableName;
    private String weatherSummary = "";
    private String startDate = "";
    private double latitude = 0.0;
    private double longitude = 0.0;

    private static final String WEATHER_API_KEY = "CZ8VZ5PCCXU7RHHSYR4GT2QK9";
    private static final String LOCATIONIQ_API_KEY = "pk.84838c131bfac08c74fb98c5c37aee00";

    private static final String FASTAPI_URL = "http://10.176.21.93:8000/ai_timetable";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aichatbot);

        etArea = findViewById(R.id.etArea);
        etVegetable = findViewById(R.id.etVegetable);
        btnFetchWeather = findViewById(R.id.btnFetchWeather);
        btnGenerateAI = findViewById(R.id.btnGenerateAI);
        tvAITimetable = findViewById(R.id.tvAIResponse);
        weatherSection = findViewById(R.id.weatherSection);
        aiResultSection = findViewById(R.id.aiResultSection);
        weatherContainer = findViewById(R.id.weatherContainer);
        btnStartProgress = findViewById(R.id.btnStartProgress);
        btnStartProgress.setVisibility(View.GONE);


        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("email");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "No email received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String safeEmail = userEmail.replace(".", "_");
        dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(safeEmail)
                .child("crops");

        // 🔹 START PROGRESS CLICK LISTENER
        btnStartProgress.setOnClickListener(v -> saveCropProgress());


        btnFetchWeather.setOnClickListener(v -> {
            cityName = etArea.getText().toString().trim();
            vegetableName = etVegetable.getText().toString().trim();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                if (cityName.isEmpty() || vegetableName.isEmpty()) {
                    Toast.makeText(this, "Please enter both area and vegetable name", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                new FetchCoordinatesTask().execute(cityName);
            }
        });

        btnGenerateAI.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                if (weatherSummary.isEmpty()) {
                    Toast.makeText(this, "Please fetch weather first", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                if (startDate.isEmpty()) {
                    Toast.makeText(this, "Start date not found. Fetch weather again.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                new GenerateAITask().execute();
            }
        });
    }
    private void saveCropProgress() {
        try {
            if (lastGeneratedTimetable == null) {
                Toast.makeText(this, "No timetable to save", Toast.LENGTH_SHORT).show();
                return;
            }

            String cropKey = vegetableName.toLowerCase().replace(" ", "_");

            DatabaseReference cropRef = dbRef.child(cropKey);

            cropRef.child("cropName").setValue(vegetableName);
            cropRef.child("startDate").setValue(startDate);
            cropRef.child("city").setValue(cityName);
            cropRef.child("latitude").setValue(latitude);
            cropRef.child("longitude").setValue(longitude);
            cropRef.child("createdAt").setValue(System.currentTimeMillis());


            // Save timetable day-wise
            for (int i = 0; i < lastGeneratedTimetable.length(); i++) {
                JSONObject dayObj = lastGeneratedTimetable.getJSONObject(i);

                String dayKey = "day" + dayObj.getInt("day");
                String task = dayObj.getString("task");

                cropRef.child("timetable").child(dayKey).setValue(task);
            }

            Toast.makeText(this, "✅ Crop progress saved successfully", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Failed to save data", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class FetchCoordinatesTask extends AsyncTask<String, Void, double[]> {
        @Override
        protected double[] doInBackground(String... params) {
            String city = params[0];
            try {
                String cityEncoded = URLEncoder.encode(city, "UTF-8");
                String urlStr = "https://us1.locationiq.com/v1/search.php?key=" + LOCATIONIQ_API_KEY +
                        "&q=" + cityEncoded + "&format=json&limit=1";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONArray geoArray = new JSONArray(sb.toString());
                if (geoArray.length() == 0) return null;

                JSONObject location = geoArray.getJSONObject(0);
                double lat = Double.parseDouble(location.getString("lat"));
                double lon = Double.parseDouble(location.getString("lon"));

                return new double[]{lat, lon};
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(double[] coords) {
            if (coords == null) {
                Toast.makeText(AIChatbot.this, "Could not fetch coordinates. Check the city name.", Toast.LENGTH_LONG).show();
                return;
            }

            latitude = coords[0];
            longitude = coords[1];

            Toast.makeText(AIChatbot.this, "Coordinates: Lat=" + latitude + ", Lon=" + longitude, Toast.LENGTH_LONG).show();

            new FetchWeatherTask().execute(new double[]{latitude, longitude});
        }
    }

    private class FetchWeatherTask extends AsyncTask<double[], Void, String> {
        @Override
        protected String doInBackground(double[]... params) {
            double lat = params[0][0];
            double lon = params[0][1];
            String response = "";
            try {
                String urlStr = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
                        + lat + "," + lon
                        + "/next7days?unitGroup=metric&key=" + WEATHER_API_KEY + "&contentType=json";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                response = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray daysArray = jsonObject.getJSONArray("days");

                weatherContainer.removeAllViews();

                JSONArray weatherList = new JSONArray();
                startDate = "";

                for (int i = 0; i < daysArray.length(); i++) {
                    JSONObject dayObj = daysArray.getJSONObject(i);

                    String date = dayObj.optString("datetime", "");
                    String condition = dayObj.optString("conditions", "Clear");
                    double maxTemp = dayObj.optDouble("tempmax", Double.NaN);
                    double minTemp = dayObj.optDouble("tempmin", Double.NaN);
                    double humidity = dayObj.optDouble("humidity", Double.NaN);

                    double rainMM = dayObj.optDouble("precip", 0);

                    if (i == 0) startDate = date;

                    JSONObject w = new JSONObject();
                    w.put("condition", condition);
                    w.put("rain_mm", rainMM);
                    w.put("temp_c", maxTemp);
                    weatherList.put(w);

                    TextView card = new TextView(AIChatbot.this);
                    card.setText("📅 " + date + "\n" + condition + "\n🌡 "
                            + minTemp + "°C - " + maxTemp + "°C\n💧 "
                            + humidity + "%");
                    card.setPadding(30, 30, 30, 30);
                    card.setBackgroundResource(R.drawable.editext);
                    card.setTextColor(getResources().getColor(android.R.color.black));
                    card.setTextSize(15f);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(20, 10, 20, 10);
                    card.setLayoutParams(params);
                    card.setGravity(Gravity.CENTER);

                    weatherContainer.addView(card);
                }

                weatherSummary = weatherList.toString();
                weatherSection.setVisibility(View.VISIBLE);

                Toast.makeText(AIChatbot.this, "Weather fetched for 7 days (start: " + startDate + ")", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(AIChatbot.this, "Error fetching weather. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GenerateAITask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            tvAITimetable.setText("🧠 Generating timetable from AgriBandhu AI...");
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                URL url = new URL(FASTAPI_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(20000);

                // Build JSON
                JSONObject json = new JSONObject();
                json.put("crop", vegetableName);
                json.put("start_date", startDate);

                // Convert string to array
                JSONArray fullWeather = new JSONArray(weatherSummary);

                // ---- FIX: LIMIT TO EXACTLY 7 DAYS ----
                JSONArray trimmedWeather = new JSONArray();
                for (int i = 0; i < Math.min(fullWeather.length(), 7); i++) {
                    trimmedWeather.put(fullWeather.getJSONObject(i));
                }

                json.put("weather", trimmedWeather);

                // ---- DEBUG LOGS ----
                Log.e("AI_DEBUG", "URL: " + FASTAPI_URL);
                Log.e("AI_DEBUG", "SEND BODY:\n" + json.toString(4));
                // ---------------------

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                Log.e("AI_DEBUG", "RESPONSE CODE: " + code);

                BufferedReader reader;
                if (code >= 200 && code < 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                Log.e("AI_DEBUG", "RESPONSE BODY:\n" + sb.toString());

                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
                // Log full exception message for debugging
                Log.e("AI_ERROR", "Exception while generating timetable: " + e.getMessage(), e);
                return "⚠️ Error: Could not generate timetable. Check FastAPI server at " + FASTAPI_URL;
            }
        }

        @Override

        protected void onPostExecute(String result) {
            try {
                if (result != null && result.trim().startsWith("{")) {

                    JSONObject obj = new JSONObject(result);

                    // Extract timetable array
                    JSONArray timetable = obj.getJSONArray("timetable");

                    // 🔹 STORE for Start Progress
                    lastGeneratedTimetable = timetable;

                    StringBuilder formatted = new StringBuilder();

                    for (int i = 0; i < timetable.length(); i++) {
                        JSONObject item = timetable.getJSONObject(i);

                        int day = item.getInt("day");
                        String date = item.getString("date");
                        String task = item.getString("task");

                        formatted.append("Day ").append(day)
                                .append(" – ").append(date)
                                .append("\nTask: ").append(task)
                                .append("\n\n");
                    }

                    tvAITimetable.setText(formatted.toString());

                } else {
                    tvAITimetable.setText(result);
                }

            } catch (Exception e) {
                tvAITimetable.setText("Error parsing response:\n" + result);
            }

            aiResultSection.setVisibility(View.VISIBLE);
            btnStartProgress.setVisibility(View.VISIBLE);
        }

    }
}
