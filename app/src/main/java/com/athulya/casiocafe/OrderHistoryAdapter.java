package com.athulya.casiocafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.HistoryViewHolder> {

    private Context context;
    private ArrayList<OrderModel> orderList;

    public OrderHistoryAdapter(Context context, ArrayList<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.tvOrderId.setText(order.getOrderId());
        holder.tvStatus.setText(order.getStatus());
        holder.tvItems.setText(order.getItemsDescription());
        holder.tvTotal.setText(String.format("Total: LKR %.2f", order.getTotalAmount()));

        // Convert the timestamp to a nice date (e.g. "Oct 12, 2023")
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateString = sdf.format(new Date(order.getTimestamp()));
        holder.tvDate.setText(dateString);

        // Optional: Change color based on status!
        if (order.getStatus().equals("Completed") || order.getStatus().equals("Delivered")) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#A77D54")); // Default Brown
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvItems, tvDate, tvTotal;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_history_order_id);
            tvStatus = itemView.findViewById(R.id.tv_history_status);
            tvItems = itemView.findViewById(R.id.tv_history_items);
            tvDate = itemView.findViewById(R.id.tv_history_date);
            tvTotal = itemView.findViewById(R.id.tv_history_total);
        }
    }
}