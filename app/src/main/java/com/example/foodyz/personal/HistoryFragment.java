package com.example.foodyz.personal;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private LinearLayout yourLinearLayout;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        yourLinearLayout = rootView.findViewById(R.id.yourLinearLayout);

        // Initial query to fetch orders
        queryFirestoreAndCreateButtons();

        return rootView;
    }

    private void queryFirestoreAndCreateButtons() {
        CollectionReference ordersCollection = db.collection("orders");

        ordersCollection.whereEqualTo("personal-id", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    yourLinearLayout.removeAllViews(); // Clear previous buttons

                    boolean hasResults = false; // Flag to track if any orders are found

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String orderId = document.getId();
                            String businessId = document.getString("business-id");
                            Date date = document.getDate("date");
                            String status = document.getString("status");
                            double totalCost = document.getDouble("total-cost");

                            // Create button for each order
                            createButtonForOrder(orderId, businessId, date, status, totalCost);
                            hasResults = true; // Set flag to true as matching orders are found
                        }
                    } else {
                        // Handle errors
                        Exception exception = task.getException();
                        if (exception != null) {
                            exception.printStackTrace();
                        }
                    }

                    // Display "no results found" message if no matching orders are found
                    if (!hasResults) {
                        displayNoResultsMessage();
                    }
                });
    }

    private void createButtonForOrder(String orderId, String businessId, Date date, String status, double totalCost) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        // Query the business name using the business ID
        db.collection("business-accounts")
                .whereEqualTo("business-id", businessId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String businessName = document.getString("user-name");

                            requireActivity().runOnUiThread(() -> {
                                // Create button for each order with business name
                                Button button = new Button(requireContext());
                                button.setText(String.format(Locale.getDefault(), "Business: %s\nDate: %s\nStatus: %s\nTotal Cost: %.2f ₪", businessName, formattedDate, status, totalCost));
                                button.setTag(orderId);

                                // Add an OnClickListener to handle button clicks
                                button.setOnClickListener(v -> {
                                    // Handle button click here
                                    // Retrieve the order-id from the button's tag
                                    String clickedOrderId = (String) v.getTag();
                                    // Now you can use the order-id as needed
                                    navigateToOrderDetails(clickedOrderId,businessName);
                                });

                                // Add the button to your LinearLayout inside the ScrollView
                                yourLinearLayout.addView(button);
                            });
                        }
                    } else {
                        // Handle errors
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("HistoryFragment", "Error getting documents", exception);
                        } else {
                            Log.e("HistoryFragment", "Error: Task result is not successful");
                        }
                    }
                });
    }


    private void displayNoResultsMessage() {
        // Create a TextView to display "no results found" message
        TextView noResultsTextView = new TextView(requireContext());
        noResultsTextView.setText("No orders found");
        noResultsTextView.setGravity(Gravity.CENTER);

        // Add the TextView to your LinearLayout inside the ScrollView
        yourLinearLayout.addView(noResultsTextView);
    }

    private void navigateToOrderDetails(String orderId, String businessName) {
        // Create an instance of OrderDetailsFragment with the selected order-id and business name
        OrderDetailsFragment orderDetailsFragment = OrderDetailsFragment.newInstance(orderId, businessName);

        // Replace the current fragment with OrderDetailsFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.personal_frame_layout, orderDetailsFragment)
                .addToBackStack(null)
                .commit();
    }

}
