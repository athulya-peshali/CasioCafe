package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    private EditText etName, etEmail, etPhone;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        View rootView = findViewById(R.id.main_profile);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Security check: Make sure someone is actually logged in
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Bind Views
        etName = findViewById(R.id.et_profile_name);
        etEmail = findViewById(R.id.et_profile_email);
        etPhone = findViewById(R.id.et_profile_phone);
        btnSave = findViewById(R.id.btn_save_profile);

        // Fetch User Data
        loadUserData();

        // Save Button Logic
        btnSave.setOnClickListener(v -> saveUserData());

        // Logout Button Logic
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            mAuth.signOut(); // Logs them out of Firebase
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Send back to Auth screen and clear backstack
            Intent intent = new Intent(ProfileActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Secret Admin Login (Clicking the Avatar)
        findViewById(R.id.iv_user_avatar).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, StaffDashboardActivity.class));
        });

        // Other existing buttons
        findViewById(R.id.btn_profile_back).setOnClickListener(v -> finish());

        // Nav Routing
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        findViewById(R.id.nav_shop).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ShopActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        findViewById(R.id.nav_cart).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    private void loadUserData() {
        // Fallback: Get email directly from Firebase Auth just in case!
        String authEmail = mAuth.getCurrentUser().getEmail();

        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // The user has a profile in the database!
                        String name = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");

                        if (name != null) etName.setText(name);
                        if (phone != null) etPhone.setText(phone);

                        // NEW FIX: If Firestore doesn't have the email, use the Auth email!
                        if (email != null && !email.isEmpty()) {
                            etEmail.setText(email);
                        } else {
                            etEmail.setText(authEmail != null ? authEmail : "No Email Found");
                        }

                    } else {
                        // The user exists in Auth, but has no database profile yet!
                        etName.setText("");
                        etName.setHint("Enter your name");
                        etEmail.setText(authEmail != null ? authEmail : "No Email Found");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    etName.setText("Error");
                    etEmail.setText("Error");
                });
    }

    private void saveUserData() {
        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // Only updating Name and Phone, NOT email.
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        updates.put("phone", newPhone);

        // Using SetOptions.merge() creates the document if it's missing!
        db.collection("Users").document(currentUserId).set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                });
    }
}