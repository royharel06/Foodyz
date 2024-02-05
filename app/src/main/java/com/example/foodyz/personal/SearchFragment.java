package com.example.foodyz.personal;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.foodyz.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class SearchFragment extends Fragment {

    private LinearLayout yourLinearLayout;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();
        yourLinearLayout = rootView.findViewById(R.id.yourLinearLayout);

        // Query Firestore and create buttons
        queryFirestoreAndCreateButtons();

        return rootView;
    }

    private void queryFirestoreAndCreateButtons() {
        CollectionReference businessCollection = db.collection("business-accounts");

        businessCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String userName = document.getString("user-name"); // Get the business user-name from the document
                    String businessId = document.getString("business-id"); // Get the business id from the document
                    createButtonWithUserName(userName, businessId);
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
