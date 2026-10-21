package com.athulya.casiocafe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentSuccessActivity extends AppCompatActivity {

    private String orderId;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_success);

        View rootView = findViewById(R.id.main_payment_success);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        // 1. Get Data Passed from CartActivity
        orderId = getIntent().getStringExtra("ORDER_ID");
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);

        // 2. Bind Views & Set Text
        TextView tvOrderId = findViewById(R.id.tv_success_order_id);
        TextView tvAmount = findViewById(R.id.tv_success_amount);

        tvOrderId.setText(orderId != null ? orderId : "ORDER_UNKNOWN");
        tvAmount.setText(String.format("LKR %.2f", totalAmount));

        // 3. Setup Buttons
        findViewById(R.id.btn_download_receipt).setOnClickListener(v -> generatePDFReceipt());

        findViewById(R.id.btn_track_order).setOnClickListener(v -> {
            startActivity(new Intent(PaymentSuccessActivity.this, OrderStatusActivity.class));
            finish();
        });

        findViewById(R.id.btn_go_profile).setOnClickListener(v -> {
            startActivity(new Intent(PaymentSuccessActivity.this, ProfileActivity.class));
            finish();
        });
    }

    // ==========================================
    // BRANDED PDF GENERATION LOGIC
    // ==========================================
    private void generatePDFReceipt() {
        PdfDocument pdfDocument = new PdfDocument();

        // Define our App Colors
        int primaryColor = Color.parseColor("#885A3A"); // Dark Brown
        int secondaryColor = Color.parseColor("#A77D54"); // Light Brown
        int accentColor = Color.parseColor("#F5EDE6"); // Very Light Brown / Beige

        // Setup Paints
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint dividerPaint = new Paint();

        // Page Specs (Standard Receipt Size)
        int pageWidth = 400;
        int pageHeight = 600;
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(myPageInfo);
        Canvas canvas = myPage.getCanvas();

        // 1. Draw Background Header Accent (Optional: light box at the top)
        paint.setColor(accentColor);
        canvas.drawRect(0, 0, pageWidth, 150, paint);

        // 2. Draw Logo (Convert vector to bitmap to draw safely on PDF)
        Drawable logoDrawable = ContextCompat.getDrawable(this, R.drawable.ic_store);
        if (logoDrawable != null) {
            Bitmap logoBitmap = getBitmapFromDrawable(logoDrawable);
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logoBitmap, 60, 60, false);
            // Center the logo
            canvas.drawBitmap(scaledLogo, 170, 20, paint);
        }

        // 3. Draw Brand Name
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(24f);
        titlePaint.setColor(primaryColor);
        canvas.drawText("CASIO CAFE", pageWidth / 2f, 110, titlePaint);

        titlePaint.setTextSize(14f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        titlePaint.setColor(secondaryColor);
        canvas.drawText("Official Payment Receipt", pageWidth / 2f, 135, titlePaint);

        // 4. Draw Divider Line
        dividerPaint.setColor(accentColor);
        dividerPaint.setStrokeWidth(2f);
        canvas.drawLine(40, 160, 360, 160, dividerPaint);

        // 5. Draw Order Details
        paint.setColor(secondaryColor);
        paint.setTextSize(14f);
        paint.setTextAlign(Paint.Align.LEFT);

        // Labels (Left aligned)
        canvas.drawText("Order ID:", 40, 200, paint);
        canvas.drawText("Date:", 40, 230, paint);
        canvas.drawText("Payment Method:", 40, 260, paint);

        // Values (Right aligned to look clean)
        paint.setColor(primaryColor);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        canvas.drawText(orderId != null ? orderId : "UNKNOWN", 360, 200, paint);

        String currentDate = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(new Date());
        canvas.drawText(currentDate, 360, 230, paint);

        canvas.drawText("PayHere Secured", 360, 260, paint);

        // 6. Draw Second Divider
        canvas.drawLine(40, 290, 360, 290, dividerPaint);

        // 7. Draw Totals
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(secondaryColor);
        paint.setTextSize(18f);
        canvas.drawText("Total Paid", 40, 340, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(primaryColor);
        paint.setTextSize(22f);
        canvas.drawText(String.format("LKR %.2f", totalAmount), 360, 340, paint);

        // 8. Draw Footer
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(secondaryColor);
        titlePaint.setTextSize(14f);
        canvas.drawText("Thank you for choosing Casio Cafe!", pageWidth / 2f, 520, titlePaint);
        canvas.drawText("Contact: support@casiocafe.com", pageWidth / 2f, 545, titlePaint);

        // 9. Finish the Page
        pdfDocument.finishPage(myPage);

        // 10. Save to the Phone's "Downloads" Folder
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String safeOrderId = orderId != null ? orderId : "UNKNOWN";
            File file = new File(downloadsDir, "CasioCafe_Receipt_" + safeOrderId + ".pdf");

            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Receipt saved to Downloads folder!", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }

    // Helper method to safely convert Android Vector Icons to Bitmaps for the PDF Canvas
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

        // Tint the logo to match your brand color
        drawable.setTint(Color.parseColor("#885A3A"));
        drawable.draw(canvas);
        return bitmap;
    }
}