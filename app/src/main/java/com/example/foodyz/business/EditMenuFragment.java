package com.example.foodyz.business;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.foodyz.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditMenuFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditMenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditMenuFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditMenuFragment newInstance(String param1, String param2) {
        EditMenuFragment fragment = new EditMenuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    private FirebaseFirestore db;
    private LinearLayout yourLinearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_menu, container, false);

        db = FirebaseFirestore.getInstance();

        // Reference to your LinearLayout inside the ScrollView
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
                    String userName = document.getString("user-name");
                    createButtonWithUserName(userName);
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

    private void createButtonWithUserName(String userName) {
        Button button = new Button(requireContext());
        button.setText(userName);

        // Set any other properties for the button as needed

        // Add an OnClickListener if you want to handle button clicks

        // Add the button to your LinearLayout inside the ScrollView
        yourLinearLayout.addView(button);
    }
}