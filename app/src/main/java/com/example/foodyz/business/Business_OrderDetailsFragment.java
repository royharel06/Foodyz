package com.example.foodyz.business;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Business_OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "orderId";
    private static final String ARG_CUSTOMER_NAME = "customerName";
    private static final String ARG_CUSTOMER_ID = "customerId";
    private String orderId;
    private String customerId;
    private String customerName;
    private String businessId;
    private LinearLayout orderDetailsLayout;
    private FirebaseFirestore db;
    private TextView orderDetailsTextView;

    private double totalCost;

    public static Business_OrderDetailsFragment newInstance(String orderId, String customerId, String customerName) {
        Business_OrderDetailsFragment fragment = new Business_OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        args.putString(ARG_CUSTOMER_ID, customerId);
        args.putString(ARG_CUSTOMER_NAME, customerName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
            customerName = getArguments().getString(ARG_CUSTOMER_NAME);
            customerId = getArguments().getString(ARG_CUSTOMER_ID);
        }
        // Set personalId using Firebase Auth
        businessId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_order_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderDetailsLayout = view.findViewById(R.id.item_scroll);
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

                            // Create TextViews with product details
                            createTextViewWithProductDetails(productName, quantity, unitPrice);
                        }
                    }
                });

        Spinner spinner = view.findViewById(R.id.drop_down);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.dropdown_items, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item); // Use custom layout for dropdown view as well
        spinner.setAdapter(adapter);

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String status = document.getString("status");

                            if (status.equals("Pending"))
                                spinner.setSelection(0);
                            else if (status.equals("Ready"))
                                spinner.setSelection(1);
                            else if (status.equals("Received"))
                                spinner.setSelection(2);
                        }
                    }
                });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String status = parent.getItemAtPosition(position).toString();

                db.collection("orders")
                        .document(orderId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Update the document with the new status
                                    db.collection("orders")
                                            .document(document.getId())
                                            .update("status", status)
                                            .addOnSuccessListener(aVoid -> Log.d("UpdateStatus", "DocumentSnapshot successfully updated!"))
                                            .addOnFailureListener(e -> Log.w("UpdateStatus", "Error updating document", e));
                                }
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void createTextViewWithProductDetails(String productName, int quantity, double unitPrice) {
        // Create TextViews for each variable
        TextView productTextView = createColoredTextView("Product: " + productName, Color.WHITE, 24);
        TextView quantityTextView = createColoredTextView("Quantity: " + quantity, Color.GRAY, 18);
        TextView unitPriceTextView = createColoredTextView("Unit Price: " + String.format("%.2f", unitPrice), Color.GRAY, 18);

        // Calculate total cost for this product
        double productCost = unitPrice * quantity;
        totalCost += productCost; // Accumulate total cost
        TextView totalCostValueTextView = createColoredTextView(String.format("Cost: %.2f \n", productCost), Color.CYAN, 24);

        // Set gravity to center
        productTextView.setGravity(Gravity.CENTER);
        quantityTextView.setGravity(Gravity.CENTER);
        unitPriceTextView.setGravity(Gravity.CENTER);
        totalCostValueTextView.setGravity(Gravity.CENTER);

        // Add TextViews to the LinearLayout
        orderDetailsLayout.addView(productTextView);
        orderDetailsLayout.addView(quantityTextView);
        orderDetailsLayout.addView(unitPriceTextView);
        orderDetailsLayout.addView(totalCostValueTextView);

        addGreyBorderAndSpace();
    }

    private TextView createColoredTextView(String text, int color, int textSize) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(color);
        textView.setTextSize(textSize);
        return textView;
    }

    private void addGreyBorderAndSpace() {
        // Add a gray border below the product details
        View border = new View(requireContext());
        LinearLayout.LayoutParams borderParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        borderParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin), 0, 0);
        border.setLayoutParams(borderParams);
        border.setBackgroundColor(Color.GRAY);
        orderDetailsLayout.addView(border);

        // Add space below the border
        View space = new View(requireContext());
        LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
        space.setLayoutParams(spaceParams);
        orderDetailsLayout.addView(space);
    }
}