package com.example.agribandhu;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;

public class crop_actitvty extends AppCompatActivity {

    private static final String TAG = "CropActivityDebug";
    private static final String WEATHER_API_KEY = "CZ8VZ5PCCXU7RHHSYR4GT2QK9";

    private String startDate;
    private double latitude = 0.0;
    private double longitude = 0.0;

    TextView tvCropTimetable;
    TextView tvAiUpdate;   // ✅ NEW (AI result)

    DatabaseReference timetableRef;
    DatabaseReference dailyUpdatesRef;
    DatabaseReference cropRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crop_actitvty);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Get Intent data
        String cropName = getIntent().getStringExtra("cropName");
        String userEmail = getIntent().getStringExtra("email");

        if (cropName == null || userEmail == null) {
            Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String emailKey = userEmail.replace(".", "_");
        String cropKey = cropName.trim().toLowerCase();

        // 🔹 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(cropName);
        }

        tvCropTimetable = findViewById(R.id.tvCropTimetable);
        tvAiUpdate = findViewById(R.id.tvAiUpdate);   // ✅ NEW

        tvCropTimetable.setText("Loading timetable...");
        tvAiUpdate.setText("AI update will appear here");

        // 🔹 Firebase references
        cropRootRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(emailKey)
                .child("crops")
                .child(cropKey);

        timetableRef = cropRootRef.child("timetable");
        dailyUpdatesRef = cropRootRef.child("dailyUpdates");

        ensureDailyUpdatesNode();
        fetchCropMetaData();
        fetchTimetable();
    }

    // --------------------------------------------------
    // FETCH startDate + latitude + longitude
    // --------------------------------------------------
    private void fetchCropMetaData() {

        cropRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                startDate = snapshot.child("startDate").getValue(String.class);
                Object latObj = snapshot.child("latitude").getValue();
                Object lonObj = snapshot.child("longitude").getValue();

                if (startDate == null || latObj == null || lonObj == null) return;

                latitude = Double.parseDouble(latObj.toString());
                longitude = Double.parseDouble(lonObj.toString());

                String todayDate = getTodayDate();

                // 🔥 Fetch weather (weather → Random Forest)
                fetchTodayWeather(latitude, longitude, todayDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Meta fetch failed: " + error.getMessage());
            }
        });
    }

    // --------------------------------------------------
    // TODAY WEATHER FETCH
    // --------------------------------------------------
    private void fetchTodayWeather(double lat, double lon, String date) {

        OkHttpClient client = new OkHttpClient();
        String coordinates = lat + "," + lon;

        String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
                + coordinates + "/" + date + "/" + date
                + "?unitGroup=metric&include=days"
                + "&key=" + WEATHER_API_KEY
                + "&contentType=json";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                crop_actitvty.this,
                                "Weather fetch failed",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful() || response.body() == null) return;

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject day = json.getJSONArray("days").getJSONObject(0);

                    double temp = day.optDouble("temp", 0);
                    double humidity = day.optDouble("humidity", 0);
                    double rain = day.optDouble("precip", 0);
                    double wind = day.optDouble("windspeed", 0);

                    // ✅ SEND WEATHER + DATE TO RANDOM FOREST
                    callAIPrediction(temp, humidity, rain, wind, 1, date);

                } catch (Exception e) {
                    Log.e(TAG, "Weather parse error", e);
                }
            }
        });
    }

    // --------------------------------------------------
    // RANDOM FOREST API CALL (USES WEATHER)
    // --------------------------------------------------
    private void callAIPrediction(
            double temp,
            double humidity,
            double rain,
            double wind,
            int cropStage,
            String date
    ) {

        OkHttpClient client = new OkHttpClient();

        String url = "http://10.176.21.93:8001/predict"
                + "?temp=" + temp
                + "&humidity=" + humidity
                + "&rain=" + rain
                + "&wind=" + wind
                + "&crop_stage=" + cropStage
                + "&date=" + date;

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0]))
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        tvAiUpdate.setText("AI server not reachable")
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful() || response.body() == null) return;

                try {
                    JSONObject json = new JSONObject(response.body().string());

                    String aiDate = json.getString("date");
                    String action = json.getString("action");

                    runOnUiThread(() ->
                            tvAiUpdate.setText(
                                    "📅 Date: " + aiDate + "\n"
                                            + "🤖 AI Action: " + action
                            )
                    );

                } catch (Exception e) {
                    Log.e(TAG, "AI parse error", e);
                }
            }
        });
    }

    // --------------------------------------------------
    // FORCE CREATE dailyUpdates NODE
    // --------------------------------------------------
    private void ensureDailyUpdatesNode() {
        dailyUpdatesRef.child("_init").setValue(true);
    }

    // --------------------------------------------------
    // FETCH TIMETABLE
    // --------------------------------------------------
    private void fetchTimetable() {

        timetableRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    tvCropTimetable.setText("No timetable found.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for (DataSnapshot d : snapshot.getChildren()) {
                    sb.append("• ").append(d.getValue(String.class)).append("\n");
                }
                tvCropTimetable.setText(sb.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --------------------------------------------------
    // GET TODAY DATE
    // --------------------------------------------------
    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
