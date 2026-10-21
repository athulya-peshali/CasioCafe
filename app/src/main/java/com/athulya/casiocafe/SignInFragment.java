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

public class SignInFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // NEW: Added Firestore database access

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // NEW: Initialize Firestore

        EditText etEmail = view.findViewById(R.id.et_email);
        EditText etPassword = view.findViewById(R.id.et_password);
        Button btnLogin = view.findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String inputEmail = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (inputEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Secret mapping for the Super Admin username!
            if (inputEmail.equalsIgnoreCase("athulya")) {
                inputEmail = "athulya@casiocafe.com";
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");

            // 2. Sign into Firebase Auth
            mAuth.signInWithEmailAndPassword(inputEmail, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();

                            // 3. TRAFFIC COP LOGIC: Check the user's role in Firestore
                            db.collection("Users").document(userId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        String role = documentSnapshot.getString("role");

                                        Intent intent;
                                        if ("admin".equals(role)) {
                                            // Route to Super Admin
                                            Toast.makeText(getContext(), "Welcome, Super Admin!", Toast.LENGTH_SHORT).show();
                                            intent = new Intent(getActivity(), SuperAdminDashboardActivity.class);
                                        } else if ("staff".equals(role)) {
                                            // Route to Staff Dashboard
                                            Toast.makeText(getContext(), "Welcome, Staff Member!", Toast.LENGTH_SHORT).show();
                                            intent = new Intent(getActivity(), StaffDashboardActivity.class);
                                        } else {
                                            // Normal User Route
                                            Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
                                            intent = new Intent(getActivity(), HomeActivity.class);
                                        }

                                        startActivity(intent);
                                        if (getActivity() != null) getActivity().finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Fallback to home if database request fails
                                        Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), HomeActivity.class));
                                        if (getActivity() != null) getActivity().finish();
                                    });

                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Sign In");
                        }
                    });
        });

        // Forgot Password Logic
        View tvForgot = view.findViewById(R.id.tv_forgot_password);
        if(tvForgot != null) {
            tvForgot.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), ForgotPasswordActivity.class));
            });
        }

        return view;
    }
}