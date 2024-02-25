package com.example.foodyz.personal;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Order_Details_Fragment extends Fragment {

    private static final String ARG_ORDER_ID = "orderId";
    private String orderId;
    private LinearLayout fragmenthistory;
    private FirebaseFirestore db ;

    public Order_Details_Fragment() {
        // Required empty public constructor
    }

    public static Order_Details_Fragment newInstance(String orderId) {
        Order_Details_Fragment fragment = new Order_Details_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        db = FirebaseFirestore.getInstance();

        fragmenthistory = rootView.findViewById(R.id.fragmenthistory);
        if (getArguments() != null) {
            orderId = getArguments().getString("orderID");
            // Query Firestore and create product buttons
        }

        return rootView;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Update UI with order details
        if (orderId != null) {
            db.collection("orders-details").whereEqualTo("order-id", orderId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String productName = document.getString("product-name");
                        int quantity = document.getLong("quantity").intValue(); // Convert Long to int
                        double unitPrice = document.getDouble("unit-price");

                        // Create TextViews to display order details
                        TextView productTextView = new TextView(requireContext());
                        productTextView.setText("Product: " + productName);

                        TextView quantityTextView = new TextView(requireContext());
                        quantityTextView.setText("Quantity: " + quantity);

                        TextView priceTextView = new TextView(requireContext());
                        priceTextView.setText("Price: " + unitPrice);

                        // Set layout parameters
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        productTextView.setLayoutParams(params);
                        quantityTextView.setLayoutParams(params);
                        priceTextView.setLayoutParams(params);

                        // Add TextViews to the LinearLayout
                        fragmenthistory.addView(productTextView);
                        fragmenthistory.addView(quantityTextView);
                        fragmenthistory.addView(priceTextView);

                        // Add more TextViews for other order details as needed
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
    }


}
