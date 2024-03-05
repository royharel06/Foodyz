package com.example.foodyz.personal;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.foodyz.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchFragment extends Fragment {

    private LinearLayout yourLinearLayout;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();
        yourLinearLayout = rootView.findViewById(R.id.searchBusinessLinearLayout); // Change the ID to match your XML

        // Get the EditText for search input
        EditText searchEditText = rootView.findViewById(R.id.searchBusinessEditText);

        // Set a TextChangedListener to detect changes in search text
        searchEditText.addTextChangedListener(new TextWatcher() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove pending queries to avoid queuing multiple queries
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Set a delay before executing the query
                runnable = () -> {
                    // Query Firestore with the new search text
                    queryFirestore(s.toString().trim());
                };
                handler.postDelayed(runnable, 300); // Adjust delay time as needed
            }
        });

        // Initially query Firestore with an empty search text
        queryFirestore("");

        return rootView;
    }


    private List<String> fetchedBusinessIds = new ArrayList<>();

    private void queryFirestore(String searchText) {
        CollectionReference businessCollection = db.collection("business-accounts");

        businessCollection.get().addOnCompleteListener(task -> {
            yourLinearLayout.removeAllViews(); // Clear previous views
            fetchedBusinessIds.clear(); // Clear previously fetched business IDs

            boolean hasResults = false; // Flag to track if any matching businesses are found

            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String userName = document.getString("user-name"); // Get the business user-name from the document
                    String businessId = document.getString("business-id"); // Get the business id from the document

                    // Check if the business name contains the search text (case-insensitive)
                    if (userName.toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault()))) {
                        // Check if the business ID has already been fetched
                        if (!fetchedBusinessIds.contains(businessId)) {
                            // Fetch and set the rating
                            fetchBusinessRating(businessId, userName, document.getString("imageURL"));
                            fetchedBusinessIds.add(businessId); // Add the business ID to the list
                            hasResults = true; // Set flag to true as matching businesses are found
                        }
                    }
                }
            } else {
                // Handle errors
                Exception exception = task.getException();
                if (exception != null) {
                    exception.printStackTrace();
                }
            }

            // Display "no results found" message if no matching businesses are found
            if (!hasResults) {
                displayNoResultsMessage();
            }
        });
    }

    private void fetchBusinessRating(String businessId, String userName, String imageUrl) {
        // Fetch ratings from Firestore using businessId
        CollectionReference ratingsCollection = db.collection("ratings");

        ratingsCollection.whereEqualTo("business-id", businessId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalRatings = 0;
                        int numberOfRatings = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double rating = document.getDouble("rating");
                            if (rating != null) {
                                totalRatings += rating;
                                numberOfRatings++;
                            }
                        }

                        double averageRating = 0;
                        if (numberOfRatings > 0) {
                            averageRating = totalRatings / (double) numberOfRatings;
                        }

                        // Create the business card with user name and rating
                        createBusinessCard(userName, businessId, imageUrl, averageRating);
                    } else {
                        // Handle errors
                        Exception exception = task.getException();
                        if (exception != null) {
                            exception.printStackTrace();
                        }
                    }
                });
    }
    private void createBusinessCard(String userName, String businessId, String imageUrl, double averageRating) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setPadding(16, 16, 16, 16);
        layout.setBackgroundColor(Color.BLACK);

        // TextView for user name and rating
        TextView textView = new TextView(requireContext());
        textView.setTextAppearance(android.R.style.TextAppearance_Medium);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(null, Typeface.BOLD);
        String buttonText = String.format(Locale.getDefault(), "%s\n\n\n\nRating: %.2f/5", userName, averageRating);
        SpannableStringBuilder spannableText = new SpannableStringBuilder(buttonText);
        int startUserName = buttonText.indexOf(userName);
        int endUserName = startUserName + userName.length();
        int startRating = buttonText.indexOf("Rating:");
        int endRating = startRating + "Rating:".length();
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), startUserName, endUserName, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(Color.WHITE), startUserName, endUserName, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(Color.CYAN), startRating, endRating, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new AbsoluteSizeSpan(30, true), startUserName, endUserName, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableText);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.weight = 1;
        textView.setLayoutParams(textParams);

        // Add the TextView to the layout
        layout.addView(textView);

        // ImageView for business image
        ImageView imageView = new ImageView(requireContext());
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3; // Set size to 1/3 of the screen width
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize); // Fixed image size
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(32, 16, 0, 16); // Fixed padding
        Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.loading_placeholder)
                .error(android.R.drawable.ic_dialog_alert)
                .override(imageSize, imageSize) // Set the fixed width and height
                .centerCrop() // Ensure all images maintain the same scale
                .transform(new RoundedCorners(30)) // Apply rounded corners
                .into(imageView);

        // Add the ImageView to the layout
        layout.addView(imageView);

        // Add the arrow button
        ImageButton arrowButton = new ImageButton(requireContext());
        arrowButton.setImageResource(R.drawable.baseline_arrow_forward_24); // Your arrow icon resource
        arrowButton.setBackgroundColor(Color.TRANSPARENT);
        arrowButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.bright_cyan)); // Set color filter
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        arrowParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        arrowButton.setLayoutParams(arrowParams);
        arrowButton.setOnClickListener(v -> navigateToPlaceOrder(businessId)); // Navigate to PlaceOrderFragment when clicked

        // Add the arrow button to the layout
        layout.addView(arrowButton);

        // Add the layout to the parent layout
        yourLinearLayout.addView(layout);

        // Add space below the card
        View ratingSpace = new View(requireContext());
        ratingSpace.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 32)); // Adjusted space height
        yourLinearLayout.addView(ratingSpace);

        // Add a gray border below the card
        View border = new View(requireContext());
        border.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)); // Adjusted border height
        border.setBackgroundColor(Color.GRAY);
        yourLinearLayout.addView(border);
    }




    private void displayNoResultsMessage() {
        // Create a TextView to display "no results found" message
        TextView noResultsTextView = new TextView(requireContext());
        noResultsTextView.setText("No results found");
        noResultsTextView.setGravity(Gravity.CENTER);

        // Set text size, color, and style
        noResultsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 2 * 24); // x5 of 24sp
        noResultsTextView.setTextColor(Color.WHITE);
        noResultsTextView.setTypeface(null, Typeface.BOLD);

        // Add the TextView to your LinearLayout inside the ScrollView
        yourLinearLayout.addView(noResultsTextView);
    }


    private void navigateToPlaceOrder(String businessId) {
        // Create an instance of PlaceOrderFragment with the selected business id
        PlaceOrderFragment placeOrderFragment = PlaceOrderFragment.newInstance(businessId);

        // Replace the current fragment with PlaceOrderFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.personal_frame_layout, placeOrderFragment)
                .addToBackStack(null)
                .commit();
    }
}
