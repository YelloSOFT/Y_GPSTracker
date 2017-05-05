package ru.yellosoft_club.y_gpstracker;

import android.app.Application;
import android.content.Intent;

import com.evernote.android.job.JobManager;


public class MyApplication extends Application {

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.instance = this;
        JobManager.create(this).addJobCreator(new LocationJobCreator());
        CheckLocationJob.scheduleJob();
        Intent i = new Intent(MyApplication.getInstance(), LocationTrackingService.class);
        MyApplication.getInstance().startService(i);
    }

}
