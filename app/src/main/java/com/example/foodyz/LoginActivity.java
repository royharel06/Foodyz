package com.example.foodyz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.foodyz.business.Business_MainActivity;
import com.example.foodyz.personal.Personal_MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);

        auth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                
                loginUser(txt_email, txt_password);
            }
        });
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    handleUserLogin();
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, StartActivity.class));
                    finish();
                }
            }
        });
    }

    private void handleUserLogin() {
        // Get user id:
        FirebaseUser user = auth.getCurrentUser();
        String user_id = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users_collection = db.collection("users");

        users_collection.whereEqualTo("id", user_id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            QuerySnapshot querySnapshot = task.getResult();

                            if (querySnapshot != null && querySnapshot.size() == 1) {
                                // Document found, retrieve the value of the "personal" field:
                                boolean is_personal = querySnapshot.getDocuments().get(0).getBoolean("personal");

                                Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                                // Direct the user accordingly:
                                if (is_personal)
                                    startActivity(new Intent(LoginActivity.this, Personal_MainActivity.class));
                                else
                                    startActivity(new Intent(LoginActivity.this, Business_MainActivity.class));

                                finish();
                            } else {
                                // Either no document found or multiple matching documents were found. Direct user to start screen:
                                Log.e("FirestoreQuery", "Could not find ID of currently authenticated user. (ID: " + user_id + ")");

                                Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, StartActivity.class));
                                finish();
                            }
                        } catch (Exception e) {
                            // Exception found. Direct user to start screen:
                            Log.e("FirestoreQuery", "Error getting documents: " + e.getMessage());

                            Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, StartActivity.class));
                            finish();
                        }
                    } else {
                        // Exception found. Direct user to start screen:
                        Log.e("FirestoreQuery", "Error getting documents: " + task.getException().getMessage());

                        Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, StartActivity.class));
                        finish();
                    }
                });
    }
}