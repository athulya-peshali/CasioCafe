package com.athulya.casiocafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private Context context;
    private ArrayList<OrderModel> orderList;
    private FirebaseFirestore db;

    public AdminOrderAdapter(Context context, ArrayList<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.tvOrderId.setText(order.getOrderId());
        holder.tvCustomer.setText("Customer: " + order.getCustomerName());
        holder.tvItems.setText(order.getItemsDescription());
        holder.tvPrice.setText(String.format("LKR %.2f", order.getTotalAmount()));
        holder.btnStatus.setText(order.getStatus() + " ▼");

        // Click listener to change the status of the order in the live database!
        holder.btnStatus.setOnClickListener(v -> {
            String[] statuses = {"Pending", "Preparing", "Ready for Pickup", "Delivered"};
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Update Order Status");
            builder.setItems(statuses, (dialog, which) -> {
                String selectedStatus = statuses[which];

                // Update Firebase
                db.collection("Orders").document(order.getDocumentId())
                        .update("status", selectedStatus)
                        .addOnSuccessListener(aVoid -> {
                            holder.btnStatus.setText(selectedStatus + " ▼");
                            Toast.makeText(context, "Status Updated!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show());
            });
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomer, tvItems, tvPrice;
        MaterialButton btnStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_admin_order_id);
            tvCustomer = itemView.findViewById(R.id.tv_admin_customer_name);
            tvItems = itemView.findViewById(R.id.tv_admin_order_items);
            tvPrice = itemView.findViewById(R.id.tv_admin_order_price);
            btnStatus = itemView.findViewById(R.id.btn_admin_change_status);
        }
    }
}