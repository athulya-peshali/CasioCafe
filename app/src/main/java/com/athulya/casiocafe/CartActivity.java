package com.athulya.casiocafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.UUID;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CartActivity extends AppCompatActivity {

    // ==========================================
    // 🛑 PASTE YOUR PAYHERE CREDENTIALS HERE 🛑
    // ==========================================
    private final String MERCHANT_ID = "1222469";
    private final String MERCHANT_SECRET = "MzE0OTA2OTE5NDE1Njg2OTcxOTAxNTAyNTY1MjIxMjc5MjgwODY4Mw==";

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private TextView tvSubtotal, tvTaxes, tvTotal;
    private LinearLayout layoutEmptyCart;
    private MaterialCardView checkoutPanel;

    private double taxRate = 0.05;
    private double discount = 0.0;
    private double currentFinalTotal = 0.0;

    private String currentOrderId = "";

    // Firebase & User Data Variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String customerFirstName = "Guest";
    private String customerLastName = "User";
    private String customerEmail = "no-email@example.com";
    private String customerPhone = "0000000000";

    // This Launcher listens for the result from the PayHere popup
    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null && result.getData().hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                    PHResponse<StatusResponse>
                            response = (PHResponse<StatusResponse>) result.getData().getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                    if (response != null && response.isSuccess()) {
                        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show();
                        CartManager.getInstance().clearCart();

                        // NEW ROUTING LOGIC:
                        Intent intent = new Intent(CartActivity.this, PaymentSuccessActivity.class);
                        intent.putExtra("ORDER_ID", currentOrderId);
                        intent.putExtra("TOTAL_AMOUNT", currentFinalTotal);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Payment Failed: " + (response != null ? response.toString() : "Unknown Error"),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        View rootView = findViewById(R.id.main_cart);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTaxes = findViewById(R.id.tv_taxes);
        tvTotal = findViewById(R.id.tv_total);
        layoutEmptyCart = findViewById(R.id.layout_empty_cart);
        checkoutPanel = findViewById(R.id.checkout_panel);

        findViewById(R.id.btn_cart_back).setOnClickListener(v -> finish());

        // Setup RecyclerView
        rvCartItems = findViewById(R.id.rv_cart_items);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, CartManager.getInstance().getCartItems(), this::calculateTotals);
        rvCartItems.setAdapter(cartAdapter);

        calculateTotals();
        fetchUserDetails(); // Fetch the logged-in user's data in the background

        // Promo Logic
        findViewById(R.id.btn_apply_promo).setOnClickListener(v -> {
            EditText etPromo = findViewById(R.id.et_promo_code);
            String code = etPromo.getText().toString().trim().toUpperCase();

            if (code.equals("FREECOFFEE") || code.equals("BOGO")) {
                discount = 200.00;
                Toast.makeText(this, "Promo Applied! LKR 200 off.", Toast.LENGTH_SHORT).show();
                calculateTotals();
            } else {
                Toast.makeText(this, "Invalid Promo Code", Toast.LENGTH_SHORT).show();
            }
        });

        // Checkout Logic -> Trigger PayHere
        findViewById(R.id.btn_checkout).setOnClickListener(v -> {
            if (CartManager.getInstance().getCartItems().isEmpty()) return;
            startPayHereCheckout();
        });

        findViewById(R.id.btn_shop_now).setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, ShopActivity.class));
            finish();
        });
    }

    private void fetchUserDetails() {
        if (mAuth.getCurrentUser() != null) {
            customerEmail = mAuth.getCurrentUser().getEmail(); // Default to Auth email
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String phone = documentSnapshot.getString("phone");

                            // PayHere needs First and Last name separately, so we split the full name
                            if (fullName != null && !fullName.trim().isEmpty()) {
                                String[] nameParts = fullName.trim().split(" ", 2);
                                customerFirstName = nameParts[0];
                                if (nameParts.length > 1) {
                                    customerLastName = nameParts[1];
                                } else {
                                    customerLastName = "";
                                }
                            }
                            if (phone != null && !phone.isEmpty()) {
                                customerPhone = phone;
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If it fails, we just fall back to the default "Guest" values
                    });
        }
    }

    private void calculateTotals() {
        if (CartManager.getInstance().getCartItems().isEmpty()) {
            rvCartItems.setVisibility(View.GONE);
            checkoutPanel.setVisibility(View.GONE);
            layoutEmptyCart.setVisibility(View.VISIBLE);
            return;
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            checkoutPanel.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
        }

        double subtotal = CartManager.getInstance().getSubtotal();
        double taxes = subtotal * taxRate;
        currentFinalTotal = (subtotal + taxes) - discount;

        if (currentFinalTotal < 0) currentFinalTotal = 0;

        tvSubtotal.setText(String.format("LKR %.2f", subtotal));
        tvTaxes.setText(String.format("LKR %.2f", taxes));
        tvTotal.setText(String.format("LKR %.2f", currentFinalTotal));
    }

    // ==========================================
    // PAYHERE CHECKOUT LOGIC
    // ==========================================
    private void startPayHereCheckout() {
        String orderId = "ORDER_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String currency = "LKR";
        currentOrderId = "ORDER_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        InitRequest req = new InitRequest();
        req.setMerchantId(MERCHANT_ID);
        req.setMerchantSecret(MERCHANT_SECRET); // <-- NEW: Just pass the secret directly!
        req.setCurrency(currency);
        req.setAmount(currentFinalTotal);
        req.setOrderId(orderId);
        req.setItemsDescription("Casio Cafe App Order");
        req.setCustom1("In-App Purchase");
        req.setCustom2("");
        req.setOrderId(currentOrderId);

        // Pass the Dynamic Firebase Data to PayHere
        req.getCustomer().setFirstName(customerFirstName);
        req.getCustomer().setLastName(customerLastName);
        req.getCustomer().setEmail(customerEmail);
        req.getCustomer().setPhone(customerPhone);

        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // (We completely removed the manual hash generation code here)

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        payHereLauncher.launch(intent);
    }

}