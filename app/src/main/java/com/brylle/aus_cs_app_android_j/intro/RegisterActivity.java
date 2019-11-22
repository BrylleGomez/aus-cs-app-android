package com.brylle.aus_cs_app_android_j.intro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.AppUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.protobuf.Any;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    /* Variables */

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference firestoreUserList;
    private DocumentReference firestoreUserCount;
    private EditText username;
    private EditText password; 
    private EditText password2; 
    private EditText firstname; 
    private EditText lastname; 
    private EditText mobile; 
    private Button buttonRegister;
    private TextView loginText;
    private ProgressBar progressBar;

    /* Activity Life Cycle Functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Retrieve instance of firebase auth and firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firestoreUserList = firebaseFirestore.collection("users");
        firestoreUserCount = firebaseFirestore.document("metadata/counts");

        // Initialize view objects
        username = findViewById(R.id.register_plaintext_username);
        password = findViewById(R.id.register_password);
        password2 = findViewById(R.id.register_password2);
        firstname = findViewById(R.id.register_plaintext_firstname);
        lastname = findViewById(R.id.register_plaintext_lastname);
        mobile = findViewById(R.id.register_mobile);
        buttonRegister = findViewById(R.id.register_button_register);
        loginText = findViewById(R.id.register_textview_noaccount);
        progressBar = findViewById(R.id.register_progressbar);

        // Hide progress bar initially
        hideProgress();

        // Attach event listeners to signup button and login text prompt
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signupUser();
            }
        });
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

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

    private void signupUser() {

        showProgress();

        // Retrieve form contents
        final String usernameString = username.getText().toString().trim();
        final String passwordString = password.getText().toString().trim();
        final String password2String = password2.getText().toString().trim();
        final String firstnameString = firstname.getText().toString().trim();
        final String lastnameString = lastname.getText().toString().trim();
        final String mobileString = mobile.getText().toString().trim();

        // Input Validation
        if (usernameString.equals("")) {
            // username is empty
            hideProgress();
            username.setError("No email entered!");
            username.requestFocus();
            return;
        }
        if (passwordString.equals("")) {
            // password is empty
            hideProgress();
            password.setError("No password entered!");
            password.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(usernameString).matches()) {
            // invalid email
            hideProgress();
            username.setError("Valid email required!");
            username.requestFocus();
            return;
        }
        if (passwordString.length() < 6) {
            // password is too short
            hideProgress();
            password.setError("Password too short! It should contain more than 6 characters.");
            password.requestFocus();
            return;
        }
        if (!passwordString.equals(password2String)) {
            // passwords not matching
            hideProgress();
            password2.setError("Passwords not matching!");
            password2.requestFocus();
            return;
        }
        if (firstnameString.equals("")) {
            // firstname is empty
            hideProgress();
            firstname.setError("No first name entered!");
            firstname.requestFocus();
            return;
        }
        if (lastnameString.equals("")) {
            // lastname is empty
            hideProgress();
            lastname.setError("No last name entered!");
            lastname.requestFocus();
            return;
        }
        if (mobileString.equals("")) {
            // mobile number is empty
            hideProgress();
            mobile.setError("No mobile number entered!");
            mobile.requestFocus();
            return;
        }
        if (mobileString.length() != 8) {
            // invalid mobile number length
            hideProgress();
            mobile.setError("Invalid mobile number!");
            mobile.requestFocus();
            return;
        }

        // Request Firebase to make account
        firebaseAuth.createUserWithEmailAndPassword(usernameString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    // add user to Firestore DB
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    addUserToDatabase(user, firstnameString, lastnameString, mobileString);

                    Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                    hideProgress();
                    firebaseAuth.signOut();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));

                } else {

                    hideProgress();
                    Toast.makeText(getApplicationContext(),"Registration Failed! Please try again...", Toast.LENGTH_SHORT).show();
                    // Clear entered text
                    username.setText("");
                    password.setText("");

                }
            }
        });

    }

    private void addUserToDatabase(FirebaseUser user, String firstname, String lastname, String mobile) {
        // adds new user as a Firestore database entry in /users

        // check if user is null
        if (user == null) {
            Toast.makeText(getApplicationContext(),"Failed to add user to database, current user not found!", Toast.LENGTH_SHORT).show();
        }

        // Retrieve user information from Firebase Authentication
        String email = user.getEmail();
        String joinDate = AppUtils.epochToDate(user.getMetadata().getCreationTimestamp());

//        // Append extension to supplied mobile number
//        String mobileExt = "+97150";
//        String completeMobile = mobileExt + mobile;
//        Toast.makeText(getApplicationContext(),"Mobile: " + completeMobile, Toast.LENGTH_SHORT).show();

        // Append extension to supplied mobile number
        String mobileExt = "+9715";
        String mobileNum = mobile;
        String completeMobile = mobileExt.concat(mobileNum);

        // Create document
        final HashMap<String,Object> entry = new HashMap<>();
        entry.put(AppUtils.KEY_FIRST_NAME, firstname);
        entry.put(AppUtils.KEY_LAST_NAME, lastname);
        entry.put(AppUtils.KEY_EMAIL, email);
        entry.put(AppUtils.KEY_MOBILE_NUMBER, completeMobile);
        entry.put(AppUtils.KEY_VOLUNTEER_HOURS, 0);

        Toast.makeText(getApplicationContext(),"Mobile: " + completeMobile, Toast.LENGTH_SHORT).show();

        firestoreUserCount.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {     // check if Firestore document exists
                    // user to be created is user_x+1 where x is no. of current users
                    final long userCount = documentSnapshot.getLong(AppUtils.KEY_USER_COUNT) + 1;

                    // Upload document to Firestore (nested inside above addOnSuccessListener)
                    firestoreUserList.document("user_" + userCount).set(entry).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"User added to Firebase", Toast.LENGTH_SHORT).show();
                                // Increment metadata/counts/user_count by 1 due to new user
                                firestoreUserCount.update(AppUtils.KEY_USER_COUNT, userCount).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),"Failed to update user count!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(),"Failed to add user to Firebase", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Log.d("RegisterActivity", "Error, Firestore Document does not exist!");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("RegisterActivity", "Failed to retrieve user count from Firestore", e);
            }
        });

    }

}
