package com.example.foodyz.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "orderId";
    private String orderId;
    private TextView orderDetailsTextView;
    private FirebaseFirestore db;

    public static OrderDetailsFragment newInstance(String orderId) {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderDetailsTextView = view.findViewById(R.id.orderDetailsTextView);
        Button rateButton = view.findViewById(R.id.rateButton);

        db = FirebaseFirestore.getInstance();

        // Query the database for order details
        db.collection("order-details")
                .whereEqualTo("order-id", orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder orderDetailsBuilder = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("product-name");
                            int quantity = document.getLong("quantity").intValue();
                            double unitPrice = document.getDouble("unit-price");

                            orderDetailsBuilder.append("Product: ").append(productName)
                                    .append("\nQuantity: ").append(quantity)
                                    .append("\nUnit Price: ").append(unitPrice).append("\n\n");
                        }
                        // Set the order details text to the TextView
                        orderDetailsTextView.setText(orderDetailsBuilder.toString());
                    }
                });

        // Button to rate the business
        rateButton.setOnClickListener(v -> {
            // Handle button click to rate the business
            // You can implement the rating functionality here
        });
    }
}
