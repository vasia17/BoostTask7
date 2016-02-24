package com.example.shon.boost4;

import com.firebase.client.Firebase;

public class AccApplication extends android.app.Application  {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
