package ru.yellosoft_club.y_gpstracker;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class LocationJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case CheckLocationJob.TAG:
                return new CheckLocationJob();
            default:
                return null;
        }
    }
}
