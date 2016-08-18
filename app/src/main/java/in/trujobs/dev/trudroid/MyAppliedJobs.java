package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.Collections;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.Adapters.MyAppliedJobsAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.CandidateAppliedJobsRequest;
import in.trujobs.proto.CandidateAppliedJobsResponse;
import in.trujobs.proto.JobPostResponse;

public class MyAppliedJobs extends AppCompatActivity {

    private AsyncTask<CandidateAppliedJobsRequest, Void, CandidateAppliedJobsResponse> mAsyncTask;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applied_jobs);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("My Applied Jobs");
        getMyJobs();
    }

    public void getMyJobs(){
        CandidateAppliedJobsRequest.Builder candidateAppliedJobPostBuilder = CandidateAppliedJobsRequest.newBuilder();
        candidateAppliedJobPostBuilder.setCandidateMobile(Prefs.candidateMobile.get());
        mAsyncTask = new MyAppliedJobPostAsyncTask();
        mAsyncTask.execute(candidateAppliedJobPostBuilder.build());
    }

    private class MyAppliedJobPostAsyncTask extends AsyncTask<CandidateAppliedJobsRequest,
            Void, CandidateAppliedJobsResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MyAppliedJobs.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected CandidateAppliedJobsResponse doInBackground(CandidateAppliedJobsRequest... params) {
            return HttpRequest.getMyJobs(params[0]);
        }

        @Override
        protected void onPostExecute(CandidateAppliedJobsResponse candidateAppliedJobsResponse) {
            super.onPostExecute(candidateAppliedJobsResponse);
            pd.cancel();
            ListView myJobListView = (ListView) findViewById(R.id.my_jobs_list_view);
            if (candidateAppliedJobsResponse == null) {
                ImageView errorImageView = (ImageView) findViewById(R.id.something_went_wrong_image);
                errorImageView.setVisibility(View.VISIBLE);
                myJobListView.setVisibility(View.GONE);
                Log.w("","Null my jobs Response");
                return;
            } else {
                if(candidateAppliedJobsResponse.getJobApplicationCount() > 0){
                    MyAppliedJobsAdapter myAppliedJobsAdapter = new MyAppliedJobsAdapter(MyAppliedJobs.this, candidateAppliedJobsResponse.getJobApplicationList());
                    myJobListView.setAdapter(myAppliedJobsAdapter);
                } else {
                    ImageView noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);
                    noJobsImageView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
