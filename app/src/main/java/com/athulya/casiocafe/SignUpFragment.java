package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etName = view.findViewById(R.id.et_name);
        EditText etEmail = view.findViewById(R.id.et_email);
        EditText etPassword = view.findViewById(R.id.et_password);
        Button btnRegister = view.findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);
            btnRegister.setText("Creating Account...");

            // 1. Create User in Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();

                            // 2. Save Name to Firestore Database
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("fullName", name);
                            userMap.put("email", email);

                            db.collection("Users").document(userId).set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Account Created!", Toast.LENGTH_SHORT).show();

                                        // 3. Trigger the Welcome Email!
                                        sendWelcomeEmail(name, email);

                                        startActivity(new Intent(getActivity(), HomeActivity.class));
                                        if (getActivity() != null) getActivity().finish();
                                    });
                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            btnRegister.setEnabled(true);
                            btnRegister.setText("Create Account");
                        }
                    });
        });

        return view;
    }

    // EmailJS Background Task Method
    private void sendWelcomeEmail(String userName, String userEmail) {
        // Run network request on a background thread
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // PASTE YOUR EMAILJS KEYS HERE
                String serviceId = "service_gc75rwf";
                String templateId = "template_xb5sg5g";
                String publicKey = "CFM23o2G09sYz6Klp";

                // Build the JSON Payload
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("service_id", serviceId);
                payload.put("template_id", templateId);
                payload.put("user_id", publicKey);

                // ADD THIS EXACT LINE:
                payload.put("accessToken", "aum1VwvSiZ12l8cY4S9DU");

                org.json.JSONObject templateParams = new org.json.JSONObject();
                templateParams.put("to_name", userName);
                templateParams.put("to_email", userEmail);
                payload.put("template_params", templateParams);

                // Make the HTTP POST Request to EmailJS API
                java.net.URL url = new java.net.URL("https://api.emailjs.com/api/v1.0/email/send");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(conn.getOutputStream());
                writer.write(payload.toString());
                writer.flush();
                writer.close();

                // Check Response
                int responseCode = conn.getResponseCode();
                if(responseCode == 200) {
                    System.out.println("Welcome email sent successfully via EmailJS!");
                } else {
                    System.out.println("Failed to send email. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}