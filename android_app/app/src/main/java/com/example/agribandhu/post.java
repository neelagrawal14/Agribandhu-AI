package com.example.agribandhu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class post extends AppCompatActivity {

    private ImageView imagePreview;
    private EditText postDescription;
    private Button selectImageBtn, uploadPostBtn;

    private Uri selectedImageUri = null;
    private DatabaseReference dbRef;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);

        imagePreview = findViewById(R.id.imagePreview);
        postDescription = findViewById(R.id.postDescription);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        uploadPostBtn = findViewById(R.id.uploadPostBtn);

        // ✅ Get user email from intent
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("email");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "No email received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Reference to user's post node in Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userEmail.replace(".", "_"))
                .child("post");

        // ✅ Image picker
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imagePreview.setImageURI(selectedImageUri);
                    }
                });

        selectImageBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_PICK);
            intent1.setType("image/*");
            imagePickerLauncher.launch(intent1);
        });

        uploadPostBtn.setOnClickListener(v -> uploadPost());
    }

    private void uploadPost() {
        String description = postDescription.getText().toString();

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // ✅ Convert selected image to Base64 string
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos); // reduce image size
            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            // ✅ Auto-generate post number
            dbRef.get().addOnSuccessListener(snapshot -> {
                long nextPostNumber = snapshot.getChildrenCount() + 1;

                DatabaseReference newPostRef = dbRef.child(String.valueOf(nextPostNumber));
                newPostRef.child("description").setValue(description);
                newPostRef.child("farmerName").setValue(userEmail.split("@")[0]); // display name
                newPostRef.child("imageUri").setValue(base64Image) // ✅ Save Base64 image under imageUri
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(post.this, "Post saved successfully!", Toast.LENGTH_SHORT).show();
                            postDescription.setText("");
                            imagePreview.setImageURI(null);
                            selectedImageUri = null;
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(post.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            });

        } catch (IOException e) {
            Toast.makeText(this, "Image error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
