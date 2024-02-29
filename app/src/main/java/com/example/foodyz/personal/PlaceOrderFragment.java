package com.example.foodyz.personal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaceOrderFragment extends Fragment {

    private LinearLayout placeOrderLinearLayout;
    private EditText searchProductEditText;
    private String businessId;
    private List<String> selectedProducts = new ArrayList<>();

    public PlaceOrderFragment() {
    }

    public static PlaceOrderFragment newInstance(String businessId) {
        PlaceOrderFragment fragment = new PlaceOrderFragment();
        Bundle args = new Bundle();
        args.putString("businessId", businessId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            businessId = getArguments().getString("businessId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_order, container, false);

        placeOrderLinearLayout = rootView.findViewById(R.id.placeOrderLinearLayout);
        searchProductEditText = rootView.findViewById(R.id.searchProductEditText);

        if (getArguments() != null) {
            businessId = getArguments().getString("businessId");
            // Query Firestore and create product buttons
            queryFirestoreAndCreateButtons("");
        }

        // Add text change listener to the search EditText
        searchProductEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Query Firestore and create product buttons based on the search text
                queryFirestoreAndCreateButtons(s.toString());
            }
        });

        return rootView;
    }

    private void queryFirestoreAndCreateButtons(String searchText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference menuCollection = db.collection("business-menu");

        AtomicBoolean foundResults = new AtomicBoolean(false); // Use AtomicBoolean to track results

        menuCollection.whereEqualTo("business-id", businessId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                placeOrderLinearLayout.removeAllViews(); // Clear existing views

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String productName = document.getString("product-name");
                    String productDetails = document.getString("product-details");
                    Double unitPrice = document.getDouble("product-price");

                    // Check if the product name contains the search text (case insensitive)
                    if (productName.toLowerCase().contains(searchText.toLowerCase())) {
                        // Call the updated function with additional information
                        createButtonWithProductName(productName, productDetails, unitPrice);
                        foundResults.set(true); // Set flag to true if a product is found
                    }
                }

                // Create "Complete Order" button after product buttons if results are found
                if (!foundResults.get()) {
                    createNoResultsMessage();
                } else {
                    createCompleteOrderButton();
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

    private void createNoResultsMessage() {
        // Create a TextView for displaying "No Results Found" message
        TextView noResultsTextView = new TextView(requireContext());
        noResultsTextView.setText("No Results Found");
        noResultsTextView.setTextSize(18); // Adjust text size if needed
        noResultsTextView.setGravity(Gravity.CENTER); // Center the text horizontally
        noResultsTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Add the TextView to the LinearLayout inside the ScrollView
        placeOrderLinearLayout.addView(noResultsTextView);
    }


    private void createButtonWithProductName(String productName, String productDetails, Double unitPrice) {
        // Create a button
        Button button = new Button(requireContext());

        // Set the text on the button to display product name, details, and price
        String buttonText = String.format("%s\n%s\n%s ₪", productName, productDetails, unitPrice);
        button.setText(buttonText);

        // Add an OnClickListener to handle button clicks
        button.setOnClickListener(v -> {
            // Handle button click here
            addProductToOrderList(productName);
        });

        // Add the button to the LinearLayout inside the ScrollView
        placeOrderLinearLayout.addView(button);
    }

    private void navigateToCompleteOrder(String businessId, List<String> selectedProducts) {
        // Create an instance of CompleteOrderFragment with the selected products and businessId
        CompleteOrderFragment completeOrderFragment = CompleteOrderFragment.newInstance(businessId, selectedProducts);

        // Replace the current fragment with CompleteOrderFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.personal_frame_layout, completeOrderFragment)
                .addToBackStack(null)
                .commit();
    }

    private void createCompleteOrderButton() {
        // Create a button for completing the order
        Button completeOrderButton = new Button(requireContext());
        completeOrderButton.setText("Complete Order");

        // Add an OnClickListener to handle button clicks
        completeOrderButton.setOnClickListener(v -> {
            // Navigate to CompleteOrderFragment when "Complete Order" button is clicked
            navigateToCompleteOrder(businessId, selectedProducts);
        });

        // Add the "Complete Order" button to the LinearLayout inside the ScrollView
        placeOrderLinearLayout.addView(completeOrderButton);
    }

    private void addProductToOrderList(String productName) {
        // Add the selected product to the order list
        selectedProducts.add(productName);

        // Optionally, you can update the UI or perform any other actions based on the selected product
        Toast.makeText(requireContext(), "Product added to order list: " + productName, Toast.LENGTH_SHORT).show();
    }
}
