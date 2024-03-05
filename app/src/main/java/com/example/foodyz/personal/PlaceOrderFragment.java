package com.example.foodyz.personal;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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
                    String imageUrl = document.getString("imageURL"); // Retrieve the image URL from Firestore

                    // Check if the product name contains the search text (case insensitive)
                    if (productName.toLowerCase().contains(searchText.toLowerCase())) {
                        // Call the updated function with additional information
                        createMenuProduct(productName, productDetails, unitPrice, imageUrl); // Pass the image URL
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


    private void createMenuProduct(String productName, String productDetails, Double unitPrice, String imageUrl) {
        // Create a linear layout to hold the text, image, and plus icon
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Create the text view
        TextView textView = new TextView(requireContext());
        String buttonText = String.format("%s\n\n%s\n\n%.2f ₪", productName, productDetails, unitPrice);
        SpannableStringBuilder spannableText = new SpannableStringBuilder(buttonText);
        int startPrice = buttonText.lastIndexOf(String.format("%.2f", unitPrice));
        int endPrice = startPrice + String.format("%.2f", unitPrice).length();
        int startShekel = buttonText.indexOf("₪");
        int endShekel = startShekel + 1;
        spannableText.setSpan(new ForegroundColorSpan(Color.CYAN), startPrice, endPrice, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(Color.CYAN), startShekel, endShekel, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int startDetails = buttonText.indexOf(productDetails);
        int endDetails = startDetails + productDetails.length();
        spannableText.setSpan(new ForegroundColorSpan(Color.GRAY), startDetails, endDetails, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        spannableText.setSpan(boldSpan, 0, productName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new AbsoluteSizeSpan(24, true), 0, productName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableText);
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundColor(Color.BLACK);
        textView.setPadding(16, 16, 16, 16);
        textView.setGravity(Gravity.CENTER_VERTICAL);

        // Set layout params for the text view
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textLayoutParams.width = getResources().getDisplayMetrics().widthPixels / 4; // Set width to 1/4 of the screen's width
        textView.setLayoutParams(textLayoutParams);

        // Create the image view
        ImageView imageView = new ImageView(requireContext());
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3; // Set size to 1/4 of the screen width
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
        imageView.setLayoutParams(imageLayoutParams);
        Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.loading_placeholder) // Placeholder image while loading
                .error(android.R.drawable.ic_dialog_alert) // Error image if loading fails
                .override(imageSize, imageSize) // Set the fixed width and height
                .centerCrop() // Ensure all images maintain the same scale
                .transform(new RoundedCorners(30)) // Apply rounded corners
                .into(imageView);


        // Create the plus icon
        ImageView plusIcon = new ImageView(requireContext());
        plusIcon.setImageResource(R.drawable.baseline_add_circle_outline_24); // Plus icon
        plusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.bright_cyan)); // Set icon color
        plusIcon.setOnClickListener(v -> {
            // Show dialog when plus icon is clicked
            showDialogToAddToOrder(productName);
        });

        // Set layout params for plus icon
        LinearLayout.LayoutParams plusIconLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        plusIconLayoutParams.gravity = Gravity.END;

        // Add views to the linear layout
        linearLayout.addView(textView);
        linearLayout.addView(imageView);
        linearLayout.addView(plusIcon, plusIconLayoutParams);

        // Add the linear layout to the parent layout
        placeOrderLinearLayout.addView(linearLayout);

        // Add space below the text, image, and plus icon
        View space = new View(requireContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 64));
        placeOrderLinearLayout.addView(space);

        // Add a gray border below the text, image, and plus icon
        View border = new View(requireContext());
        border.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 8));
        border.setBackgroundColor(Color.GRAY);
        placeOrderLinearLayout.addView(border);
    }




    private void showDialogToAddToOrder(String productName) {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quantity_selection, null);

        // Initialize number picker
        NumberPicker quantityPicker = dialogView.findViewById(R.id.quantityPicker);
        quantityPicker.setMinValue(1);
        quantityPicker.setMaxValue(10);

        // Set the message
        TextView messageTextView = dialogView.findViewById(R.id.messageTextView);
        String message = "How many " + productName + " would you like to order?";
        messageTextView.setText(message);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Handle button clicks
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            int quantity = quantityPicker.getValue();
            addProductToOrderList(productName, quantity);
            dialog.dismiss();
            Toast.makeText(requireContext(), "Added " + quantity + " " + productName + " to your order!", Toast.LENGTH_SHORT).show();
        });

        // Show the dialog
        dialog.show();
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

        // Set text appearance attributes
        completeOrderButton.setAllCaps(false); // Set uppercase to false
        completeOrderButton.setTextColor(Color.BLACK); // Set text color to black
        completeOrderButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size to slightly bigger

        // Set background color to bright cyan
        completeOrderButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bright_cyan));

        // Set corner radius
        int cornerRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        completeOrderButton.setBackgroundResource(R.drawable.rectangle_with_rounded_corners);

        // Adjust width to be half of the screen width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL; // Center the button horizontally
        layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.space_above_button); // Space above the button
        completeOrderButton.setLayoutParams(layoutParams);

        // Add an OnClickListener to handle button clicks
        completeOrderButton.setOnClickListener(v -> {
            if (selectedProducts != null && !selectedProducts.isEmpty()) {
                // Navigate to CompleteOrderFragment when "Complete Order" button is clicked
                navigateToCompleteOrder(businessId, selectedProducts);
            } else {
                // Show message if no products were selected
                Toast.makeText(requireContext(), "No products were selected!", Toast.LENGTH_SHORT).show();
            }
        });


        // Add the "Complete Order" button to the LinearLayout inside the ScrollView
        placeOrderLinearLayout.addView(completeOrderButton);
    }





    private void addProductToOrderList(String productName, int quantity) {
        // Add the selected product to the order list
        for (int i = 0; i < quantity; i++) {
            selectedProducts.add(productName);
        }
    }

}
