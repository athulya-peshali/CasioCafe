package com.athulya.casiocafe;

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

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        View rootView = findViewById(R.id.main_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        EditText etEmail = findViewById(R.id.et_forgot_email);
        MaterialButton btnSendReset = findViewById(R.id.btn_send_reset);

        findViewById(R.id.btn_forgot_back).setOnClickListener(v -> finish());

        btnSendReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendReset.setEnabled(false);
            btnSendReset.setText("Sending...");

            // Firebase Magic happens here
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Check your email for the reset link!", Toast.LENGTH_LONG).show();
                    finish(); // Close screen and go back to login
                } else {
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    btnSendReset.setEnabled(true);
                    btnSendReset.setText("Send Reset Link");
                }
            });
        });
    }
}