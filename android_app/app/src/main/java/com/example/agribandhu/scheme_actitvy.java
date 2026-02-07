package com.example.agribandhu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class scheme_actitvy extends AppCompatActivity {

    private EditText editTextDate;
    private Button btnFetchScheme;
    private TextView textViewResult;
    private LinearLayout mainLayout;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_actitvy);

        editTextDate = findViewById(R.id.editTextdate);
        btnFetchScheme = findViewById(R.id.btnfetchscheme);

        // Create a TextView dynamically to show results
        textViewResult = new TextView(this);
        textViewResult.setTextSize(16);
        textViewResult.setPadding(40, 50, 40, 40);
        textViewResult.setTextColor(getResources().getColor(android.R.color.black));

        mainLayout = findViewById(R.id.main);
        mainLayout.addView(textViewResult);

        dbRef = FirebaseDatabase.getInstance().getReference("schemes");

        btnFetchScheme.setOnClickListener(v -> {
            String date = editTextDate.getText().toString().trim();
            if (date.isEmpty()) {
                Toast.makeText(scheme_actitvy.this, "Please enter a date!", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchSchemeData(date);
        });
    }

    private void fetchSchemeData(String date) {
        dbRef.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LinearLayout schemeContainer = findViewById(R.id.schemeContainer);
                View scrollSchemes = findViewById(R.id.scrollSchemes);

                schemeContainer.removeAllViews(); // Clear old cards

                if (snapshot.exists()) {
                    // Hide inputs
                    editTextDate.setVisibility(View.GONE);
                    btnFetchScheme.setVisibility(View.GONE);

                    // Show scroll view
                    scrollSchemes.setVisibility(View.VISIBLE);

                    // Loop through all schemes for that date
                    for (DataSnapshot schemeSnap : snapshot.getChildren()) {
                        String name = schemeSnap.child("Name").getValue(String.class);
                        String details = schemeSnap.child("Details").getValue(String.class);

                        // Inflate (create) a card layout
                        View cardView = getLayoutInflater().inflate(R.layout.scheme_card, schemeContainer, false);

                        // Get references inside card
                        TextView tvName = cardView.findViewById(R.id.tvSchemeName);
                        TextView tvDetails = cardView.findViewById(R.id.tvSchemeDetails);

                        // Fill data
                        tvName.setText(name != null ? name : "No name available");
                        tvDetails.setText(details != null ? details : "No details available");

                        // Add card to container
                        schemeContainer.addView(cardView);
                    }
                } else {
                    Toast.makeText(scheme_actitvy.this, "❌ No schemes found for this date!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(scheme_actitvy.this, "⚠️ Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }}