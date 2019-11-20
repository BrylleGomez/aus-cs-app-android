package com.brylle.aus_cs_app_android_j;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.brylle.aus_cs_app_android_j.home.HomeActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class CSService extends Service {

//    public CSService() {
//
//    }

    // Get references to Firestore elements
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference firestoreEventList = firebaseFirestore.collection("events");

    @Override
    public void onCreate() {
        Log.d("CSAppService", "Service created");
        startListenToDB();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CSAppService", "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("CSAppService", "Service bound - not used!");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("CSAppService", "Service destroyed");
    }

    private void startListenToDB() {

        // test snapshot listener
        firestoreEventList.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("CSAppService", "Listen failed.", e);
                    return;
                }
                if (snapshots != null) {
                    Log.d("CSAppService", "EventsList updated!");
                    sendNotification("Click to View");
                }
            }
        });

    }

    private void sendNotification(String text)
    {

        // create the intent for the notification
        Intent notificationIntent = new Intent(this, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // create the pending intent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flags);

        // create the variables for the notification
        int icon = R.drawable.ic_logo;
        CharSequence tickerText = "New event from AUS Community Services!";
        CharSequence contentTitle = "New event from AUS Community Services!";
        CharSequence contentText = text;

        NotificationChannel notificationChannel = new NotificationChannel("Channel_ID", "Event Announcements", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);


        // create the notification and set its data
        Notification notification =
                new Notification.Builder(this, "Channel_ID")
                        .setSmallIcon(icon)
                        .setTicker(tickerText)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setChannelId("Channel_ID")
                        .build();

        final int NOTIFICATION_ID = 1;
        manager.notify(NOTIFICATION_ID, notification);
    }

}
