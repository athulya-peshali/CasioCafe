package com.athulya.casiocafe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;

public class AddProductActivity extends AppCompatActivity {

    private ArrayList<String> categoryList = new ArrayList<>(Arrays.asList("Hot Coffee", "Cold Coffee", "Pastries"));
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    private Uri imageUri;
    private ImageView ivPreviewImage;
    private MaterialButton btnSaveProduct;

    // Launcher to pick image from gallery
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivPreviewImage.setImageURI(imageUri);
                    ivPreviewImage.setVisibility(View.VISIBLE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        db = FirebaseFirestore.getInstance();
        ivPreviewImage = findViewById(R.id.iv_preview_image);
        btnSaveProduct = findViewById(R.id.btn_save_product);

        View rootView = findViewById(R.id.main_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        AutoCompleteTextView dropdownCategory = findViewById(R.id.dropdown_category);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryList);
        dropdownCategory.setAdapter(adapter);
        dropdownCategory.setOnClickListener(v -> dropdownCategory.showDropDown());

        findViewById(R.id.btn_add_category).setOnClickListener(v -> showAddCategoryDialog(dropdownCategory));
        findViewById(R.id.btn_back_add).setOnClickListener(v -> finish());

        // Open Gallery
        findViewById(R.id.cv_upload_image).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });

        // Save Button
        btnSaveProduct.setOnClickListener(v -> uploadData(dropdownCategory));
    }

    private void uploadData(AutoCompleteTextView dropdownCategory) {
        EditText etName = findViewById(R.id.et_product_name);
        EditText etPrice = findViewById(R.id.et_product_price);
        EditText etDesc = findViewById(R.id.et_product_desc);

        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = dropdownCategory.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        String uniqueID = UUID.randomUUID().toString();

        btnSaveProduct.setEnabled(false);
        btnSaveProduct.setText("Uploading...");

        if (imageUri != null) {
            uploadImageToImgBBAndSave(uniqueID, name, price, category, desc);
        } else {
            saveProductToFirestore(uniqueID, name, price, category, desc, "");
        }
    }

    private void uploadImageToImgBBAndSave(String id, String name, double price, String category, String desc) {
        // Run network task in background to prevent app freeze
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 1. Convert Image to Base64
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = imageStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                // 2. Setup HTTP POST to ImgBB
                String API_KEY = "dd88074a869feca844dbf925be096d1e"; // <--- PUT YOUR KEY HERE
                URL url = new URL("https://api.imgbb.com/1/upload?key=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 3. Send Data
                String data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(base64Image, "UTF-8");
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // 4. Read Response
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // 5. Extract URL from JSON
                JSONObject jsonObject = new JSONObject(response.toString());
                String imageUrl = jsonObject.getJSONObject("data").getString("url");

                // 6. Go back to Main Thread to save to Firestore
                runOnUiThread(() -> saveProductToFirestore(id, name, price, category, desc, imageUrl));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AddProductActivity.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
                    btnSaveProduct.setEnabled(true);
                    btnSaveProduct.setText("Publish to Menu");
                });
            }
        });
    }

    private void saveProductToFirestore(String id, String name, double price, String category, String desc, String imageUrl) {
        Product newProduct = new Product(id, name, price, category, desc, imageUrl);

        db.collection("Products").document(id).set(newProduct)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product Published Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSaveProduct.setEnabled(true);
                    btnSaveProduct.setText("Publish to Menu");
                });
    }

    private void showAddCategoryDialog(AutoCompleteTextView dropdown) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");
        final EditText input = new EditText(this);
        input.setHint("e.g. Seasonal Drinks");
        builder.setView(input);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String newCategory = input.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                categoryList.add(newCategory);
                adapter.notifyDataSetChanged();
                dropdown.setText(newCategory, false);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}