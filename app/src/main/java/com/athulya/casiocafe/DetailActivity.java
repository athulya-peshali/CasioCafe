package com.athulya.casiocafe;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private double singleItemPrice = 0.0;
    private int quantity = 1;

    // NEW: We need to temporarily hold the loaded product so the cart can grab it!
    private Product currentProduct;

    // UI Elements
    private ImageView ivImage;
    private TextView tvName, tvPrice, tvCategory, tvDesc, tvQty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        // Initialize Views
        ivImage = findViewById(R.id.iv_detail_image);
        tvName = findViewById(R.id.tv_detail_name);
        tvPrice = findViewById(R.id.tv_detail_price);
        tvCategory = findViewById(R.id.tv_detail_category);
        tvDesc = findViewById(R.id.tv_detail_desc);
        tvQty = findViewById(R.id.tv_qty);

        db = FirebaseFirestore.getInstance();

        // 1. Get the Product ID passed from the RecyclerView
        String productId = getIntent().getStringExtra("PRODUCT_ID");

        if (productId != null) {
            loadProductDetails(productId);
        } else {
            Toast.makeText(this, "Error: No product selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 2. Setup Buttons
        findViewById(R.id.btn_detail_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_detail_favorite).setOnClickListener(v ->
                Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show()
        );

        // Quantity Logic
        findViewById(R.id.btn_qty_plus).setOnClickListener(v -> {
            quantity++;
            updatePriceUI();
        });

        findViewById(R.id.btn_qty_minus).setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updatePriceUI();
            }
        });

        // 3. NEW ADD TO CART LOGIC
        findViewById(R.id.btn_add_to_cart_large).setOnClickListener(v -> {
            if (currentProduct != null) {
                // Send the product and quantity directly to our local Cart database
                CartManager.getInstance().addItem(currentProduct, quantity);

                Toast.makeText(this, "Added " + quantity + " to Cart!", Toast.LENGTH_SHORT).show();
                finish(); // Close the detail screen and go back to shop
            } else {
                Toast.makeText(this, "Please wait for product to load", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductDetails(String productId) {
        db.collection("Products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {

                            // NEW: Save it to our global variable
                            currentProduct = product;

                            // Populate UI
                            tvName.setText(product.getName());
                            tvCategory.setText(product.getCategory());
                            tvDesc.setText(product.getDescription());

                            singleItemPrice = product.getPrice();
                            updatePriceUI();

                            // Load Image with Glide
                            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                                ivImage.setColorFilter(null); // Remove brown tint so image shows in full color
                                Glide.with(this)
                                        .load(product.getImageUrl())
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_store)
                                        .into(ivImage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load product", Toast.LENGTH_SHORT).show());
    }

    private void updatePriceUI() {
        tvQty.setText(String.valueOf(quantity));
        double totalPrice = singleItemPrice * quantity;
        tvPrice.setText(String.format("LKR %.2f", totalPrice));
    }
}