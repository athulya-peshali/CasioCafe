package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class StaffDashboardActivity extends AppCompatActivity {

    // Orders Variables
    private RecyclerView rvLiveOrders;
    private AdminOrderAdapter orderAdapter;
    private ArrayList<OrderModel> liveOrderList;

    // Catalog Variables
    private RecyclerView rvAdminCatalog;
    private AdminProductAdapter adminAdapter;
    private ArrayList<Product> adminProductList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_dashboard);

        View rootView = findViewById(R.id.main_staff_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        findViewById(R.id.btn_admin_back).setOnClickListener(v -> finish());

        // Profile Button Security Check
        ImageButton btnStaffProfile = findViewById(R.id.btn_staff_profile);
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");

                    // ONLY show and activate the button if they are a staff member
                    if ("staff".equals(role)) {
                        btnStaffProfile.setVisibility(View.VISIBLE); // Un-hide the button!
                        btnStaffProfile.setOnClickListener(v -> {
                            startActivity(new Intent(StaffDashboardActivity.this, StaffProfileActivity.class));
                        });
                    }
                });

        // 1. Setup Live Orders RecyclerView
        rvLiveOrders = findViewById(R.id.rv_live_orders);
        rvLiveOrders.setLayoutManager(new LinearLayoutManager(this));
        liveOrderList = new ArrayList<>();
        orderAdapter = new AdminOrderAdapter(this, liveOrderList);
        rvLiveOrders.setAdapter(orderAdapter);

        // 2. Setup Catalog RecyclerView
        rvAdminCatalog = findViewById(R.id.rv_admin_catalog);
        rvAdminCatalog.setLayoutManager(new LinearLayoutManager(this));
        adminProductList = new ArrayList<>();
        adminAdapter = new AdminProductAdapter(this, adminProductList);
        rvAdminCatalog.setAdapter(adminAdapter);

        // Listen for Real-Time Orders & Load Catalog
        listenForLiveOrders();
        loadAdminCatalog();

        // Buttons
        findViewById(R.id.btn_add_promo).setOnClickListener(v -> startActivity(new Intent(StaffDashboardActivity.this, AddPromoActivity.class)));
        findViewById(R.id.btn_delete_promo).setOnClickListener(v -> Toast.makeText(this, "Promo Code Deleted", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_add_product).setOnClickListener(v -> startActivity(new Intent(StaffDashboardActivity.this, AddProductActivity.class)));
    }

    // ==========================================
    // REAL-TIME FIREBASE LISTENER
    // ==========================================
    private void listenForLiveOrders() {
        // addSnapshotListener creates a live connection. Updates happen instantly!
        db.collection("Orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        Toast.makeText(this, "Failed to load live orders", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    liveOrderList.clear();
                    for (QueryDocumentSnapshot document : snapshots) {
                        OrderModel order = document.toObject(OrderModel.class);
                        order.setDocumentId(document.getId()); // Save ID so we can update status later
                        liveOrderList.add(order);
                    }
                    orderAdapter.notifyDataSetChanged();
                });
    }

    private void loadAdminCatalog() {
        // By changing .get() to .addSnapshotListener(), this list is now LIVE!
        db.collection("Products").addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) {
                Toast.makeText(this, "Failed to load live catalog", Toast.LENGTH_SHORT).show();
                return;
            }

            adminProductList.clear();
            for (QueryDocumentSnapshot document : snapshots) {
                Product product = document.toObject(Product.class);
                product.setId(document.getId()); // CRITICAL: Save the ID for edits/deletes!
                adminProductList.add(product);
            }
            adminAdapter.notifyDataSetChanged();
        });
    }
}