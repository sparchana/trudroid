package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.JobFilterFragment;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.FetchCandidateAlertRequest;
import in.trujobs.proto.FetchCandidateAlertResponse;
import in.trujobs.proto.JobPostResponse;

public class JobActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private AsyncTask<Void, Void, JobPostResponse> mAsyncTask;
    private AsyncTask<FetchCandidateAlertRequest, Void, FetchCandidateAlertResponse> mAlertAsyncTask;

    ProgressDialog pd;
    public ListView jobPostListView;
    private Bundle jobPostExtraDetails;
    private FloatingActionButton fab;

    private JobFilterFragment jobFilterFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FetchCandidateAlertRequest.Builder requestBuilder = FetchCandidateAlertRequest.newBuilder();
                requestBuilder.setCandidateMobile(Prefs.candidateMobile.toString());

                mAlertAsyncTask = new FetchAlertAsyncTask();
                mAlertAsyncTask.execute(requestBuilder.build());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showJobPosts();

        /* Filter Actions */
        Button btnFilterJob = (Button) findViewById(R.id.btn_job_filter);

        btnFilterJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check that the activity is using the layout version with
                // the overlay_job_filter_fragment_container FrameLayout
                if (findViewById(R.id.overlay_job_filter_fragment_container) != null) {

                    // Create a new Fragment to be placed in the activity layout
                    jobFilterFragment = new JobFilterFragment();

                    // In case this activity was started with special instructions from an
                    // Intent, pass the Intent's extras to the fragment as arguments
                    jobFilterFragment.setArguments(getIntent().getExtras());

                    // Add the fragment to the 'overlay_job_filter_fragment_container' FrameLayout
                    getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .add(R.id.overlay_job_filter_fragment_container, jobFilterFragment).commit();
                }
            }
        });
    }



    public void dismissFilterPanel(View view){
        if( jobFilterFragment != null ){
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(
                            R.id.overlay_job_filter_fragment_container)).commit();
        }
    }

    private void showJobPosts(){
        mAsyncTask = new JobPostAsyncTask();
        mAsyncTask.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
            super.onBackPressed();
        }
    }

    private class JobPostAsyncTask extends AsyncTask<Void,
            Void, JobPostResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(JobActivity.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected JobPostResponse doInBackground(Void... params) {
            return HttpRequest.getJobPosts();
        }

        @Override
        protected void onPostExecute(JobPostResponse jobPostResponse) {
            super.onPostExecute(jobPostResponse);
            pd.cancel();
            if (jobPostResponse == null) {
                ImageView errorImageView = (ImageView) findViewById(R.id.something_went_wrong_image);
                ImageView noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);
                errorImageView.setVisibility(View.VISIBLE);
                jobPostListView.setVisibility(View.GONE);
                Tlog.w("Null JobPosts Response");
                return;
            }
            updateJobPostUI(jobPostResponse.getJobPostList());
        }
    }

    private void updateJobPostUI(List<JobPostObject> jobPostObjectList) {
        jobPostListView = (ListView) findViewById(R.id.jobs_list_view);
        if (jobPostObjectList.size() > 0) {
            jobPostExtraDetails = new Bundle();
            Tlog.i("DataSize: " + jobPostObjectList.size());
            JobPostAdapter jobPostAdapter = new JobPostAdapter(JobActivity.this, jobPostObjectList);
            jobPostListView.setAdapter(jobPostAdapter);
        } else {
            ImageView noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);
            noJobsImageView.setVisibility(View.VISIBLE);
            showToast("No jobs found in your locality");
        }
    }

    private class FetchAlertAsyncTask extends AsyncTask<FetchCandidateAlertRequest,
            Void, FetchCandidateAlertResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected FetchCandidateAlertResponse doInBackground(FetchCandidateAlertRequest... params) {
            return HttpRequest.fetchCandidateAlert(params[0]);
        }

        @Override
        protected void onPostExecute(FetchCandidateAlertResponse candidateAlertResponse) {
            super.onPostExecute(candidateAlertResponse);
            if (candidateAlertResponse == null) {
                Tlog.e("Null Candidate Alert Response");
                return;
            } else {
                ViewDialog alert = new ViewDialog();

                if (candidateAlertResponse.getAlertType() == FetchCandidateAlertResponse.Type.COMPLETE_PROFILE) {
                    alert.showDialog(JobActivity.this,
                            "Complete Your Profile", candidateAlertResponse.getAlertMessage(), "",
                            R.drawable.assesment, 1);
                }
                else if (candidateAlertResponse.getAlertType() == FetchCandidateAlertResponse.Type.NEW_JOBS_IN_LOCALITY) {
                    alert.showDialog(JobActivity.this,
                            "New Jobs Posted", candidateAlertResponse.getAlertMessage(), "",
                            R.drawable.assesment, 2);
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_logout) {
            Prefs.onLogout();
            Toast.makeText(JobActivity.this, "Logout Successful",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(JobActivity.this, WelcomeScreen.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
        } else if (id == R.id.nav_my_jobs) {
            Intent intent = new Intent(JobActivity.this, JobPreference.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(JobActivity.this, CandidateInfoActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
        } else if (id == R.id.nav_home_locality) {
            Intent intent = new Intent(JobActivity.this, HomeLocality.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
        }

        return true;
    }


    /**
     * Shows a toast with the given text.
     */
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
