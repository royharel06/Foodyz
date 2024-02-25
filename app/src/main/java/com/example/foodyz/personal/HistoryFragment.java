package com.example.foodyz.personal;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodyz.R;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private String PersonalId;
    private LinearLayout fragmenthistory;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String PersonalId) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString("PersonalId", PersonalId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.PersonalId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        fragmenthistory = rootView.findViewById(R.id.fragmenthistory);
        if (getArguments() != null) {
            PersonalId = getArguments().getString("PersonalId");
            // Query Firestore and create product buttons
            queryFirestoreAndCreateButtons();
        }
        return rootView;
    }

    private void queryFirestoreAndCreateButtons() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference OrdersCollection = db.collection("orders");


        OrdersCollection.whereEqualTo("personal-id", PersonalId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String BusinessName = document.getString("business-id");
                    Timestamp Date = document.getTimestamp("date");
                    Date date = Date.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String dateString = sdf.format(date);
                    Double Price = document.getDouble("total-cost");
                    String orderId = document.getId(); // Get the order id

                    // Call the updated function with additional information
                    createButtonWithOrderDetails(BusinessName, dateString, Price, orderId);
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

    private void createButtonWithOrderDetails(String BusinessName, String Date, Double TotalCost, String orderId) {
        // Create a button
        Button button = new Button(requireContext());

        // Set the text on the button to display business Name ,Date , total-cost
        String buttonText = String.format("%s\n%s\n%s ₪", BusinessName, Date, TotalCost);
        button.setText(buttonText);

        // Set the tag of the button to the order id
        button.setTag(orderId);

        // Add an OnClickListener to handle button clicks
        button.setOnClickListener(v -> {
            // Handle button click here
            String orderIdClicked = (String) v.getTag(); // Get the order id from the clicked button
            navigateToOrderDetailsFragment(orderIdClicked);
        });

        // Add the button to the LinearLayout inside the ScrollView
        fragmenthistory.addView(button);
    }

    private void navigateToOrderDetailsFragment(String orderId) {
        // Log statement to check if the method is called
        Log.d("HistoryFragment", "Navigating to OrderDetailsFragment");

        // Create a new instance of OrderDetailsFragment and pass the order id
        Order_Details_Fragment fragment = Order_Details_Fragment.newInstance(orderId);

        // Begin the transaction to replace the current fragment with the OrderDetailsFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
