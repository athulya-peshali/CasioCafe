package com.athulya.casiocafe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    private EditText etName, etPrice, etCategory, etDescription;
    private ImageView ivProductImage;
    private MaterialButton btnSave;

    private String productId;
    private String currentImageUrl;
    private Uri newImageUri = null;

    private FirebaseFirestore db;

    // Photo Picker
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    newImageUri = uri;
                    ivProductImage.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        db = FirebaseFirestore.getInstance();

        // 1. Matched the EXACT IDs from your XML!
        etName = findViewById(R.id.et_product_name);
        etPrice = findViewById(R.id.et_product_price);
        etCategory = findViewById(R.id.dropdown_category);
        etDescription = findViewById(R.id.et_product_desc);
        ivProductImage = findViewById(R.id.iv_preview_image);
        btnSave = findViewById(R.id.btn_save_product);

        btnSave.setText("Update Product");

        Intent intent = getIntent();
        productId = intent.getStringExtra("PRODUCT_ID");
        etName.setText(intent.getStringExtra("PRODUCT_NAME"));
        etPrice.setText(String.valueOf(intent.getDoubleExtra("PRODUCT_PRICE", 0.0)));
        etCategory.setText(intent.getStringExtra("PRODUCT_CATEGORY"));
        etDescription.setText(intent.getStringExtra("PRODUCT_DESC"));
        currentImageUrl = intent.getStringExtra("PRODUCT_IMAGE_URL");

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            ivProductImage.setVisibility(View.VISIBLE); // Make sure the image view is visible!
            Glide.with(this).load(currentImageUrl).into(ivProductImage);
        }

        // 2. Click the card to upload an image, and use the correct back button ID
        findViewById(R.id.cv_upload_image).setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        findViewById(R.id.btn_back_add).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> updateProduct());
    }

    private void updateProduct() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        btnSave.setEnabled(false);
        btnSave.setText("Updating...");

        if (newImageUri != null) {
            // They selected a new image! Upload it to ImgBB first.
            String base64Image = getBase64FromUri(newImageUri);
            uploadImageToImgBB(base64Image, name, price, category, desc);
        } else {
            // Keep the old image URL
            saveToFirestore(name, price, category, desc, currentImageUrl);
        }
    }

    private void saveToFirestore(String name, double price, String category, String desc, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("price", price);
        updates.put("category", category);
        updates.put("description", desc);
        updates.put("imageUrl", imageUrl);

        db.collection("Products").document(productId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating product", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Update Product");
                });
    }

    // --- IMGBB HELPER METHODS (Same as AddProductActivity) ---
    private String getBase64FromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadImageToImgBB(String base64Image, String name, double price, String category, String desc) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String apiKey = "dd88074a869feca844dbf925be096d1e"; // <-- PASTE KEY HERE
                URL url = new URL("https://api.imgbb.com/1/upload?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(base64Image, "UTF-8");

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(data);
                writer.flush();
                writer.close();

                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String newImageUrl = jsonObject.getJSONObject("data").getString("url");

                    // Save to Firestore with the NEW image URL
                    runOnUiThread(() -> saveToFirestore(name, price, category, desc, newImageUrl));
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                        btnSave.setText("Update Product");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}