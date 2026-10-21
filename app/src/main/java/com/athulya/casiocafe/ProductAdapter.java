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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private ArrayList<Product> productList;

    public ProductAdapter(Context context, ArrayList<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This grabs your item_product_card.xml blueprint
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // This stamps the data onto the blueprint
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());
        // Format price to have 2 decimal places
        holder.tvPrice.setText(String.format("LKR %.2f", product.getPrice()));

        // Use Glide to load the ImgBB web URL into the ImageView!
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_store) // Shows while loading
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_store); // Fallback
        }

        // Handle Add to Cart Click (Quick Add from Shop)
        holder.btnAdd.setOnClickListener(v -> {
            CartManager.getInstance().addItem(product, 1);
            Toast.makeText(context, product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
        });

        // Handle clicking the whole card (to go to details)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId()); // Pass the unique Firebase ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size(); // Tells the conveyor belt how many items to build
    }

    // This class links the XML IDs to Java variables
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvCategory, tvPrice;
        ImageButton btnAdd;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvCategory = itemView.findViewById(R.id.tv_product_category);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            btnAdd = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}