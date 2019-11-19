package com.brylle.aus_cs_app_android_j.events;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.home.HomeActivity;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private int REQUEST_CAMERA_PERMISSION = 201;
    private Toast AttSuccess;
    int time = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_qrscan);
        try {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                AttSuccess = Toast.makeText(this, "Attendance Successfully Taken!", Toast.LENGTH_SHORT);
                scannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
                setContentView(scannerView);
                //mScannerView!!.setResultHandler(this)
                //mScannerView!!.startCamera()
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scannerView != null) {
            scannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            scannerView.startCamera();    // Start camera on resume
        }

    }

    public void onPause() {
        super.onPause();
        if (scannerView != null) {
            scannerView.stopCamera();         // Stop camera on pause
        }
    }

    @Override
    public void handleResult(Result result) {
        AttSuccess.show();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("qrData", result.getText());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}
