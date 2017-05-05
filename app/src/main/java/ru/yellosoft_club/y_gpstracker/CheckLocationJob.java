package ru.yellosoft_club.y_gpstracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class CheckLocationJob extends Job {

    public static final String TAG = "check_location_job_tag";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        Log.d("Location", "Check location task triggered");
        Intent i = new Intent(MyApplication.getInstance(), LocationTrackingService.class);
        MyApplication.getInstance().startService(i);
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(CheckLocationJob.TAG)
                .setPeriodic(TimeUnit.SECONDS.toMillis(900), TimeUnit.SECONDS.toMillis(300))
                .setPersisted(true)
                .build()
                .schedule();
    }
}
