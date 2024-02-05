package com.example.foodyz.business;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.foodyz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddItemFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NAME = "name";
    private static final String ARG_DETAILS = "details";
    private static final String ARG_PRICE = "price";

    private String product_name;
    private String product_details;
    private Double unit_price;

    public AddItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddItemFragment.
     */
    public static AddItemFragment newInstance(String name, String details, Double price) {
        AddItemFragment fragment = new AddItemFragment();

        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_DETAILS, details);
        args.putDouble(ARG_PRICE, price);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            product_name = getArguments().getString(ARG_NAME);
            product_details = getArguments().getString(ARG_DETAILS);
            unit_price = getArguments().getDouble(ARG_PRICE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        EditText name = view.findViewById(R.id.item_name);
        EditText details = view.findViewById(R.id.item_details);
        EditText price = view.findViewById(R.id.item_price);

        Button save = view.findViewById(R.id.save_item);

        if (!product_name.isEmpty()) {
            name.setText(product_name);
            details.setText(product_details);
            price.setText(Double.toString(unit_price));
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = name.getText().toString();
                String itemDetails = details.getText().toString();
                double itemPrice = Double.parseDouble(price.getText().toString());

                if (!product_name.isEmpty()) {
                    // If product_name is not empty, update the document
                    updateDocument(itemName, itemDetails, itemPrice);
                } else {
                    // If product_name is empty, create a new document
                    createNewDocument(itemName, itemDetails, itemPrice);
                }

                // Return to EditMenuFragment:
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.business_frame_layout, new EditMenuFragment())
                        .addToBackStack(null)
                        .remove(AddItemFragment.this)
                        .commit();
            }
        });

        return view;
    }

    private void updateDocument(String itemName, String itemDetails, double itemPrice) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming "ass" is your collection name
        db.collection("business-menu")
                .whereEqualTo("business-id", user_id) // Change to your actual field name
                .whereEqualTo("product-name", product_name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Update the document with the new values
                        document.getReference().update(
                                "product-name", itemName,
                                "product-details", itemDetails,
                                "product-price", itemPrice
                        ).addOnSuccessListener(aVoid -> {
                            // Document updated successfully
                            Log.d("Firestore", "DocumentSnapshot updated successfully");
                        }).addOnFailureListener(e -> {
                            // Handle errors
                            Log.e("Firestore", "Error updating document", e);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e("Firestore", "Error getting documents", e);
                });
    }

    private void createNewDocument(String itemName, String itemDetails, double itemPrice) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming "ass" is your collection name
        db.collection("business-menu")
                .add(new HashMap<String, Object>() {{
                    put("product-name", itemName);
                    put("product-details", itemDetails);
                    put("product-price", itemPrice);
                    put("business-id", user_id); // Set the actual field name and value
                }})
                .addOnSuccessListener(documentReference -> {
                    // Document created successfully
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e("Firestore", "Error creating document", e);
                });
    }
}