package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView rvShopProducts;
    private ProductAdapter productAdapter;
    private ArrayList<Product> productList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        View rootView = findViewById(R.id.main_shop);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        // Initialize Firebase & RecyclerView
        db = FirebaseFirestore.getInstance();
        rvShopProducts = findViewById(R.id.rv_shop_products);
        rvShopProducts.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        rvShopProducts.setAdapter(productAdapter);

        // Fetch Data!
        loadProductsFromDatabase();

        // Nav Routing
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(ShopActivity.this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        findViewById(R.id.nav_cart).setOnClickListener(v -> {
            startActivity(new Intent(ShopActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(ShopActivity.this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void loadProductsFromDatabase() {
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
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                });
    }
}