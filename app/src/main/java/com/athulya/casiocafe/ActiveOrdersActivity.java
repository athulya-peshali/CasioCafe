package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ActiveOrdersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_active_orders);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // When user clicks the Active Order card, open the tracking timeline!
        findViewById(R.id.card_active_order).setOnClickListener(v -> {
            startActivity(new Intent(ActiveOrdersActivity.this, OrderStatusActivity.class));
        });
    }
}