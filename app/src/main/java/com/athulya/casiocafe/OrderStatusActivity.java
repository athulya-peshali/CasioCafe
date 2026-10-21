package com.athulya.casiocafe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Import OSMDroid libraries
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class OrderStatusActivity extends AppCompatActivity {

    private MapView map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CRITICAL FOR OSMDROID: Load configuration BEFORE setContentView
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_status);

        View rootView = findViewById(R.id.main_order_status);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        // 1. Initialize the Map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK); // Standard OpenStreetMap style
        map.setMultiTouchControls(true); // Allow pinch to zoom

        // 2. Set the Map Center and Zoom
        IMapController mapController = map.getController();
        mapController.setZoom(16.0);
        GeoPoint cafeLocation = new GeoPoint(7.0840, 79.9939);
        mapController.setCenter(cafeLocation);

        // 3. Drop a Pin (Marker) on the Cafe
        Marker cafeMarker = new Marker(map);
        cafeMarker.setPosition(cafeLocation);
        cafeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        cafeMarker.setTitle("Casio Cafe");
        map.getOverlays().add(cafeMarker);

        // 4. Setup Buttons
        findViewById(R.id.btn_status_close).setOnClickListener(v -> finish());

        findViewById(R.id.btn_back_to_home).setOnClickListener(v -> {
            Intent intent = new Intent(OrderStatusActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // OSMDroid requires us to handle lifecycle events to save battery
    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}