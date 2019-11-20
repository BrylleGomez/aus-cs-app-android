package com.brylle.aus_cs_app_android_j;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CSService extends Service {
    public CSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
