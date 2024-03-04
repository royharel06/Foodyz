package com.example.foodyz.personal;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.style.AbsoluteSizeSpan;
import android.widget.LinearLayout.LayoutParams;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

        // Format the button text with empty lines between variables
        String buttonText = String.format("%s\n\n%s\n\n%.2f ₪", productName, productDetails, unitPrice);
        SpannableStringBuilder spannableText = new SpannableStringBuilder(buttonText);

        // Set the colors for text and background
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.BLACK);

        // Set the unit price and shekel sign color
        String unitPriceString = String.format("%.2f", unitPrice);
        int startPrice = buttonText.lastIndexOf(unitPriceString);
        int endPrice = startPrice + unitPriceString.length();
        int startShekel = buttonText.indexOf("₪");
        int endShekel = startShekel + 1;

        spannableText.setSpan(new ForegroundColorSpan(Color.CYAN), startPrice, endPrice, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(Color.CYAN), startShekel, endShekel, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the product details color
        int startDetails = buttonText.indexOf(productDetails);
        int endDetails = startDetails + productDetails.length();
        spannableText.setSpan(new ForegroundColorSpan(Color.GRAY), startDetails, endDetails, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set bold style for product name and increase text size
        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        spannableText.setSpan(boldSpan, 0, productName.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new AbsoluteSizeSpan(24, true), 0, productName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Adjust the text size as needed

        // Set the formatted text to the button
        button.setText(spannableText);

        // Set textAllCaps to false
        button.setAllCaps(false);

        // Set text alignment to start
        button.setGravity(Gravity.START);

        // Add an OnClickListener to handle button clicks
        button.setOnClickListener(v -> {
            // Handle button click here
            addProductToOrderList(productName);
        });

        // Add the button to the LinearLayout inside the ScrollView
        placeOrderLinearLayout.addView(button);

        // Add space below the button
        View space = new View(requireContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 64)); // Adjust the space height as needed
        placeOrderLinearLayout.addView(space);

        // Add a gray border below the button
        View border = new View(requireContext());
        border.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 8)); // Adjust the border height as needed
        border.setBackgroundColor(Color.GRAY);
        placeOrderLinearLayout.addView(border);
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
