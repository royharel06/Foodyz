package com.example.foodyz.personal;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import com.example.foodyz.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Locale;


public class SearchFragment extends Fragment {

    private LinearLayout yourLinearLayout;
    private FirebaseFirestore db;
    private EditText searchEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();
        yourLinearLayout = rootView.findViewById(R.id.yourLinearLayout);
        searchEditText = rootView.findViewById(R.id.searchEditText);

        // Add text change listener to searchEditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Call queryFirestoreAndCreateButtons with the search text
                queryFirestoreAndCreateButtons(s.toString());
            }
        });

        // Initial query without any search text
        queryFirestoreAndCreateButtons("");

        return rootView;
    }

    private void queryFirestoreAndCreateButtons(String searchText) {
        CollectionReference businessCollection = db.collection("business-accounts");

        businessCollection.get().addOnCompleteListener(task -> {
            yourLinearLayout.removeAllViews(); // Clear previous buttons

            boolean hasResults = false; // Flag to track if any matching businesses are found

            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String userName = document.getString("user-name"); // Get the business user-name from the document
                    String businessId = document.getString("business-id"); // Get the business id from the document

                    // Check if the business name contains the search text (case-insensitive)
                    if (userName.toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault()))) {
                        createButtonWithUserName(userName, businessId);
                        hasResults = true; // Set flag to true as matching businesses are found
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

    private void createButtonWithUserName(String userName, String businessId) {
        Button button = new Button(requireContext());
        button.setText(userName);
        button.setTag(businessId);

        // Add an OnClickListener to handle button clicks
        button.setOnClickListener(v -> {
            // Handle button click here
            // Retrieve the business-id from the button's tag
            String clickedBusinessId = (String) v.getTag();
            // Now you can use the business-id as needed
            navigateToPlaceOrder(clickedBusinessId);
        });

        // Add the button to your LinearLayout inside the ScrollView
        yourLinearLayout.addView(button);
    }

    private void displayNoResultsMessage() {
        // Create a TextView to display "no results found" message
        TextView noResultsTextView = new TextView(requireContext());
        noResultsTextView.setText("No results found");
        noResultsTextView.setGravity(Gravity.CENTER);

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
