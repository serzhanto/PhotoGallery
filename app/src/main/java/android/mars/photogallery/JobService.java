package android.mars.photogallery;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

/**
 * Created by Mars on 07.08.2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobService extends android.app.job.JobService {
    private PollTask mCurrentTask;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mCurrentTask = new PollTask();
        mCurrentTask.execute(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return true;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void> {

        @Override
        protected Void doInBackground(JobParameters... jobParameterses) {
            JobParameters jobParams = jobParameterses[0];
            Log.i("JobService", "doInBackground");
            jobFinished(jobParams, false);
            return null;
        }
    }
}
