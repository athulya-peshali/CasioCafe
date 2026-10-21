package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Switch out of the Splash theme to your normal theme
        setTheme(R.style.Theme_CasioCafe);
        super.onCreate(savedInstanceState);

        // 2. Check if the user is currently logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User IS logged in -> Check their role to route them safely!
            routeUserSafely(currentUser.getUid());
        } else {
            // User is NOT logged in -> Send to Sign In / Sign Up Screen
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
        }
    }

    private void routeUserSafely(String uid) {
        FirebaseFirestore.getInstance().collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");

                    Intent intent;
                    if ("admin".equals(role)) {
                        intent = new Intent(MainActivity.this, SuperAdminDashboardActivity.class);
                    } else if ("staff".equals(role)) {
                        intent = new Intent(MainActivity.this, StaffDashboardActivity.class);
                    } else {
                        // Standard Customer
                        intent = new Intent(MainActivity.this, HomeActivity.class);
                    }

                    startActivity(intent);

                    // Close the launcher so the user can't hit "back" to return to the splash screen
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Safety Fallback: If the database fails (e.g., no internet connection),
                    // force them to log in again safely rather than sending them to the wrong screen.
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, AuthActivity.class));
                    finish();
                });
    }
}