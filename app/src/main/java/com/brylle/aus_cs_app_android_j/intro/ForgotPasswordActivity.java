package com.brylle.aus_cs_app_android_j.intro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.view.View;
import android.widget.Toast;

import com.brylle.aus_cs_app_android_j.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    /* Variables */

    private FirebaseAuth firebaseAuth;
    private EditText email;
    private Button buttonReset;
    private ProgressBar progressBar;

    /* Initializer Functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Retrieve instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Create form objects
        email = findViewById(R.id.forgotpass_plaintext_email);
        buttonReset = findViewById(R.id.forgotpass_button_reset);
        progressBar = findViewById(R.id.forgotpass_progressbar);

        // Hide progress bar initially
        progressBar.setVisibility(View.INVISIBLE);

        // Attach event listeners to login button and signup text prompt
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }

    /* Helper Functions */

    private void resetPassword() {

        String emailString = email.getText().toString().trim();

        if (emailString.isEmpty()) {
            // username is empty
            email.setError("No email entered!");
            email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            // invalid email
            email.setError("Valid email required!");
            email.requestFocus();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth
            .sendPasswordResetEmail(emailString)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Password reset email sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Failed to send password reset email!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    }

}
