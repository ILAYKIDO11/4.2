package com.example.project1311;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity2 extends AppCompatActivity
{
    private TextInputEditText editTextEmail, editTextPassword;
    private MaterialButton btnSignIn, btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth and Database instances
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if a user is already signed in
        if (mAuth.getCurrentUser() != null)
        {
            navigateToMain();
            return; // Exit method to avoid showing login UI
        }

        setContentView(R.layout.activity_main2);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);

        btnSignIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (validateForm())
                {
                    signIn();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (validateForm())
                {
                    register();
                }
            }
        });
    }

    private void signIn()
    {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Sign in with Firebase using email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            initializeUserScores(mAuth.getCurrentUser());
                            Toast.makeText(MainActivity2.this, "Authentication successful.",
                                    Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                        else
                        {
                            Toast.makeText(MainActivity2.this, "Authentication failed: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void register()
    {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Register new user with Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            initializeUserScores(mAuth.getCurrentUser());
                            Toast.makeText(MainActivity2.this, "Registration successful.",
                                    Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                        else
                        {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException)
                            {
                                Toast.makeText(MainActivity2.this,
                                        "Email already registered. Please sign in.",
                                        Toast.LENGTH_LONG).show();
                                editTextEmail.setError("Email already in use");
                            }
                            else
                            {
                                Toast.makeText(MainActivity2.this, "Registration failed: " +
                                                task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void initializeUserScores(FirebaseUser user)
    {
        String userId = user.getUid();
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        // Save user's email in database
        userRef.child("email").setValue(user.getEmail());

        DatabaseReference userScoresRef = userRef.child("scores");

        // Initialize 'test' score if it doesn't exist
        userScoresRef.child("test").get().addOnCompleteListener(task ->
        {
            if (!task.isSuccessful() || task.getResult().getValue() == null)
            {
                userScoresRef.child("test").setValue(0);
            }
        });

        // Initialize 'numbers' score if it doesn't exist
        userScoresRef.child("numbers").get().addOnCompleteListener(task ->
        {
            if (!task.isSuccessful() || task.getResult().getValue() == null)
            {
                userScoresRef.child("numbers").setValue(0);
            }
        });

        // Initialize 'family' score if it doesn't exist
        userScoresRef.child("family").get().addOnCompleteListener(task ->
        {
            if (!task.isSuccessful() || task.getResult().getValue() == null)
            {
                userScoresRef.child("family").setValue(0);
            }
        });
    }

    private void navigateToMain()
    {
        Intent intent = new Intent(MainActivity2.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close this activity to prevent back navigation
    }

    private boolean validateForm()
    {
        boolean valid = true;
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email))
        {
            editTextEmail.setError("Required");
            valid = false;
        }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            editTextEmail.setError("Enter a valid email");
            valid = false;
        }

        if (TextUtils.isEmpty(password))
        {
            editTextPassword.setError("Required");
            valid = false;
        }
        else if (password.length() < 6)
        {
            editTextPassword.setError("Password must be at least 6 characters");
            valid = false; // Firebase requirement
        }

        return valid;
    }
}