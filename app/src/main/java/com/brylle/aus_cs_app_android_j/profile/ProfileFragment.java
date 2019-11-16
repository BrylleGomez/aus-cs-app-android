package com.brylle.aus_cs_app_android_j.profile;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.intro.LoginActivity;
import com.brylle.aus_cs_app_android_j.intro.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileFragment extends Fragment {

    /* Variables */

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference firestoreUserList = firebaseFirestore.collection("users");
    private Button buttonEdit;
    private Button buttonSave;
    private Button buttonSignout;
    private Button buttonDiscard;
    private Button buttonChangePass;
    private Button buttonRegisteredEvents;
    private ProgressBar progressBar;
    private TextView displayName;
    private TextView email;
    private TextView phoneNumber;
    private TextView volunteerHours;
    private EditText editName;
    private TextView verifyText;

    /* Initializer Functions */

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve database reference to user information
        String userEmail = currentUser.getEmail();

        // Initialize Objects
        buttonEdit = getView().findViewById(R.id.profile_button_edit);
        buttonSave = getView().findViewById(R.id.profile_button_save);
        buttonSignout = getView().findViewById(R.id.profile_button_signout);
        buttonDiscard = getView().findViewById(R.id.profile_button_discard);
        buttonChangePass = getView().findViewById(R.id.profile_button_changepass);
        buttonRegisteredEvents = getView().findViewById(R.id.profile_button_registered_events);
        progressBar = getView().findViewById(R.id.profile_progressbar);
        displayName = getView().findViewById(R.id.profile_textview_name_p);
        email = getView().findViewById(R.id.profile_textview_email_p);
        phoneNumber = getView().findViewById(R.id.profile_textview_phone_p);
        volunteerHours = getView().findViewById(R.id.profile_textview_hours_p);
        editName = getView().findViewById(R.id.profile_plaintext_editname);
        verifyText = getView().findViewById(R.id.profile_textview_email_verified);

        // Attach event listeners
        buttonSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signoutUser();
            }
        });
        buttonRegisteredEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { seeRegisteredEvents();
            }
        });
//        buttonDiscard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                discardChanges();
//            }
//        });
        // buttonChangePass.setOnClickListener {changePassword(it)}
//        verifyText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                verifyEmail();
//            }
//        });
        //        buttonEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                editProfile();
//            }
//        });
//        buttonSave.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) { updateProfile(editName.getText().toString().trim());
//            }
//        });

        // Hide edit profile objects
        buttonSave.setVisibility(View.INVISIBLE);
        buttonDiscard.setVisibility(View.INVISIBLE);
        editName.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        // Retrieve profile information from Firebase
        if(currentUser != null) {

            // Retrieve email from Firebase Auth
            email.setText(currentUser.getEmail());
            if (currentUser.isEmailVerified()) {
                verifyText.setVisibility(View.INVISIBLE);
            } else {
                verifyText.setVisibility(View.VISIBLE);
            }

            // Retrieve other user info from Firestore
            // Search for user document from Firestore/users through query
            firestoreUserList.whereEqualTo(AppUtils.KEY_EMAIL, currentUser.getEmail())      // query: look for user document that matches email of current user
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                            @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w("ProfileFragment", "Listen failed.", e);
                                                return;
                                            }

                                            if (snapshot != null && snapshot.exists()) {
                                                // Retrieve other user info
                                                String fullNameTemp = snapshot.getString(AppUtils.KEY_FIRST_NAME) + " " + snapshot.getString(AppUtils.KEY_LAST_NAME);
                                                String mobileNumberTemp = snapshot.getString(AppUtils.KEY_MOBILE_NUMBER);
                                                Long volunteerHoursTemp = snapshot.getLong(AppUtils.KEY_VOLUNTEER_HOURS);
                                                // Place retrieved user info onto user profile
                                                displayName.setText(fullNameTemp);
                                                phoneNumber.setText(mobileNumberTemp);
                                                volunteerHours.setText(volunteerHoursTemp.toString());
                                                Log.d("ProfileFragment", "Profile loaded!");
                                            } else {
                                                Log.d("ProfileFragment", "Current data: null");
                                            }
                                        }
                                    });
                                }
                            } else {
                                Log.d("ProfileFragment", "Error getting documents: ", task.getException());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("ProfileFragment", "Error fetching user through query! ", e);
                        }
                    });

        }

    }

    /* Helper Functions */

    private void signoutUser() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void seeRegisteredEvents() {
        startActivity(new Intent(getContext(), RegisteredEventsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

//    private void editProfile() {
//
//        // Toggle button visibility
//        buttonEdit.visibility = View.INVISIBLE
//        buttonChangePass.visibility = View.INVISIBLE
//        buttonSave.visibility = View.VISIBLE
//        buttonDiscard.visibility = View.VISIBLE
//
//        // Toggle fields visibility
//        displayName.visibility = View.INVISIBLE
//        editName.visibility = View.VISIBLE
//
//    }

//    private fun updateProfile(newName: String) {
//
////        // Create update object
////        val updates = UserProfileChangeRequest.Builder()
////            .setDisplayName(newName)
////            .build()
////
////        // Show progress bar to indicate processing
////        progressBar.visibility = View.VISIBLE
////
////        // Request profile change from Firebase
////        currentUser?.updateProfile(updates)
////            ?.addOnCompleteListener{ task ->
////                if(task.isSuccessful) {
////                    // Display success message and hide progress
////                    Toast.makeText(this.context, "Profile update successful!", Toast.LENGTH_SHORT).show()
////                    progressBar.visibility = View.INVISIBLE
////                    // Fetch new profile information
////                    currentUser.let{ user ->
////                        profile_textview_name_p.text = if (user?.displayName.isNullOrEmpty()) "Not Set" else user?.displayName
////                    }
////                } else {
////                    // Display failure message and hide progress
////                    Toast.makeText(this.context, "Error updating profile!", Toast.LENGTH_SHORT).show()
////                    progressBar.visibility = View.INVISIBLE
////                }
////            }
//
//        // Toggle button visibility
//        buttonEdit.visibility = View.VISIBLE
//        buttonChangePass.visibility = View.VISIBLE
//        buttonSave.visibility = View.INVISIBLE
//        buttonDiscard.visibility = View.INVISIBLE
//
//        // Toggle fields visibility
//        displayName.visibility = View.VISIBLE
//        editName.visibility = View.INVISIBLE
//
//        // Discard entered text
//        editName.text.clear()
//
//    }

//    private fun discardChanges() {
//
//        // Toggle button visibility
//        buttonEdit.visibility = View.VISIBLE
//        buttonChangePass.visibility = View.VISIBLE
//        buttonSave.visibility = View.INVISIBLE
//        buttonDiscard.visibility = View.INVISIBLE
//
//        // Toggle fields visibility
//        displayName.visibility = View.VISIBLE
//        editName.visibility = View.INVISIBLE
//
//        // Discard entered text
//        editName.text.clear()
//
//    }
//
//    private fun verifyEmail() {
//        // Send verification email to current user email address
//        currentUser?.sendEmailVerification()
//                ?.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                Toast.makeText(this.context, "Verification email sent!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this.context, "Error sending verification email!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    //    private fun changePassword(view: View) {
//        val action = ProfileFragmentDirections.actionUpdatePassword()
//        Navigation.findNavController(view).navigate(action)
//    }
//

}
