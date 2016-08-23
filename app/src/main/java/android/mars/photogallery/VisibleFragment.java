package android.mars.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.*;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Mars on 06.08.2016.
 */
public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";
    JobScheduler scheduler;
    final int JOB_ID = 1;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);

        scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                scheduler.cancel(jobInfo.getId());
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);

        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
            }
        }
        if (!hasBeenScheduled) {
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(getActivity(), JobService.class))
                    .setPeriodic(1000 * 60 * 1)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
        }
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
