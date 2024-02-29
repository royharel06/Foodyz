package com.example.foodyz.personal;

import android.app.AlertDialog;
import android.content.Intent;
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

    public static OrderDetailsFragment newInstance(String orderId,String businessId, String businessName) {
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
        rateButton.setOnClickListener(v -> showRatedMessage());
    }

    private boolean alreadyRated = false; // Boolean variable to track if the message box has been generated

    private void showRatedMessage() {
        if (!alreadyRated) {
            // Query the ratings collection to get the user's rating
            db.collection("ratings")
                    .whereEqualTo("business-id", businessId)
                    .whereEqualTo("personal-id", personalId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Get the rating from the document
                            long rating = task.getResult().getDocuments().get(0).getLong("rating");

                            // Display the rated message
                            String message = "You already rated " + businessName + " with " + rating + " stars";
                            TextView ratedTextView = new TextView(requireContext());
                            ratedTextView.setText(message);

                            // Set text view properties
                            ratedTextView.setGravity(Gravity.CENTER);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin), 0, 0);
                            ratedTextView.setLayoutParams(params);

                            // Add the TextView to the layout
                            ((LinearLayout) requireView()).addView(ratedTextView);

                            // Create and add the Update button
                            Button updateButton = new Button(requireContext());
                            updateButton.setText("Update " + businessName + " rating");
                            updateButton.setTextColor(getResources().getColor(android.R.color.white)); // Apply text color
                            updateButton.setPadding(16, 8, 16, 8); // Apply padding
                            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            buttonParams.gravity = Gravity.CENTER; // Center the button
                            buttonParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin), 0, 0); // Add top margin
                            updateButton.setLayoutParams(buttonParams); // Apply layout parameters
                            updateButton.setOnClickListener(v -> showUpdateRatingDialog());
                            ((LinearLayout) requireView()).addView(updateButton);

                            // Set alreadyRated to true to prevent multiple messages
                            alreadyRated = true;
                        }
                    });
        }
    }


    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rate This Business");

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

    private void showUpdateRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Rating for " + businessName);

        // Create a RatingBar programmatically
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        builder.setPositiveButton("Submit", (dialog, which) -> {
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
