package com.example.foodyz.personal;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompleteOrderFragment extends Fragment {

    private List<String> selectedProducts;
    private String businessId;
    private Double totalCost;

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

        LinearLayout completeOrderLinearLayout = view.findViewById(R.id.completeOrderLinearLayout);
        TextView totalTextView = view.findViewById(R.id.totalTextView);

        if (selectedProducts != null && !selectedProducts.isEmpty()) {
            // Calculate quantities and total cost
            Map<String, Integer> quantities = new HashMap<>();

            // Query Firestore for product prices
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference menuCollection = db.collection("business-menu");

            // Start processing the first product
            processProduct(0, menuCollection, completeOrderLinearLayout, totalTextView, quantities);
        } else {
            // Display a message prompt if no products are selected
            TextView noOrderTextView = new TextView(requireContext());
            noOrderTextView.setText("You ordered nothing!!!");
            completeOrderLinearLayout.addView(noOrderTextView);
            totalTextView.setVisibility(View.GONE); // Hide total cost TextView
        }
    }

    private void processProduct(int index, CollectionReference menuCollection, LinearLayout layout, TextView totalTextView, Map<String, Integer> quantities) {
        if (index < selectedProducts.size()) {
            String productName = selectedProducts.get(index);
            int quantity = Collections.frequency(selectedProducts, productName);

            // Query Firestore to get product price
            menuCollection.whereEqualTo("business-id", businessId)
                    .whereEqualTo("product-name", productName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                double unitPrice = document.getDouble("product-price");

                                // Update total cost
                                totalCost += unitPrice * quantity;

                                // Create a TextView for each product
                                createTextViewWithProductDetails(productName, quantity, unitPrice, layout);

                                // Update the total cost TextView
                                totalTextView.setText("Total: " + totalCost);
                            }

                            // Process the next product
                            processProduct(index + quantity, menuCollection, layout, totalTextView, quantities);
                        } else {
                            // Handle errors
                            Exception exception = task.getException();
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                        }
                    });
        }
    }


    private void createTextViewWithProductDetails(String productName, int quantity, double unitPrice, LinearLayout layout) {
        // Create a TextView
        TextView textView = new TextView(requireContext());

        // Set the text to display product details
        String text = String.format("Product: %s\nQuantity: %d\nUnit Price: %.2f\nTotal Cost: %.2f", productName, quantity, unitPrice, unitPrice * quantity);





        // Use SpannableString to make "Product: Burger" bold
        SpannableString spannableString = new SpannableString(text);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, Math.min("Product: ".length() + productName.length(), text.length()), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        // Set the formatted text to the TextView
        textView.setText(spannableString);

        // Add the TextView to the LinearLayout
        layout.addView(textView);
    }
}


