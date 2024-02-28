package com.example.foodyz.business;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.foodyz.R;
import com.example.foodyz.personal.PlaceOrderFragment;
import com.google.firebase.FirebaseApp;
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

        Button new_item = view.findViewById(R.id.new_item);

        new_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddItemFragment fragment = AddItemFragment.newInstance("","",0.0);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.business_frame_layout, fragment)
                        .addToBackStack(null)
                        .remove(EditMenuFragment.this)
                        .commit();
            }
        });

        placeOrderLinearLayout = view.findViewById(R.id.item_scroll);
        queryFirestoreAndCreateButtons();

        return view;
    }

    private void queryFirestoreAndCreateButtons() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference menuCollection = db.collection("business-menu");

        menuCollection.whereEqualTo("business-id", user_id).get().addOnCompleteListener(task -> {
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
    }
}