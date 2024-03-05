package com.example.foodyz.personal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompleteOrderFragment extends Fragment {

    private List<String> selectedProducts;
    private String businessId;
    private Double totalCost;
    private String orderId;
    private LinearLayout completeOrderLinearLayout;
    private TextView totalTextView;

    public CompleteOrderFragment() {
        // Required empty public constructor
    }

    public static CompleteOrderFragment newInstance(String businessId, List<String> selectedProducts) {
        CompleteOrderFragment fragment = new CompleteOrderFragment();
        Bundle args = new Bundle();
        args.putString("businessId", businessId);
        args.putStringArrayList("selectedProducts", (ArrayList<String>) selectedProducts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            businessId = getArguments().getString("businessId");
            selectedProducts = getArguments().getStringArrayList("selectedProducts");
        }
        totalCost = 0.0; // Initialize totalCost
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_complete_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        completeOrderLinearLayout = view.findViewById(R.id.completeOrderLinearLayout);
        totalTextView = view.findViewById(R.id.totalTextView); // Initialize total TextView

        // Create a new document in the "orders" collection and get its ID
        createOrderDocument();

        // Add grey border and space below the text views
        addGreyBorderAndSpace();

        // Center the text views at the top in bright cyan
        centerTextViews();


    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(requireContext(), Personal_MainActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Close the current activity
    }


    private void addGreyBorderAndSpace() {
        // Create a grey border view
        View borderView = new View(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2 // Border height
        );
        params.setMargins(0, 20, 0, 20); // Add space above and below the border
        borderView.setLayoutParams(params);
        borderView.setBackgroundColor(Color.GRAY);

        // Add the border view to the LinearLayout
        completeOrderLinearLayout.addView(borderView);
    }

    private void centerTextViews() {
        // Center the text views at the top in bright cyan
        totalTextView.setTextColor(Color.CYAN);
        totalTextView.setGravity(Gravity.CENTER);
    }


    private void createOrderDocument() {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add order details to "orders" collection
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("business-id", businessId);
        orderData.put("date", FieldValue.serverTimestamp()); // Add current timestamp
        orderData.put("status", "Not Received");
        orderData.put("total-cost", 0.0); // Initially set total cost to 0
        orderData.put("personal-id", FirebaseAuth.getInstance().getCurrentUser().getUid()); // Set personal-id to the current user's UID

        // Add a new document to the "orders" collection
        db.collection("orders").add(orderData)
                .addOnSuccessListener(documentReference -> {
                    orderId = documentReference.getId(); // Set orderId to the ID of the created document
                    processProducts(); // Start processing the products after orderId is available
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(requireContext(), "Failed to create order document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void processProducts() {
        if (selectedProducts != null && !selectedProducts.isEmpty()) {
            // Calculate quantities and total cost
            Map<String, Integer> quantities = new HashMap<>();

            // Count the occurrences of each product
            for (String productName : selectedProducts) {
                quantities.put(productName, quantities.getOrDefault(productName, 0) + 1);
            }

            // Query Firestore for product prices
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference menuCollection = db.collection("business-menu");

            // Process each unique product
            for (String productName : quantities.keySet()) {
                int quantity = quantities.get(productName);

                // Query Firestore to get product price
                menuCollection.whereEqualTo("business-id", businessId)
                        .whereEqualTo("product-name", productName)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    double unitPrice = document.getDouble("product-price");

                                    // Create a TextView for the product with its details
                                    createTextViewWithProductDetails(productName, quantity, unitPrice);

                                    // Save order details to Firestore
                                    saveOrderDetailsToFirestore(productName, quantity, unitPrice);

                                    // Update total cost
                                    totalCost += unitPrice * quantity;
                                    totalTextView.setText("Total: " + totalCost);
                                    // Update total cost in Firestore document
                                    updateTotalCostInFirestore(totalCost);
                                }

                            } else {
                                // Handle errors
                                Exception exception = task.getException();
                                if (exception != null) {
                                    exception.printStackTrace();
                                }
                            }
                        });
            }
            createAcceptButton();
        } else {
            // Display a message prompt if no products are selected
            TextView noOrderTextView = new TextView(requireContext());
            noOrderTextView.setText("You ordered nothing");
            completeOrderLinearLayout.addView(noOrderTextView);
            totalTextView.setVisibility(View.GONE); // Hide total cost TextView
        }
    }


    private void createTextViewWithProductDetails(String productName, int quantity, double unitPrice) {
        // Create TextViews for each variable
        TextView productTextView = createColoredTextView("Product: " + productName, Color.WHITE, 24);
        TextView quantityTextView = createColoredTextView("Quantity: " + quantity, Color.GRAY, 18);
        TextView unitPriceTextView = createColoredTextView("Unit Price: " + String.format("%.2f", unitPrice), Color.GRAY, 18);

        // Calculate total cost for this product
        double totalCost = unitPrice * quantity;
        TextView totalCostLabelTextView = createColoredTextView("Total Cost: ", Color.CYAN, 24);
        TextView totalCostValueTextView = createColoredTextView(String.format("%.2f", totalCost), Color.CYAN, 24);

        // Set gravity to center
        productTextView.setGravity(Gravity.CENTER);
        quantityTextView.setGravity(Gravity.CENTER);
        unitPriceTextView.setGravity(Gravity.CENTER);
        totalCostLabelTextView.setGravity(Gravity.CENTER);
        totalCostValueTextView.setGravity(Gravity.CENTER);

        // Add TextViews to the LinearLayout
        completeOrderLinearLayout.addView(productTextView);
        completeOrderLinearLayout.addView(quantityTextView);
        completeOrderLinearLayout.addView(unitPriceTextView);
        completeOrderLinearLayout.addView(totalCostLabelTextView);
        completeOrderLinearLayout.addView(totalCostValueTextView);

        addGreyBorderAndSpace();
    }

    private TextView createColoredTextView(String text, int color, int textSize) {
        TextView textView = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20); // Add margin bottom
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        return textView;
    }






    private void saveOrderDetailsToFirestore(String productName, int quantity, double unitPrice) {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add order details to "order-details" collection
        Map<String, Object> orderDetailsData = new HashMap<>();
        orderDetailsData.put("order-id", orderId); // Set order-id to the value of orderId
        orderDetailsData.put("product-name", productName);
        orderDetailsData.put("quantity", quantity);
        orderDetailsData.put("unit-price", unitPrice);

        // Add a new document to the "order-details" collection
        db.collection("order-details").add(orderDetailsData)
                .addOnSuccessListener(documentReference -> {
                    // Successfully saved order details
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(requireContext(), "Failed to save order details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTotalCostInFirestore(double totalCost) {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update total cost in "orders" collection
        db.collection("orders").document(orderId)
                .update("total-cost", totalCost)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated total cost
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(requireContext(), "Failed to update total cost: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createAcceptButton() {
        // Create the button
        Button acceptButton = new Button(requireContext());
        acceptButton.setText("Accept");

        // Set text appearance attributes
        acceptButton.setAllCaps(false);
        acceptButton.setTextColor(Color.BLACK);
        acceptButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        // Set background color to bright cyan
        acceptButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bright_cyan));

        // Set corner radius
        int cornerRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        acceptButton.setBackgroundResource(R.drawable.rectangle_with_rounded_corners);

        // Set layout parameters for the button
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM); // Align the button to the bottom of the parent layout
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL); // Center the button horizontally
        layoutParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.space_above_button)); // Space above the button
        acceptButton.setLayoutParams(layoutParams);

        // Set OnClickListener to navigate to Personal_MainActivity
        acceptButton.setOnClickListener(v -> navigateToMainActivity());

        // Add the button to the parent layout
        completeOrderLinearLayout.addView(acceptButton); // Assuming completeOrderLinearLayout is the parent layout
    }






}
