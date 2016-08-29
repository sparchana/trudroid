package in.trujobs.dev.trudroid;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import in.trujobs.dev.trudroid.Adapters.MyAppliedJobsAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.CandidateAppliedJobsRequest;
import in.trujobs.proto.CandidateAppliedJobsResponse;

public class MyAppliedJobs extends TruJobsBaseActivity {

    private AsyncTask<CandidateAppliedJobsRequest, Void, CandidateAppliedJobsResponse> mAsyncTask;
    ProgressDialog pd;

    private Integer PERMISSIONS_REQUEST_CALL_PHONE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applied_jobs);

        pd = CustomProgressDialog.get(MyAppliedJobs.this);

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
                LinearLayout myAppliedJobsMessage = (LinearLayout) findViewById(R.id.my_jobs_message);
                if(candidateAppliedJobsResponse.getStatusValue() == ServerConstants.SUCCESS){
                    if(candidateAppliedJobsResponse.getJobApplicationCount() > 0){
                        MyAppliedJobsAdapter myAppliedJobsAdapter = new MyAppliedJobsAdapter(MyAppliedJobs.this, candidateAppliedJobsResponse.getJobApplicationList());
                        myJobListView.setAdapter(myAppliedJobsAdapter);
                        myAppliedJobsMessage.setVisibility(View.VISIBLE);
                    } else {
                        myAppliedJobsMessage.setVisibility(View.GONE);
                        ImageView noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);
                        noJobsImageView.setVisibility(View.VISIBLE);
                    }
                } else{
                    myAppliedJobsMessage.setVisibility(View.GONE);
                    ImageView errorImageView = (ImageView) findViewById(R.id.something_went_wrong_image);
                    errorImageView.setVisibility(View.VISIBLE);
                    myJobListView.setVisibility(View.GONE);
                }
                myAppliedJobsMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:8048039089"));
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MyAppliedJobs.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    PERMISSIONS_REQUEST_CALL_PHONE);
                            return;
                        }
                        startActivity(callIntent);
                    }
                });
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
