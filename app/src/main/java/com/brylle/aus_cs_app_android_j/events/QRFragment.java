package com.brylle.aus_cs_app_android_j.events;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.brylle.aus_cs_app_android_j.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class QRFragment extends Fragment {

    private Button btn;
    public TextView tvResult;

    public QRFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvResult = getView().findViewById(R.id.qr_textview_result);
        btn = getView().findViewById(R.id.qr_button_scan);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), QRScanActivity.class));
            }
        });

        Bundle arguments = this.getArguments();
        if (arguments != null) {
            String QRserial = "Scanned: " + this.getArguments().getString("qrSerial");
        }


    }
}
