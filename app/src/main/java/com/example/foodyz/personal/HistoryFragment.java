package com.example.foodyz.personal;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodyz.R;
import com.google.firebase.Timestamp;
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

    private ListView orderListView;
    private ArrayAdapter<String> adapter;
    private List<String> orderList;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        orderListView = view.findViewById(R.id.orderListView);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, orderList);
        orderListView.setAdapter(adapter);

        // Set click listener for ListView items
        orderListView.setOnItemClickListener((parent, view1, position, id) -> {
            // Log statement to check if the click listener is triggered
            Log.d("HistoryFragment", "Order clicked at position: " + position);

            String orderDetails = orderList.get(position);
            navigateToHelloWorldFragment();
        });

        retrieveOrderHistory();
    }



    private void retrieveOrderHistory() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ordersRef = db.collection("orders");


        ordersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                orderList.clear(); // Clear the list before adding new data
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String orderDetails = formatOrderDetails(document);
                    orderList.add(orderDetails);
                }
                adapter.notifyDataSetChanged();
            } else {
                // Handle error
                Toast.makeText(requireContext(), "Failed to retrieve order history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatOrderDetails(QueryDocumentSnapshot document) {
        String restaurantID = document.getString("business-id");


        double totalPrice = document.getDouble("total-cost");
        Timestamp timestamp = document.getTimestamp("date");
        Date date = timestamp.toDate();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateString = sdf.format(date);
        String totalp = String.valueOf(totalPrice);
        StringBuilder orderDetailsBuilder = new StringBuilder();
        orderDetailsBuilder.append("Restaurant Name: ").append(restaurantID).append("\n");
        orderDetailsBuilder.append("Order Date: ").append(dateString).append("\n");
        orderDetailsBuilder.append("Total Price: ").append(totalp);

        return orderDetailsBuilder.toString();
    }


    private void navigateToOrderDetailsFragment(String orderDetails) {
        // Log statement to check if the method is called
        Log.d("HistoryFragment", "Navigating to OrderDetailsFragment");

        Order_Details_Fragment fragment = new Order_Details_Fragment();
        Bundle args = new Bundle();
        args.putString("orderDetails", orderDetails);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void navigateToHelloWorldFragment() {
        Order_Details_Fragment fragment = new Order_Details_Fragment();

        // Get the FragmentManager and start a transaction
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the contents of the fragment container with the new fragment
        transaction.replace(R.id.fragment_container, fragment);

        // Add the transaction to the back stack (optional)
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

}