package com.brylle.aus_cs_app_android_j.intro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.home.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends Activity {

    /* Variables */

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private EditText username;
    private EditText password;
    private Button buttonLogin;
    private TextView signupText;
    private TextView forgotText;
    private ProgressBar progressBar;

    /* Activity Life Cycle Functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Retrieve instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Create form objects
        username = findViewById(R.id.login_plaintext_username);
        password = findViewById(R.id.login_plaintext_password);
        buttonLogin = findViewById(R.id.login_button_login);
        signupText = findViewById(R.id.login_textview_noaccount);
        forgotText = findViewById(R.id.login_textview_forgotpass);
        progressBar = findViewById(R.id.login_progressbar);

        // Hide progress bar initially
        hideProgress();

        // Attach event listeners to login button and signup text prompt
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginValidation();
            }
        });
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
        forgotText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //If a user is already logged in, jumps to HomeActivity
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // if email of user is not verified yet, do not allow log in
            if (!currentUser.isEmailVerified()) {
                firebaseAuth.signOut();
                Toast.makeText(getApplicationContext(), "Login failed! Email not verified.", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        }
    }

    /* Helper Functions */

    private void hideProgress() {
        // hides the progress bar
        progressBar.setVisibility(View.GONE);
    }

    private void showProgress() {
        // shows the progress bar
        progressBar.setVisibility(View.VISIBLE);
    }

    private void loginValidation() {

        showProgress();

        final String usernameString = username.getText().toString().trim();
        final String passwordString = password.getText().toString().trim();

        // Input Validation (fields not empty)
        if (username.getText().toString().equals("")) {
            // username is empty
            hideProgress();
            username.setError("No email entered!");
            username.requestFocus();
            return;
        }
        if (password.getText().toString().equals("")) {
            // password is empty
            hideProgress();
            password.setError("No password entered!");
            password.requestFocus();
            return;
        }

        // Check if credentials belong to a normal user; if not, disallow login
        // Normal users and admin users pertain to different database structures
        // Allowing this interchange would result to the app crashing as parts of the code
        // request Firebase Firestore fields that are non-existent (either for admin or for user)
        firebaseFirestore.collection("users").whereEqualTo(AppUtils.KEY_EMAIL, usernameString)      // query: look for user document in Firestore using email of current user
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot fetchedUser : queryDocumentSnapshots) {      // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                            boolean isAdmin = fetchedUser.getBoolean(AppUtils.KEY_IS_ADMIN);
                            if (!isAdmin) {      // if user is not admin, proceed to login
                                loginUser(usernameString, passwordString);
                            } else {            // if user is admin, login denied
                                hideProgress();
                                Toast.makeText(getApplicationContext(), "Login failed! Invalid credentials...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("EventsFragment", "1: Error fetching user through query! ", e);
                    }
                });

    }

    private void loginUser(String usernameString, String passwordString) {

        firebaseAuth.signInWithEmailAndPassword(usernameString,passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    hideProgress();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    if (currentUser != null) {
                        if (!currentUser.isEmailVerified()) {
                            firebaseAuth.signOut();
                            // Clear entered credentials
                            username.setText("");
                            password.setText("");
                            Toast.makeText(getApplicationContext(), "Login failed! Email not verified.", Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            Log.d("LoginActivity", "Login Successful");
                        }
                    }
                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    hideProgress();
                    Toast.makeText(getApplicationContext(), "Login failed! Invalid credentials...", Toast.LENGTH_SHORT).show();
                    // Clear entered credentials
                    username.setText("");
                    password.setText("");
                } else {
                    hideProgress();
                    Toast.makeText(getApplicationContext(), "Error logging in!", Toast.LENGTH_SHORT).show();
                    // Clear entered credentials
                    username.setText("");
                    password.setText("");
                    String error = task.getException().toString();
                    Log.d("LoginActivity", error);
                }
            }
        });

    }

    private void forgotPassword() {
        startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
    }

}
