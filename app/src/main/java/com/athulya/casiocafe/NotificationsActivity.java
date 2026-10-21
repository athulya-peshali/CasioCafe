package com.athulya.casiocafe;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NotificationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        View rootView = findViewById(R.id.app_bar);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // 1. Setup Back Button
        findViewById(R.id.btn_notif_back).setOnClickListener(v -> finish());

        // View References
        View cardNotif1 = findViewById(R.id.card_notif_1);
        View cardNotif2 = findViewById(R.id.card_notif_2);
        LinearLayout layoutNotificationList = findViewById(R.id.layout_notification_list);

        // 2. Individual Delete Buttons
        findViewById(R.id.btn_delete_notif_1).setOnClickListener(v -> {
            cardNotif1.setVisibility(View.GONE);
            Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_delete_notif_2).setOnClickListener(v -> {
            cardNotif2.setVisibility(View.GONE);
            Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
        });

        // 3. Clear All Button
        findViewById(R.id.btn_clear_all).setOnClickListener(v -> {
            // Hides the entire list of notifications
            layoutNotificationList.setVisibility(View.GONE);
            Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
        });
    }
}