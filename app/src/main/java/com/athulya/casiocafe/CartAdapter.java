package com.athulya.casiocafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private ArrayList<CartItem> cartList;
    private Runnable onCartUpdated; // Used to tell CartActivity to recalculate the Total Price

    public CartAdapter(Context context, ArrayList<CartItem> cartList, Runnable onCartUpdated) {
        this.context = context;
        this.cartList = cartList;
        this.onCartUpdated = onCartUpdated;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.tvName.setText(item.getProduct().getName());
        holder.tvPrice.setText(String.format("LKR %.2f", item.getTotalPrice()));
        holder.tvQty.setText(String.valueOf(item.getQuantity()));

        // Load the image using Glide
        if (item.getProduct().getImageUrl() != null && !item.getProduct().getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getProduct().getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_store)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_store);
        }

        // Increase Quantity Button
        holder.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position); // Refresh just this row
            onCartUpdated.run();         // Tell CartActivity to recalculate the subtotal
        });

        // Decrease Quantity or Remove Button
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
            } else {
                // If it hits 0, remove it from the list completely
                CartManager.getInstance().removeItem(item);
                notifyItemRemoved(position);
            }
            onCartUpdated.run();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvQty;
        ImageButton btnPlus, btnMinus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_cart_image);
            tvName = itemView.findViewById(R.id.tv_cart_name);
            tvPrice = itemView.findViewById(R.id.tv_cart_price);
            tvQty = itemView.findViewById(R.id.tv_cart_qty);
            btnPlus = itemView.findViewById(R.id.btn_cart_plus);
            btnMinus = itemView.findViewById(R.id.btn_cart_minus);
        }
    }
}