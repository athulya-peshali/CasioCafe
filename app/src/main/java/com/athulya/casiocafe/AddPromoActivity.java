package com.athulya.casiocafe;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;

public class AddPromoActivity extends AppCompatActivity {

    // Options for where the promo applies
    private ArrayList<String> applyToList = new ArrayList<>(Arrays.asList("All Products", "Hot Coffee", "Cold Coffee", "Pastries"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_promo);

        View rootView = findViewById(R.id.btn_save_promo).getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Setup Back Button
        findViewById(R.id.btn_back_promo).setOnClickListener(v -> finish());

        // Setup Dropdown
        AutoCompleteTextView dropdownCategory = findViewById(R.id.dropdown_promo_category);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, applyToList);
        dropdownCategory.setAdapter(adapter);
        dropdownCategory.setOnClickListener(v -> dropdownCategory.showDropDown());

        // Save Logic
        findViewById(R.id.btn_save_promo).setOnClickListener(v -> {
            // In a real app, you would read the ToggleGroup and EditTexts here to save to Firebase/SQLite
            Toast.makeText(this, "Promotion Activated Successfully!", Toast.LENGTH_LONG).show();
            finish(); // Return to dashboard
        });
    }
}