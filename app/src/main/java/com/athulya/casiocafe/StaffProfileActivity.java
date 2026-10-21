package com.athulya.casiocafe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class StaffProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etName, etPhone, etEmail, etPassword;
    private ImageView ivProfileImage;
    private MaterialButton btnSave;
    private Uri imageUri = null;

    // Modern Android Photo Picker
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivProfileImage.setImageURI(uri); // Show preview instantly
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_profile);

        View rootView = findViewById(R.id.main_staff_profile);
        if(rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_profile_name);
        etPhone = findViewById(R.id.et_profile_phone);
        etEmail = findViewById(R.id.et_profile_email);
        etPassword = findViewById(R.id.et_new_password);
        ivProfileImage = findViewById(R.id.iv_profile_image);
        btnSave = findViewById(R.id.btn_save_profile);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Open Gallery
        findViewById(R.id.btn_upload_image).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        loadUserData();

        btnSave.setOnClickListener(v -> saveProfileChanges());
        // Setup Logout Button
        findViewById(R.id.btn_staff_logout).setOnClickListener(v -> logoutStaff());
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        etName.setText(document.getString("fullName"));
                        etPhone.setText(document.getString("phone"));
                        etEmail.setText(document.getString("email")); // Locked in XML

                        String profileImageUrl = document.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(ivProfileImage);
                        }
                    }
                });
    }

    private void saveProfileChanges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // 1. Update Password if typed
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                resetSaveButton();
                return;
            }
            user.updatePassword(newPassword).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to update password.", Toast.LENGTH_LONG).show()
            );
        }

        // 2. Handle ImgBB Image Upload
        if (imageUri != null) {
            String base64Image = getBase64FromUri(imageUri);
            if (base64Image != null) {
                uploadImageToImgBB(base64Image, user.getUid(), newName, newPhone);
            } else {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                resetSaveButton();
            }
        } else {
            // No new image, just update text data
            updateFirestoreData(user.getUid(), newName, newPhone, null);
        }
    }

    // ==========================================
    // IMGBB INTEGRATION
    // ==========================================

    private String getBase64FromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Compress to 60% quality to ensure fast upload times
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadImageToImgBB(String base64Image, String uid, String name, String phone) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // ---> IMGBB API KEY  <---
                String apiKey = "dd88074a869feca844dbf925be096d1e";

                URL url = new URL("https://api.imgbb.com/1/upload?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(base64Image, "UTF-8");

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(data);
                writer.flush();
                writer.close();

                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Extract the URL from the ImgBB JSON response
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String imageUrl = jsonObject.getJSONObject("data").getString("url");

                    // Go back to the main thread to update Firestore and the UI
                    runOnUiThread(() -> updateFirestoreData(uid, name, phone, imageUrl));
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(StaffProfileActivity.this, "ImgBB Upload Failed", Toast.LENGTH_SHORT).show();
                        resetSaveButton();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(StaffProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                });
            }
        });
    }

    private void updateFirestoreData(String uid, String name, String phone, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        updates.put("phone", phone);
        if (imageUrl != null) {
            updates.put("profileImageUrl", imageUrl);
        }

        db.collection("Users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                });
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Save Changes");
    }

    private void logoutStaff() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Route back to the Login screen and clear the history
        Intent intent = new Intent(StaffProfileActivity.this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}