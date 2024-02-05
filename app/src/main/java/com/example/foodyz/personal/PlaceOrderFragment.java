package com.example.foodyz.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class PlaceOrderFragment extends Fragment {

    private LinearLayout placeOrderLinearLayout;
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

        if (getArguments() != null) {
            businessId = getArguments().getString("businessId");
            // Query Firestore and create product buttons
            queryFirestoreAndCreateButtons();
        }

        return rootView;
    }

    private void queryFirestoreAndCreateButtons() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference menuCollection = db.collection("business-menu");

        menuCollection.whereEqualTo("business-id", businessId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String productName = document.getString("product-name");
                    String productDetails = document.getString("product-details");
                    Double unitPrice = document.getDouble("product-price");

                    // Call the updated function with additional information
                    createButtonWithProductName(productName, productDetails, unitPrice);
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

    


    private void addProductToOrderList(String productName) {
        // Add the selected product to the order list
        selectedProducts.add(productName);

        // Optionally, you can update the UI or perform any other actions based on the selected product
        Toast.makeText(requireContext(), "Product added to order list: " + productName, Toast.LENGTH_SHORT).show();
    }
}




