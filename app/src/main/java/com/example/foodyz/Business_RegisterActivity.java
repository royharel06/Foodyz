package com.example.foodyz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodyz.business.Business_MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Business_RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_register);

        TextInputEditText email = findViewById(R.id.email);
        TextInputEditText password = findViewById(R.id.password);
        TextInputEditText username = findViewById(R.id.username);
        TextInputEditText city = findViewById(R.id.address_city);
        TextInputEditText street = findViewById(R.id.address_street);
        TextInputEditText home_number = findViewById(R.id.address_number);
        TextInputEditText bank_number = findViewById(R.id.bank_number);
        TextInputEditText bank_branch = findViewById(R.id.bank_branch);

        Button register = findViewById(R.id.register_business);

        auth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                String txt_username = username.getText().toString();
                String txt_city = city.getText().toString();
                String txt_street = street.getText().toString();
                String txt_home_number = home_number.getText().toString();
                String txt_bank_number = bank_number.getText().toString();
                String txt_bank_branch = bank_branch.getText().toString();

                if (validateUserSettings(txt_email, txt_username, txt_password,
                        txt_city, txt_street, txt_home_number,
                        txt_bank_number, txt_bank_branch)) {

                    registerUser(txt_email, txt_username, txt_password,
                            txt_city, txt_street, txt_home_number,
                            txt_bank_number, txt_bank_branch);
                }
            }
        });
    }

    private void registerUser(String email, String username, String password,
                              String city, String street, String home_number,
                              String bank_number, String bank_branch) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Business_RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveUserSettings(email, username,
                            city, street, home_number,
                            bank_number, bank_branch);

                    Toast. makeText(Business_RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Business_RegisterActivity.this, Business_MainActivity.class));
                    finish();
                } else {
                    Toast. makeText(Business_RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Business_RegisterActivity.this, StartActivity.class));
                    finish();
                }
            }
        });
    }

    private boolean validateUserSettings(String email, String username, String password,
                                         String city, String street, String home_number,
                                         String bank_number, String bank_branch) {

        // Check for empty fields:
        if(email.isEmpty() || username.isEmpty() ||
                city.isEmpty() || street.isEmpty() || home_number.isEmpty() ||
                bank_number.isEmpty() || bank_branch.isEmpty()) {

            Toast. makeText(Business_RegisterActivity.this, "Field empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check password length:
        if (password.length() < 4) {
            Toast.makeText(Business_RegisterActivity.this, "Password too short!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate email address:
        String email_pattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(email_pattern);
        if (!pattern.matcher(email).matches()) {
            Toast. makeText(Business_RegisterActivity.this, "Invalid email address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate bank account credentials:
        if (bank_number.length() != 6 || bank_branch.length() != 3 || !bank_number.matches("\\d+") || !bank_branch.matches("\\d+")) {
            Toast. makeText(Business_RegisterActivity.this, "Invalid bank account credentials!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveUserSettings(String email, String username,
                                  String city, String street, String home_number,
                                  String bank_number, String bank_branch) {

        Map<String, Object> address = new HashMap<>();
        address.put("city", city);
        address.put("street", street);
        address.put("number", home_number);

        Map<String, Object> bank = new HashMap<>();
        bank.put("bank", bank_branch);
        bank.put("number", bank_number);

        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        Map<String, Object> business_accounts_entry = new HashMap<>();
        business_accounts_entry.put("user-name", username);
        business_accounts_entry.put("email", email);
        business_accounts_entry.put("business-id", user_id);
        business_accounts_entry.put("address", address);
        business_accounts_entry.put("bank-account", bank);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("business-accounts")
                .add(business_accounts_entry)
                .addOnSuccessListener(documentReference_businessAccounts -> {
                    // Document added successfully.
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference_businessAccounts.getId());

                    // Add document to 'users' collection:
                    String document_id = documentReference_businessAccounts.getId();
                    DocumentReference document_reference = db.document("/business-accounts/"+document_id);

                    Map<String, Object> users_entry = new HashMap<>();
                    users_entry.put("id", user_id);
                    users_entry.put("personal", false);
                    users_entry.put("ref", document_reference);

                    db.collection("users")
                            .add(users_entry)
                            .addOnSuccessListener(documentReference_users -> {
                                // Document added successfully
                                Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference_users.getId());
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors
                                Log.e("Firestore", "Error adding document", e);
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e("Firestore", "Error adding document", e);
                });
    }
}