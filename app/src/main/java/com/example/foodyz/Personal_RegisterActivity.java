package com.example.foodyz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodyz.personal.Personal_MainActivity;
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

public class Personal_RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_register);

        TextInputEditText email = findViewById(R.id.email);
        TextInputEditText password = findViewById(R.id.password);
        TextInputEditText username = findViewById(R.id.username);
        TextInputEditText city = findViewById(R.id.address_city);
        TextInputEditText street = findViewById(R.id.address_street);
        TextInputEditText home_number = findViewById(R.id.address_number);
        TextInputEditText card_number = findViewById(R.id.card_number);
        TextInputEditText card_cvv = findViewById(R.id.card_cvv);
        TextInputEditText card_holder_name = findViewById(R.id.card_holder_name);
        TextInputEditText card_month = findViewById(R.id.card_month);
        TextInputEditText card_year = findViewById(R.id.card_year);

        Button register = findViewById(R.id.register_personal);

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
                String txt_card_number = card_number.getText().toString();
                String txt_card_cvv = card_cvv.getText().toString();
                String txt_card_holder_name = card_holder_name.getText().toString();
                String txt_card_month = card_month.getText().toString();
                String txt_card_year = card_year.getText().toString();

                if (validateUserSettings(txt_email, txt_username, txt_password,
                        txt_city, txt_street, txt_home_number,
                        txt_card_number, txt_card_cvv, txt_card_holder_name, txt_card_month, txt_card_year)) {

                    registerUser(txt_email, txt_username, txt_password,
                            txt_city, txt_street, txt_home_number,
                            txt_card_number, txt_card_cvv, txt_card_holder_name, txt_card_month, txt_card_year);
                }
            }
        });
    }

    private void registerUser(String email, String username, String password,
                              String city, String street, String home_number,
                              String card_number, String card_cvv, String card_holder_name, String card_month, String card_year) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Personal_RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveUserSettings(email, username,
                            city, street, home_number,
                            card_number, card_cvv, card_holder_name, card_month, card_year);

                    Toast. makeText(Personal_RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Personal_RegisterActivity.this, Personal_MainActivity.class));
                    finish();
                } else {
                    Toast. makeText(Personal_RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Personal_RegisterActivity.this, StartActivity.class));
                    finish();
                }
            }
        });
    }

    private boolean validateUserSettings(String email, String username, String password,
                                      String city, String street, String home_number,
                                      String card_number, String card_cvv, String card_holder_name, String card_month, String card_year) {

        // Check for empty fields:
        if(email.isEmpty() || username.isEmpty() ||
                city.isEmpty() || street.isEmpty() || home_number.isEmpty() ||
                card_number.isEmpty() || card_cvv.isEmpty() || card_holder_name.isEmpty() || card_month.isEmpty() || card_year.isEmpty()) {

            Toast. makeText(Personal_RegisterActivity.this, "Field empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check password length:
        if (password.length() < 6) {
            Toast.makeText(Personal_RegisterActivity.this, "Password too short!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate email address:
        String email_pattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(email_pattern);
        if (!pattern.matcher(email).matches()) {
            Toast. makeText(Personal_RegisterActivity.this, "Invalid email address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate card credentials length:
        if (card_number.length() != 16 || card_cvv.length() != 3) {
            Toast. makeText(Personal_RegisterActivity.this, "Invalid credit card credentials!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Make sure card number is a number:
        if (!card_number.matches("\\d+")) {
            Toast. makeText(Personal_RegisterActivity.this, "Invalid credit card number!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate card expiration date:
        // TODO: check that date is in the future
        int month = Integer.parseInt(card_month);
        int year = Integer.parseInt(card_year);

        if (month < 1 || 12 < month || year < 0 || 99 < year) {
            Toast. makeText(Personal_RegisterActivity.this, "Invalid credit card expiration date!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveUserSettings(String email, String username,
                                  String city, String street, String home_number,
                                  String card_number, String card_cvv, String card_holder_name, String card_month, String card_year) {

        Map<String, Object> address = new HashMap<>();
        address.put("city", city);
        address.put("street", street);
        address.put("number", home_number);

        Map<String, Object> expiration_date = new HashMap<>();
        expiration_date.put("month", Integer.parseInt(card_month));
        expiration_date.put("year", Integer.parseInt(card_year));

        Map<String, Object> card = new HashMap<>();
        card.put("card-holder-name", card_holder_name);
        card.put("card-number", card_number);
        card.put("security-code", Integer.parseInt(card_cvv));
        card.put("expiration-date", expiration_date);

        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        Map<String, Object> personal_accounts_entry = new HashMap<>();
        personal_accounts_entry.put("user-name", username);
        personal_accounts_entry.put("email", email);
        personal_accounts_entry.put("personal-id", user_id);
        personal_accounts_entry.put("address", address);
        personal_accounts_entry.put("credit-card", card);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("personal-accounts")
                .add(personal_accounts_entry)
                .addOnSuccessListener(documentReference_personalAccounts -> {
                    // Document added successfully.
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference_personalAccounts.getId());

                    // Add document to 'users' collection:
                    String document_id = documentReference_personalAccounts.getId();
                    DocumentReference document_reference = db.document("/personal-accounts/"+document_id);

                    Map<String, Object> users_entry = new HashMap<>();
                    users_entry.put("id", user_id);
                    users_entry.put("personal", true);
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