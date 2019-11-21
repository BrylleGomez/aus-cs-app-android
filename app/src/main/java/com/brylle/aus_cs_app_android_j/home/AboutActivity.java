package com.brylle.aus_cs_app_android_j.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.brylle.aus_cs_app_android_j.R;

public class AboutActivity extends AppCompatActivity {

    /* Variables */

    TextView numberText;
    TextView emailText;

    /* Initializer functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize navigation toolbar
        numberText = findViewById(R.id.about_textview_phone);
        emailText = findViewById(R.id.about_textview_email);

        // Attach onClick listeners
        numberText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the Uri for the phone number
                String number = "tel:+971509832968";
                Uri callUri = Uri.parse(number);

                // create the intent and start it
                Intent callIntent = new Intent(Intent.ACTION_DIAL, callUri);
                startActivity(callIntent);
            }
        });
        emailText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                String[] addresses = {"b00068908@aus.edu", "b00067871@aus.edu"};
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Re: AUS CS App");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

    }

}
