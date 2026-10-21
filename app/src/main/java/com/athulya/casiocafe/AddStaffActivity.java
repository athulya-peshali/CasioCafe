package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddStaffActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_staff);

        View rootView = findViewById(R.id.main_add_staff);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etName = findViewById(R.id.et_staff_name);
        EditText etEmail = findViewById(R.id.et_staff_email);
        EditText etPhone = findViewById(R.id.et_staff_phone);
        Button btnCreate = findViewById(R.id.btn_create_staff);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreate.setEnabled(false);
            btnCreate.setText("Generating Account...");

            // Generate a random 8-character password
            String generatedPassword = generateRandomPassword();

            // 1. Create the user normally (Firebase will auto-login this new user)
            mAuth.createUserWithEmailAndPassword(email, generatedPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Grab the new ID
                            String newUserId = mAuth.getCurrentUser().getUid();

                            // 2. Save Staff Details to Firestore
                            Map<String, Object> staffMap = new HashMap<>();
                            staffMap.put("fullName", name);
                            staffMap.put("email", email);
                            staffMap.put("phone", phone);
                            staffMap.put("role", "staff"); // Assign staff role

                            db.collection("Users").document(newUserId).set(staffMap)
                                    .addOnSuccessListener(aVoid -> {

                                        // 3. Send the password via EmailJS
                                        sendStaffEmail(name, email, generatedPassword);

                                        Toast.makeText(this, "Staff Created! Returning to Login.", Toast.LENGTH_LONG).show();

                                        // 4. Clean up: Sign out the new user and go to the Auth screen
                                        mAuth.signOut();
                                        Intent intent = new Intent(AddStaffActivity.this, AuthActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            btnCreate.setEnabled(true);
                            btnCreate.setText("Generate Account & Send Email");
                        }
                    });
        });
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ==========================================
    // EMAILJS INTEGRATION
    // ==========================================
    private void sendStaffEmail(String staffName, String staffEmail, String staffPassword) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // ---> PASTE YOUR 4 EMAILJS KEYS HERE <---
                String serviceId = "service_gc75rwf";
                String templateId = "template_ix3v8tq";
                String publicKey = "CFM23o2G09sYz6Klp";
                String privateKey = "aum1VwvSiZ12l8cY4S9DU";

                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("service_id", serviceId);
                payload.put("template_id", templateId);
                payload.put("user_id", publicKey);
                payload.put("accessToken", privateKey);

                org.json.JSONObject templateParams = new org.json.JSONObject();
                templateParams.put("to_name", staffName);
                templateParams.put("to_email", staffEmail);
                templateParams.put("password", staffPassword);
                payload.put("template_params", templateParams);

                java.net.URL url = new java.net.URL("https://api.emailjs.com/api/v1.0/email/send");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(conn.getOutputStream());
                writer.write(payload.toString());
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                if(responseCode == 200) {
                    System.out.println("Staff credentials sent via EmailJS!");
                } else {
                    System.out.println("Failed to send staff email. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}