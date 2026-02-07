package com.example.agribandhu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FIREBASE_POSTS";
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ArrayList<PostModel> postList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get email from previous activity
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            Toast.makeText(this, "Email fetched: " + email, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Email not fetched", Toast.LENGTH_SHORT).show();
        }

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AgriBandhu");
        }

        // RecyclerView setup
        recyclerView = findViewById(R.id.postRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);

        // Fetch all user posts
        fetchAllUserPosts();
    }

    // ✅ Fetch all user posts from "users" node
    private void fetchAllUserPosts() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userName = userSnapshot.child("name").getValue(String.class);
                    String userEmail = userSnapshot.child("email").getValue(String.class);

                    if (userSnapshot.hasChild("post")) {
                        for (DataSnapshot postSnapshot : userSnapshot.child("post").getChildren()) {
                            String description = postSnapshot.child("description").getValue(String.class);
                            String imageBase64 = postSnapshot.child("imageUri").getValue(String.class);

                            if (description != null && !description.isEmpty()) {
                                // If name not found in user, use email prefix
                                if (userName == null || userName.isEmpty()) {
                                    userName = (userEmail != null) ? userEmail.split("@")[0] : "Unknown Farmer";
                                }

                                PostModel post = new PostModel(userName, description, imageBase64);
                                postList.add(post);
                                Log.d(TAG, "User: " + userName + " | Desc: " + description);
                            }
                        }
                    }
                }

                postAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Posts loaded: " + postList.size(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Toolbar menu setup
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toobar_menu, menu);
        return true;
    }

    // Handle menu clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.weather) {
            startActivity(new Intent(this, weather_page.class));
            Toast.makeText(this, "Weather clicked", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_scheme) {
            startActivity(new Intent(this, scheme_actitvy.class));
            Toast.makeText(this, "Government Scheme clicked", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_account) {
            String email = getIntent().getStringExtra("email");
            Intent intent = new Intent(this, Main_Account.class);
            intent.putExtra("email", email);
            startActivity(intent);
            Toast.makeText(this, "Account clicked. Email sent: " + email, Toast.LENGTH_SHORT).show();
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
