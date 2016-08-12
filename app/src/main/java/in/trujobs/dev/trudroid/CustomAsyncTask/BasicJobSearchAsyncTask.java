package in.trujobs.dev.trudroid.CustomAsyncTask;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobSearchRequest;

/**
 * Created by zero on 12/8/16.
 */
public class BasicJobSearchAsyncTask extends AsyncTask<JobSearchRequest, Void, JobPostResponse> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Tlog.i("Fetching Jobs for Provided lat/lng ....");
    }

    @Override
    protected JobPostResponse doInBackground(JobSearchRequest... params) {
        return HttpRequest.getJobsForLatLng(params[0]);
    }

    @Override
    protected void onPostExecute(JobPostResponse jobPostResponse) {
        super.onPostExecute(jobPostResponse);
    }
}