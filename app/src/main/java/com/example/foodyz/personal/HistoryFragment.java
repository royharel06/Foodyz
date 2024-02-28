package com.example.foodyz.personal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.foodyz.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    private String PersonalId;
    private LinearLayout fragmenthistory;
    private FirebaseFirestore db;

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
            this.PersonalId = getArguments().getString("PersonalId");
        }
    }

    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        db = FirebaseFirestore.getInstance();

        fragmenthistory = rootView.findViewById(R.id.fragmenthistory);
        queryFirestoreAndCreateButtons();

        return rootView;
    }

    private void queryFirestoreAndCreateButtons() {
        CollectionReference OrdersCollection = db.collection("orders");

        OrdersCollection.whereEqualTo("personal-id", PersonalId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String BusinessName = document.getString("business-id");
                    String Date = document.getString("date"); // Assuming date is stored as string, adjust accordingly
                    Double Price = document.getDouble("total-cost");
                    String orderId = document.getId(); // Get the order id

                    createButtonWithOrderDetails(BusinessName, Date, Price, orderId);
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
        Context context = getContext();
        if (context == null) {
            return;
        }

        Button button = new Button(context);

        String buttonText = String.format("%s\n%s\n%s ₪", BusinessName, Date, TotalCost);
        button.setText(buttonText);

        button.setTag(orderId);

        button.setOnClickListener(v -> {
            String orderIdClicked = (String) v.getTag();
            navigateToOrderDetailsFragment(orderIdClicked);
        });

        fragmenthistory.addView(button);
    }

    private void navigateToOrderDetailsFragment(String orderId) {
        Log.d("HistoryFragment", "Navigating to OrderDetailsFragment");

        Order_Details_Fragment fragment = Order_Details_Fragment.newInstance(orderId);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.personal_frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}