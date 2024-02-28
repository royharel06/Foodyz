package com.example.foodyz.personal;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodyz.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Order_Details_Fragment extends Fragment {

    private static final String ARG_ORDER_ID = "orderId";
    private String orderId;

    public Order_Details_Fragment() {
        // Required empty public constructor
    }

    public static Order_Details_Fragment newInstance(String orderId) {
        Order_Details_Fragment fragment = new Order_Details_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.order_details_fragment, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find TextViews in the layout
        TextView orderIdTextView = view.findViewById(R.id.order_id_text_view);
        TextView businessNameTextView = view.findViewById(R.id.business_name_text_view);
        // Find other TextViews for additional order details as needed

        // Update UI with order details
        if (orderId != null) {
            // Fetch order details from Firestore based on orderId
            // Update TextViews with order details
        }
    }
}
