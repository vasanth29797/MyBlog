package com.elite.blog;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by evk29 on 15-01-2018.
 */

public class SimpleBlog extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
