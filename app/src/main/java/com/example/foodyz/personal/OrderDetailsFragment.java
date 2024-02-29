package com.example.foodyz.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "orderId";
    private static final String ARG_BUSINESS_NAME = "businessName"; // Add constant for business name
    private String orderId;
    private String businessName; // Declare businessName variable
    private LinearLayout orderDetailsLayout;
    private FirebaseFirestore db;
    private TextView orderDetailsTextView;

    public static OrderDetailsFragment newInstance(String orderId, String businessName) { // Update newInstance method
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        args.putString(ARG_BUSINESS_NAME, businessName); // Pass business name to fragment
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
            businessName = getArguments().getString(ARG_BUSINESS_NAME); // Retrieve business name from arguments
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

        // Initialize orderDetailsTextView
        orderDetailsTextView = view.findViewById(R.id.orderDetailsTextView);

        db = FirebaseFirestore.getInstance();

        // Query the database for order details
        db.collection("order-details")
                .whereEqualTo("order-id", orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("product-name");
                            int quantity = document.getLong("quantity").intValue();
                            double unitPrice = document.getDouble("unit-price");

                            String orderDetailText = String.format("Product: %s\nQuantity: %d\nUnit Price: %.2f\n", productName, quantity, unitPrice);
                            // Append the order details to the TextView
                            orderDetailsTextView.append(orderDetailText);
                        }
                    }
                });

        // Button to rate the business
        Button rateButton = view.findViewById(R.id.rateButton);
        rateButton.setText("Rate " + businessName); // Update button text with business name
        rateButton.setOnClickListener(v -> {
            // Handle button click to rate the business
            // You can implement the rating functionality here
        });
    }
}
