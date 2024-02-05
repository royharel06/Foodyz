package com.example.foodyz.personal;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.foodyz.LoginActivity;
import com.example.foodyz.Personal_RegisterActivity;
import com.example.foodyz.R;
import com.example.foodyz.StartActivity;
import com.example.foodyz.business.Business_MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Personal_ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Personal_ProfileFragment extends Fragment {

    private FirebaseAuth auth;

    public Personal_ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    public static Personal_ProfileFragment newInstance() {
        return new Personal_ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personal_profile, container, false);

        EditText username = view.findViewById(R.id.username);
        EditText city = view.findViewById(R.id.address_city);
        EditText street = view.findViewById(R.id.address_street);
        EditText home_number = view.findViewById(R.id.address_number);
        EditText card_number = view.findViewById(R.id.card_number);
        EditText card_cvv = view.findViewById(R.id.card_cvv);
        EditText card_holder_name = view.findViewById(R.id.card_holder_name);
        EditText card_month = view.findViewById(R.id.card_month);
        EditText card_year = view.findViewById(R.id.card_year);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users_collection = db.collection("personal-accounts");

        // Write current profile settings onto text fields:
        users_collection.whereEqualTo("personal-id", user_id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            QuerySnapshot querySnapshot = task.getResult();

                            if (querySnapshot != null && querySnapshot.size() == 1) {
                                // Document found, retrieve the value of all settings:

                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                                username.setText(document.getString("user-name"));

                                Map<String, Object> address = (Map<String, Object>) document.get("address");
                                city.setText((String) address.get("city"));
                                street.setText((String) address.get("street"));
                                home_number.setText((String) address.get("number"));

                                Map<String, Object> card = (Map<String, Object>) document.get("credit-card");

                                card_number.setText((String) card.get("card-number"));
                                card_cvv.setText(String.valueOf((Number) card.get("security-code")));
                                card_holder_name.setText((String) card.get("card-holder-name"));

                                Map<String, Object> date = (Map<String, Object>) card.get("expiration-date");

                                card_month.setText(String.valueOf((Number) date.get("month")));
                                card_year.setText(String.valueOf((Number) date.get("year")));

                            } else {
                                // Either no document found or multiple matching documents were found. Direct user to start screen:
                                Log.e("FirestoreQuery", "Could not find ID of currently authenticated user. (ID: " + user_id + ")");

                                Toast.makeText(getContext(), "Multiple Users found!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), StartActivity.class));
                                getActivity().finish();
                            }
                        } catch (Exception e) {
                            // Exception found. Direct user to start screen:
                            Log.e("FirestoreQuery", "Error getting documents: " + e.getMessage());

                            Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), StartActivity.class));
                            getActivity().finish();
                        }
                    } else {
                        // Exception found. Direct user to start screen:
                        Log.e("FirestoreQuery", "Error getting documents: " + task.getException().getMessage());

                        Toast.makeText(getActivity(), "Login Failed!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), StartActivity.class));
                        getActivity().finish();
                    }
                });

        Button save = view.findViewById(R.id.save_settings);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_username = username.getText().toString();
                String txt_city = city.getText().toString();
                String txt_street = street.getText().toString();
                String txt_home_number = home_number.getText().toString();
                String txt_card_number = card_number.getText().toString();
                String txt_card_cvv = card_cvv.getText().toString();
                String txt_card_holder_name = card_holder_name.getText().toString();
                String txt_card_month = card_month.getText().toString();
                String txt_card_year = card_year.getText().toString();

                if (validateUserSettings(txt_username,
                        txt_city, txt_street, txt_home_number,
                        txt_card_number, txt_card_cvv, txt_card_holder_name, txt_card_month, txt_card_year)) {

                    updateUser(txt_username,
                            txt_city, txt_street, txt_home_number,
                            txt_card_number, txt_card_cvv, txt_card_holder_name, txt_card_month, txt_card_year);

                    Toast. makeText(getContext(), "Update successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), Personal_MainActivity.class));
                    getActivity().finish();
                }
            }
        });

        return view;
    }

    private boolean validateUserSettings(String username,
                                         String city, String street, String home_number,
                                         String card_number, String card_cvv, String card_holder_name, String card_month, String card_year) {

        // Check for empty fields:
        if(username.isEmpty() ||
                city.isEmpty() || street.isEmpty() || home_number.isEmpty() ||
                card_number.isEmpty() || card_cvv.isEmpty() || card_holder_name.isEmpty() || card_month.isEmpty() || card_year.isEmpty()) {

            Toast. makeText(getContext(), "Field empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate card credentials length:
        if (card_number.length() != 16 || card_cvv.length() != 3) {
            Toast.makeText(getContext(), "Invalid credit card credentials!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Make sure card number is a number:
        if (!card_number.matches("\\d+")) {
            Toast. makeText(getContext(), "Invalid credit card number!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate card expiration date:
        // TODO: check that date is in the future
        int month = Integer.parseInt(card_month);
        int year = Integer.parseInt(card_year);

        if (month < 1 || 12 < month || year < 0 || 99 < year) {
            Toast. makeText(getContext(), "Invalid credit card expiration date!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateUser(String username,
                                  String city, String street, String home_number,
                                  String card_number, String card_cvv, String card_holder_name, String card_month, String card_year) {

        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update 'personal-accounts' collection
        db.collection("personal-accounts")
                .whereEqualTo("personal-id", user_id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Update the document with the new values
                        document.getReference().update(
                                "user-name", username,
                                "address.city", city,
                                "address.street", street,
                                "address.number", home_number,
                                "credit-card.card-holder-name", card_holder_name,
                                "credit-card.card-number", card_number,
                                "credit-card.security-code", Integer.parseInt(card_cvv),
                                "credit-card.expiration-date.month", Integer.parseInt(card_month),
                                "credit-card.expiration-date.year", Integer.parseInt(card_year)
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
}