package com.example.foodyz.personal;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.Rating;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class OrderDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "orderId";
    private static final String ARG_BUSINESS_NAME = "businessName";
    private static final String ARG_BUSINESS_ID = "businessId";
    private String orderId;
    private String businessId;
    private String businessName;
    private String personalId;
    private LinearLayout orderDetailsLayout;
    private FirebaseFirestore db;
    private TextView orderDetailsTextView;

    private double totalCost;

    public static OrderDetailsFragment newInstance(String orderId, String businessId, String businessName) {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        args.putString(ARG_BUSINESS_ID, businessId);
        args.putString(ARG_BUSINESS_NAME, businessName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
            businessName = getArguments().getString(ARG_BUSINESS_NAME);
            businessId = getArguments().getString(ARG_BUSINESS_ID);
        }
        // Set personalId using Firebase Auth
        personalId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderDetailsLayout = view.findViewById(R.id.completeOrderLinearLayout);
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
                        // After adding all products, update total cost TextView
                        updateTotalCostTextView();
                    }
                });

        // Button to rate the business
        Button rateButton = view.findViewById(R.id.rateButton);
        rateButton.setText("Rate " + businessName); // Update button text with business name
        rateButton.setOnClickListener(v -> showRatedMessage());
    }

    private void updateTotalCostTextView() {
        TextView totalTextView = requireView().findViewById(R.id.totalTextView);
        totalTextView.setText("Total: " + String.format("%.2f", totalCost));
    }


    private void showRatedMessage() {
        // Query the ratings collection to get the user's rating
        db.collection("ratings")
                .whereEqualTo("business-id", businessId)
                .whereEqualTo("personal-id", personalId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Get the rating from the document
                        long rating = task.getResult().getDocuments().get(0).getLong("rating");
                        // Display the rated message and update button
                        showUpdateRatingDialog(businessName, rating);

                    } else {
                            // User has not rated the business yet
                            showRatingDialog();

                    }
                });
    }


    // Helper method to create colored TextViews with product details
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


    // Helper method to create colored TextViews
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

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rate " + businessName);

        // Create a RatingBar programmatically
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            // Handle submit button click
            RatingBar ratingBar = dialogView.findViewById(R.id.ratingBarDialog);
            long rating = (long) ratingBar.getRating();
            // Add rating to Firestore
            addRatingToFirestore(rating);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Handle cancel button click
            dialog.dismiss();
        });

        builder.show();
    }


    private void showUpdateRatingDialog(String businessName, long rating) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("You already rated " + businessName + " with " + rating + " stars");

        // Create a RatingBar programmatically
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        builder.setPositiveButton("Update", (dialog, which) -> {
            // Handle submit button click
            RatingBar ratingBar = dialogView.findViewById(R.id.ratingBarDialog);
            long newRating = (long) ratingBar.getRating();
            // Update rating in Firestore
            updateRatingInFirestore(newRating);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Handle cancel button click
            dialog.dismiss();
        });

        builder.show();
    }

    private void addRatingToFirestore(long rating) {
        // Create a new document in "ratings" collection
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("business-id", businessId);
        ratingData.put("personal-id", personalId);
        ratingData.put("rating", rating);

        db.collection("ratings").add(ratingData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to main activity fragment after rating
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to submit rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRatingInFirestore(long newRating) {
        // Query the ratings collection to get the document ID of the existing rating
        db.collection("ratings")
                .whereEqualTo("business-id", businessId)
                .whereEqualTo("personal-id", personalId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Get the document ID of the existing rating
                        String ratingId = task.getResult().getDocuments().get(0).getId();
                        // Update the rating in Firestore
                        db.collection("ratings").document(ratingId)
                                .update("rating", newRating)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "Rating updated successfully", Toast.LENGTH_SHORT).show();
                                    // Navigate to main activity fragment after updating rating
                                    navigateToMainActivity();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to update rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private void navigateToMainActivity() {
        // Create an instance of Personal_MainActivity
        Intent intent = new Intent(requireContext(), Personal_MainActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Close the current activity
    }



}

