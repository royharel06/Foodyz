package com.example.foodyz.business;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class OrdersFragment extends Fragment {
    private LinearLayout scroll_view;

    private FirebaseFirestore db;
    private FirebaseUser current_user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_orders, container, false);

        db = FirebaseFirestore.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser();

        // Find the LinearLayout in the XML layout
        scroll_view = rootView.findViewById(R.id.orders);

        // Get the EditText for search input
        TextInputEditText searchEditText = rootView.findViewById(R.id.search);

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
                    queryFirestoreAndCreateTextViews(s.toString().trim().toLowerCase(Locale.getDefault()));
                };
                handler.postDelayed(runnable, 300); // Adjust delay time as needed
            }
        });

        // Initially query Firestore with an empty search text
        queryFirestoreAndCreateTextViews("");

        return rootView;
    }

    private void queryFirestoreAndCreateTextViews(String searchText) {
        CollectionReference ordersCollection = db.collection("orders");
        ordersCollection.whereEqualTo("business-id", current_user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    scroll_view.removeAllViews(); // Clear previous TextViews

                    AtomicBoolean hasResults = new AtomicBoolean(false); // Flag to track if any orders are found

                    if (task.isSuccessful()) {
                        int numDocuments = task.getResult().size();
                        AtomicInteger count = new AtomicInteger(); // Counter for tracking iterations

                        String[] status_list = new String[] {"Pending", "Ready", "Received"};
                        for (String status : status_list) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!document.getString("status").equals(status))
                                    continue;

                                String orderId = document.getId();
                                String personalId = document.getString("personal-id");

                                // Retrieve date, status, and total cost from the orders collection
                                Date date = document.getDate("date");
                                double totalCost = document.getDouble("total-cost");

                                // Query the business name using the business ID
                                db.collection("personal-accounts")
                                        .whereEqualTo("personal-id", personalId)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                                    String businessName = document1.getString("user-name");
                                                    if (businessName.toLowerCase(Locale.getDefault()).contains(searchText)) {
                                                        // Create TextView for each order
                                                        createTextViewForOrder(orderId, personalId, businessName, date, status, totalCost);
                                                        hasResults.set(true); // Set flag to true as matching orders are found
                                                    }
                                                }
                                            } else {
                                                // Handle errors
                                                Exception exception = task1.getException();
                                                if (exception != null) {
                                                    exception.printStackTrace();
                                                }
                                            }

                                            // Increment the counter and check if it's the last iteration
                                            count.getAndIncrement();
                                            if (count.get() == numDocuments) {
                                                // Display "No results!" message if no matching orders are found overall
                                                if (!hasResults.get()) {
                                                    displayNoResultsMessage();
                                                }
                                            }
                                        });
                            }
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

    private void displayNoResultsMessage() {
        // Create a TextView to display "no results found" message
        TextView noResultsTextView = new TextView(requireContext());
        noResultsTextView.setText("No results found.");
        noResultsTextView.setGravity(Gravity.CENTER);

        // Set text size, color, and style
        noResultsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 2 * 24); // x5 of 24sp
        noResultsTextView.setTextColor(Color.WHITE);
        noResultsTextView.setTypeface(null, Typeface.BOLD);

        // Add the TextView to your LinearLayout inside the ScrollView
        scroll_view.addView(noResultsTextView);
    }

    private void createTextViewForOrder(String orderId, String personalId, String customerName, Date date, String status, double totalCost) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        requireActivity().runOnUiThread(() -> {
            // Create TextViews for each order detail
            TextView businessNameTextView = createTextView(customerName, Color.WHITE, true, 24);
            TextView dateTextView = createTextView("Date: " + formattedDate, Color.WHITE, false, 18);
            TextView statusTextView = createTextView("Status: " + status, Color.GRAY, false, 18);
            TextView totalCostTextView = createTextView("Total Cost: " + String.format(Locale.getDefault(), "%.2f", totalCost) + " ₪", Color.CYAN, true, 24);

            // Add TextViews to LinearLayout
            scroll_view.addView(businessNameTextView);
            scroll_view.addView(dateTextView);
            scroll_view.addView(statusTextView);
            scroll_view.addView(totalCostTextView);

            // Add click listener to navigate to OrderDetailsFragment
            View.OnClickListener navigateToOrderDetailsListener = v -> navigateToOrderDetails(orderId, personalId, customerName);
            businessNameTextView.setOnClickListener(navigateToOrderDetailsListener);
            dateTextView.setOnClickListener(navigateToOrderDetailsListener);
            statusTextView.setOnClickListener(navigateToOrderDetailsListener);
            totalCostTextView.setOnClickListener(navigateToOrderDetailsListener);

            // Add blue arrow to navigate to OrderDetailsFragment
            ImageView arrowImageView = new ImageView(requireContext());
            arrowImageView.setImageResource(R.drawable.baseline_arrow_forward_24);
            arrowImageView.setColorFilter(Color.CYAN);
            arrowImageView.setOnClickListener(navigateToOrderDetailsListener); // Navigate when arrow clicked
            LinearLayout.LayoutParams arrowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            arrowLayoutParams.gravity = Gravity.END;
            arrowImageView.setLayoutParams(arrowLayoutParams);
            scroll_view.addView(arrowImageView);

            // Add space
            View spaceView = new View(requireContext());
            LinearLayout.LayoutParams spaceLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 32); // Adjust space height as needed
            spaceView.setLayoutParams(spaceLayoutParams);
            scroll_view.addView(spaceView);

            // Add gray border
            View borderView = new View(requireContext());
            LinearLayout.LayoutParams borderLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2); // Adjust border height as needed
            borderView.setLayoutParams(borderLayoutParams);
            borderView.setBackgroundColor(Color.GRAY);
            scroll_view.addView(borderView);
        });
    }

    private TextView createTextView(String text, int color, boolean isBold, int textSize) {
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
        if (isBold) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        return textView;
    }

    private void navigateToOrderDetails(String orderId, String personalId, String customerName) {
        // Navigate to OrderDetailsFragment with orderId, businessId, and businessName
        Business_OrderDetailsFragment fragment = Business_OrderDetailsFragment.newInstance(orderId, personalId, customerName);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.business_frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}