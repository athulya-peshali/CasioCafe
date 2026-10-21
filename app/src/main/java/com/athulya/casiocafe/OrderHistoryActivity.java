package com.athulya.casiocafe;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrderHistory;
    private OrderHistoryAdapter adapter;
    private ArrayList<OrderModel> orderList;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);

        View rootView = findViewById(R.id.main_order_history);
        if(rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvEmpty = findViewById(R.id.tv_empty_history);
        rvOrderHistory = findViewById(R.id.rv_order_history);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(this, orderList);
        rvOrderHistory.setAdapter(adapter);

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Fetch ONLY the orders belonging to this specific user
        db.collection("Orders")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        OrderModel order = document.toObject(OrderModel.class);
                        orderList.add(order);
                    }

                    // Sort locally so newest orders appear at the top
                    // (Done locally to avoid needing complex Firestore composite indexes!)
                    orderList.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                    adapter.notifyDataSetChanged();

                    // Show empty message if they haven't bought anything yet
                    if (orderList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvOrderHistory.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvOrderHistory.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show();
                });
    }
}