package com.example.agribandhu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Main_Account extends AppCompatActivity {

    private LinearLayout aiCropButton, aiFertilizerButton;
    private TextView farmerName, farmerLocation, farmIncome;
    private LinearLayout cropContainer;
    private String email;
    private DatabaseReference dbRef, cropRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_account);

        // ✅ Set up toolbar for menu to work
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize
        aiCropButton = findViewById(R.id.aiCropButton);
        aiFertilizerButton = findViewById(R.id.aiFertilizerButton);
        cropContainer = findViewById(R.id.cropContainer);

        farmerName = findViewById(R.id.farmerName);
        farmerLocation = findViewById(R.id.farmerLocation);
        farmIncome = findViewById(R.id.farmIncome);

        // Get email from intent
        Intent intent = getIntent();
         email = intent.getStringExtra("email");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email received", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailKey = email.replace(".", "_");
        dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(emailKey);

        cropRef = dbRef.child("crops");

        loadCropCards();
        // Convert email to Firebase key format
        // Firebase reference




        // Fetch user data
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String income = "Income: ₹25,000/month"; // optional static data

                    // Set data to UI
                    farmerName.setText(name != null ? name : "Unknown");
                    farmerLocation.setText(address != null ? address : "No address");
                    farmIncome.setText(income);
                } else {
                    Toast.makeText(Main_Account.this, "User not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Main_Account.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Crop AI button
        aiCropButton.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Main_Account.this, Ai_chat.class);
                i.putExtra("email", email);

                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Fertilizer AI button
        aiFertilizerButton.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Main_Account.this, AIChatbot.class);
                i.putExtra("email", email);
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    // ---------------- USER DATA ----------------


    // ---------------- CROP CARDS ----------------
    private void loadCropCards() {

        cropRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                cropContainer.removeAllViews();

                if (!snapshot.exists()) {
                    return;
                }

                for (DataSnapshot cropSnap : snapshot.getChildren()) {

                    // ✅ Get crop name from Firebase
                    String cropName = cropSnap.child("cropName").getValue(String.class);

                    if (cropName == null || cropName.isEmpty()) {
                        continue;
                    }

                    View card = getLayoutInflater()
                            .inflate(R.layout.item_crop_card, cropContainer, false);

                    ImageView img = card.findViewById(R.id.imgCrop);
                    TextView txt = card.findViewById(R.id.txtCropName);

                    // ✅ Set crop name
                    txt.setText(capitalize(cropName));

                    // ✅ Attach crop name to card (IMPORTANT)
                    card.setTag(cropName);

                    // ✅ Image mapping
                    switch (cropName.toLowerCase()) {

                        case "wheat":
                            img.setImageResource(R.drawable.wheat);
                            break;

                        case "tomato":
                            img.setImageResource(R.drawable.tomato);
                            break;


                        default:

                            break;
                    }

                    // ✅ Click listener – identifies exact crop clicked
                    card.setOnClickListener(v -> {
                        String selectedCrop = (String) v.getTag();

                        Intent intent = new Intent(Main_Account.this, crop_actitvty.class);
                        intent.putExtra("cropName", selectedCrop);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    });

                    cropContainer.addView(card);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Main_Account.this,
                        "Failed to load crops", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }























    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_toolbar, menu);
        return true;
    }

    // Handle menu clicks and pass email
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Get the same email that was received in this activity
        Intent oldIntent = getIntent();
        String email = oldIntent.getStringExtra("email");

        if (id == R.id.post_icon) {
            Intent intent = new Intent(this, post.class);
            intent.putExtra("email", email);
            startActivity(intent);
            Toast.makeText(this, "Weather clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
