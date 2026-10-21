package com.athulya.casiocafe;

import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvPopularProducts;
    private ProductAdapter productAdapter;
    private ArrayList<Product> productList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // ADDED THIS

    private TextView tvUserName; // ADDED THIS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        View rootView = findViewById(R.id.main_home);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind the Username TextView and load the name!
        tvUserName = findViewById(R.id.tv_home_username);
        loadUserName();

        // Initialize RecyclerView
        rvPopularProducts = findViewById(R.id.rv_popular_products);
        rvPopularProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        rvPopularProducts.setAdapter(productAdapter);

        // Fetch Data
        loadPopularProducts();

        // Nav Bar Routing
        findViewById(R.id.nav_shop).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ShopActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.nav_cart).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.btn_notifications).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
        });
    }

    // NEW METHOD TO FETCH USERNAME
    private void loadUserName() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                // Split the string at the first space to just get the First Name
                                String firstName = fullName.split(" ")[0];
                                tvUserName.setText(firstName);
                            } else {
                                tvUserName.setText("Guest");
                            }
                        }
                    })
                    .addOnFailureListener(e -> tvUserName.setText("Welcome"));
        }
    }

    private void loadPopularProducts() {
        db.collection("Products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load popular items", Toast.LENGTH_SHORT).show();
                });
    }
}