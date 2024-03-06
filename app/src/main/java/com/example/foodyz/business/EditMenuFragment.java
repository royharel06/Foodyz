package com.example.foodyz.business;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.foodyz.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditMenuFragment extends Fragment {
    private LinearLayout placeOrderLinearLayout;

    public EditMenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditMenuFragment.
     */
    public static EditMenuFragment newInstance(String param1, String param2) {
        return new EditMenuFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_menu, container, false);

        TextInputEditText searchProductEditText = view.findViewById(R.id.search_item);
        placeOrderLinearLayout = view.findViewById(R.id.item_scroll);
        Button new_item = view.findViewById(R.id.new_item);

        new_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddItemFragment fragment = AddItemFragment.newInstance("","",0.0, "");

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.business_frame_layout, fragment)
                        .addToBackStack(null)
                        .remove(EditMenuFragment.this)
                        .commit();
            }
        });

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

        queryFirestoreAndCreateButtons("");

        return view;
    }

    private void queryFirestoreAndCreateButtons(String searchText) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference menuCollection = db.collection("business-menu");

        menuCollection.whereEqualTo("business-id", user_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                placeOrderLinearLayout.removeAllViews(); // Clear existing views

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String productName = document.getString("product-name");
                    String productDetails = document.getString("product-details");
                    Double unitPrice = document.getDouble("product-price");
                    String imageUrl = document.getString("imageURL");

                    // Check if the product name contains the search text (case insensitive)
                    if (productName.toLowerCase().contains(searchText.toLowerCase()) || searchText.isEmpty()) {
                        // Call the updated function with additional information
                        createMenuProduct(productName, productDetails, unitPrice, imageUrl); // Pass the image URL
                    }

                    // Call the updated function with additional information
                    //createButtonWithProductName(productName, productDetails, unitPrice);
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
        ImageView editButton = new ImageView(requireContext());
        editButton.setImageResource(R.drawable.baseline_create_24); // Plus icon
        editButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.bright_cyan)); // Set icon color
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddItemFragment fragment = AddItemFragment.newInstance(productName, productDetails, unitPrice, imageUrl);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.business_frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();

                // TODO: refresh page after editing an item to show the change right away
            }
        });

        // Set layout params for plus icon
        LinearLayout.LayoutParams editButtonLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editButtonLayoutParams.gravity = Gravity.END;

        // Add views to the linear layout
        linearLayout.addView(textView);
        linearLayout.addView(imageView);
        linearLayout.addView(editButton, editButtonLayoutParams);

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

    /*private void createButtonWithProductName(String productName, String productDetails, Double unitPrice) {
        // Create a button
        Button button = new Button(requireContext());

        // Set the text on the button to display product name, details, and price
        String buttonText = String.format("%s\n%s\n%s ₪", productName, productDetails, unitPrice);
        button.setText(buttonText);

        // Add an OnClickListener to handle button clicks
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddItemFragment fragment = AddItemFragment.newInstance(productName, productDetails, unitPrice);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.business_frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();

                // TODO: refresh page after editing an item to show the change right away
            }
        });

        // Add the button to the LinearLayout inside the ScrollView
        placeOrderLinearLayout.addView(button);
    }*/
}