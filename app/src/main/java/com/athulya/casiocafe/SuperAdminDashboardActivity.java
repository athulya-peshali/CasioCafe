package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SuperAdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_super_admin);

        // Security Check: Ensure they are logged in!
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        // 1. Open the Staff Dashboard to oversee live orders
        findViewById(R.id.btn_admin_live_orders).setOnClickListener(v -> {
            startActivity(new Intent(this, StaffDashboardActivity.class));
        });

        // 2. Open the Screen to create new staff accounts
        findViewById(R.id.btn_admin_add_staff).setOnClickListener(v -> {
            // We will build this next!
            startActivity(new Intent(this, AddStaffActivity.class));
        });

        // 3. Analytics Placeholder
        findViewById(R.id.btn_admin_analytics).setOnClickListener(v -> {
            Toast.makeText(this, "Analytics module loading...", Toast.LENGTH_SHORT).show();
        });

        // 4. Secure Log Out
        findViewById(R.id.btn_admin_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Admin Logged Out", Toast.LENGTH_SHORT).show();
        });
    }
}