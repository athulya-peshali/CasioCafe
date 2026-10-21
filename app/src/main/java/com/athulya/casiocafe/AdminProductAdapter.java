package com.athulya.casiocafe;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminViewHolder> {

    private Context context;
    private ArrayList<Product> productList;

    public AdminProductAdapter(Context context, ArrayList<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(String.format("LKR %.2f", product.getPrice()));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_store)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_store);
        }

        // 1. EDIT BUTTON LOGIC
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditProductActivity.class);
            // Pass all the product details over to the Edit Screen so it can pre-fill the form
            intent.putExtra("PRODUCT_ID", product.getId());
            intent.putExtra("PRODUCT_NAME", product.getName());
            intent.putExtra("PRODUCT_PRICE", product.getPrice());
            intent.putExtra("PRODUCT_DESC", product.getDescription());
            intent.putExtra("PRODUCT_CATEGORY", product.getCategory());
            intent.putExtra("PRODUCT_IMAGE_URL", product.getImageUrl());
            context.startActivity(intent);
        });

        // 2. DELETE BUTTON LOGIC
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to permanently delete " + product.getName() + "?")
                    .setPositiveButton("Yes, Delete", (dialog, which) -> {
                        // Delete from Firestore using the Document ID
                        FirebaseFirestore.getInstance().collection("Products")
                                .document(product.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show();
                                    // Remove from the list visually and update the RecyclerView
                                    productList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, productList.size());
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Error deleting product", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice;
        ImageButton btnEdit, btnDelete;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_admin_image);
            tvName = itemView.findViewById(R.id.tv_admin_name);
            tvPrice = itemView.findViewById(R.id.tv_admin_price);
            btnEdit = itemView.findViewById(R.id.btn_admin_edit);
            btnDelete = itemView.findViewById(R.id.btn_admin_delete);
        }
    }
}