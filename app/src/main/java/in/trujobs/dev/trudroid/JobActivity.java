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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.Adapters.PlacesAutoCompleteAdapter;
import in.trujobs.dev.trudroid.CustomAsyncTask.BasicJobSearchAsyncTask;
import in.trujobs.dev.trudroid.CustomAsyncTask.BasicLatLngAsyncTask;
import in.trujobs.dev.trudroid.Helper.LatLngAPIHelper;
import in.trujobs.dev.trudroid.Helper.PlaceAPIHelper;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.JobFilterFragment;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.FetchCandidateAlertRequest;
import in.trujobs.proto.FetchCandidateAlertResponse;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobSearchRequest;

public class JobActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private AsyncTask<Void, Void, JobPostResponse> mAsyncTask;
    private AsyncTask<FetchCandidateAlertRequest, Void, FetchCandidateAlertResponse> mAlertAsyncTask;

    ProgressDialog pd;
    public ListView jobPostListView;
    public AutoCompleteTextView mSearchJobAcTxtView;
    public String mSearchAddressOutput;
    public String mSearchedPlaceId;
    private Bundle jobPostExtraDetails;
    private FloatingActionButton fab;
    private AsyncTask<String, Void, LatLngAPIHelper> mLatLngAsyncTask;
    private AsyncTask<JobSearchRequest, Void, JobPostResponse> mJobSearchAsyncTask;

    private JobFilterFragment jobFilterFragment;
    private Double mSearchLat;
    private Double mSearchLng;
    private JobSearchRequest.Builder jobSearchRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showJobPosts();

        Button btnFilterJob = (Button) findViewById(R.id.btn_job_filter);
        btnFilterJob.setOnClickListener(this);


        /* Filter Actions */
        mSearchJobAcTxtView = (AutoCompleteTextView) findViewById(R.id.search_jobs_by_place);
        mSearchJobAcTxtView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.place_autocomplete_list_item));
        mSearchJobAcTxtView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                //mAddressOutput = (String) parent.getItemAtPosition(position);
                PlaceAPIHelper placeAPIHelper = (PlaceAPIHelper) parent.getItemAtPosition(position);
                mSearchAddressOutput = placeAPIHelper.getDescription();
                mSearchedPlaceId = placeAPIHelper.getPlaceId();
                Tlog.i("mAddressOutput ------ " + mSearchAddressOutput
                        + "\nplaceId:" + mSearchedPlaceId);
                mSearchJobAcTxtView.setText(mSearchAddressOutput.split(",")[0] + ", " + mSearchAddressOutput.split(",")[1]);
                mLatLngAsyncTask = new LatLngAsyncTask();
                mLatLngAsyncTask.execute(mSearchedPlaceId);

            }
        });
    }

    public void dismissFilterPanel(View view) {
        if (jobFilterFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(
                            R.id.overlay_job_filter_fragment_container)).commit();
        }
    }

    private void showJobPosts() {
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_job_filter:
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
                break;
            case R.id.fab:
                FetchCandidateAlertRequest.Builder requestBuilder = FetchCandidateAlertRequest.newBuilder();
                requestBuilder.setCandidateMobile(Prefs.candidateMobile.toString());

                mAlertAsyncTask = new FetchAlertAsyncTask();
                mAlertAsyncTask.execute(requestBuilder.build());
                break;
            case R.id.btn_job_search:
                if(mSearchedPlaceId!=null){
                        Tlog.i("Triggering lat/lng fetch process with "+ mSearchedPlaceId);
                        Tlog.i("lat/lng fetch completed");
                } else {
                    Tlog.e("mSearchedPlaceId is null");
                }
                break;
            default:
                break;
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

    /* ----------------- Activity Required AsyncTasK Below ------------------- */

    private  class JobSearchAsyncTask extends BasicJobSearchAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            jobPostListView.clearChoices();
        }

        @Override
        protected void onPostExecute(JobPostResponse jobPostResponse) {
            super.onPostExecute(jobPostResponse);
            pd.cancel();
            if (jobPostResponse == null) {
                ImageView errorImageView = (ImageView) findViewById(R.id.something_went_wrong_image);
                errorImageView.setVisibility(View.VISIBLE);
                jobPostListView.setVisibility(View.GONE);
                Tlog.w("Null JobPosts Response");
                return;
            }
            updateJobPostUI(jobPostResponse.getJobPostList());
        }
    }

    private  class LatLngAsyncTask extends BasicLatLngAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(JobActivity.this, R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected void onPostExecute(LatLngAPIHelper latLngAPIHelper) {
            super.onPostExecute(latLngAPIHelper);
            mSearchLat = latLngAPIHelper.getLatitude();
            mSearchLng = latLngAPIHelper.getLongitude();
            Tlog.i("mSearchLatLng Fetched.." + mSearchLat+"/"+mSearchLng);
            if(mSearchLat != null && mSearchLng != null ){
                Tlog.i("trigger job search on lat/lng");
                jobSearchRequest = JobSearchRequest.newBuilder();
                jobSearchRequest.setLatitude(mSearchLat);
                jobSearchRequest.setLongitude(mSearchLng);
                jobSearchRequest.setCandidateMobile(Prefs.candidateMobile.get());
                mJobSearchAsyncTask = new JobSearchAsyncTask();
                mJobSearchAsyncTask.execute(jobSearchRequest.build());
            } else {
                showToast("Opps Something went wrong during search. Please try again");
            }

        }
    }

    private class JobPostAsyncTask extends AsyncTask<Void,
            Void, JobPostResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(JobActivity.this, R.style.SpinnerTheme);
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
        Tlog.w("Job Search Response received...");
        if (jobPostObjectList.size() > 0) {
            jobPostExtraDetails = new Bundle();
            Tlog.i("DataSize: " + jobPostObjectList.size());
            JobPostAdapter jobPostAdapter = new JobPostAdapter(JobActivity.this, jobPostObjectList);
            if(jobPostListView.getVisibility() == View.GONE){
                jobPostListView.setVisibility(View.VISIBLE);
            }
            jobPostListView.setAdapter(jobPostAdapter);
        } else {
            jobPostListView.setVisibility(View.GONE);
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
                } else if (candidateAlertResponse.getAlertType() == FetchCandidateAlertResponse.Type.NEW_JOBS_IN_LOCALITY) {
                    alert.showDialog(JobActivity.this,
                            "New Jobs Posted", candidateAlertResponse.getAlertMessage(), "",
                            R.drawable.assesment, 2);
                }
            }
        }
    }


    /**
     * Shows a toast with the given text.
     */
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
