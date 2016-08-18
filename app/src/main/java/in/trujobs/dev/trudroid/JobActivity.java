package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.Arrays;
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
import in.trujobs.proto.JobFilterRequest;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobRoleObject;
import in.trujobs.proto.JobRoleResponse;
import in.trujobs.proto.JobSearchByJobRoleRequest;
import in.trujobs.proto.JobSearchRequest;

public class JobActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private AsyncTask<JobSearchRequest, Void, JobPostResponse> mAsyncTask;
    private AsyncTask<FetchCandidateAlertRequest, Void, FetchCandidateAlertResponse> mAlertAsyncTask;

    ProgressDialog pd;
    public ListView jobPostListView;
    public AutoCompleteTextView mSearchJobAcTxtView;
    public TextView mSearchJobsByJobRoleTxtView;
    public String mSearchAddressOutput;
    public String mSearchedPlaceId;
    private FloatingActionButton fab;
    private AsyncTask<String, Void, LatLngAPIHelper> mLatLngAsyncTask;
    private AsyncTask<JobSearchRequest, Void, JobPostResponse> mJobSearchAsyncTask;

    private JobFilterFragment jobFilterFragment;
    private static Double mSearchLat;
    private static Double mSearchLng;
    private JobSearchRequest.Builder jobSearchRequest;
    public static JobSearchByJobRoleRequest.Builder jobRoles;
    public static JobFilterRequest jobFilterRequestBkp;
    public List<JobRoleObject> jobRoleObjectList;
    public List<Long> selectedJobRoleList;
    public List<Long> jobRoleIdList;

    public boolean[] checkedItems = null;
    public BiMap<Integer, Long> biMap = null;
    public BiMap<Long, Integer> invBiMap = null;

    TextView selectedJobRolesNameTxtView;

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

        selectedJobRolesNameTxtView = (TextView) findViewById(R.id.search_jobs_by_job_role);

        showJobPosts();

        Button btnFilterJob = (Button) findViewById(R.id.btn_job_filter);
        btnFilterJob.setOnClickListener(this);

        JobRoleAsyncTask fetchAllJobs = new JobRoleAsyncTask();
        fetchAllJobs.execute();


        mSearchJobsByJobRoleTxtView = (TextView) findViewById(R.id.search_jobs_by_job_role);
        mSearchJobsByJobRoleTxtView.setOnClickListener(this);

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


    /* dismissFilterPanel is directly getting called from filter_container_layout */
    public void dismissFilterPanel(View view) {
        if (jobFilterFragment != null) {
            jobFilterRequestBkp = jobFilterFragment.jobFilterRequest.build();
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(
                            R.id.overlay_job_filter_fragment_container)).commit();
        }
    }

    private void showJobPosts() {
        JobSearchReqInit();
        mAsyncTask = new JobPostAsyncTask();
        mAsyncTask.execute(jobSearchRequest.build());
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
            case R.id.search_jobs_by_job_role:
                if(jobRoleObjectList == null || jobRoleObjectList.size() == 0){
                    JobRoleAsyncTask fetchAllJobs = new JobRoleAsyncTask();
                    fetchAllJobs.execute();
                }
                showJobRolesAlertUI(jobRoleObjectList);
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

    private class JobSearchAsyncTask extends BasicJobSearchAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            jobPostListView.clearChoices();
        }

        @Override
        protected void onPostExecute(JobPostResponse jobPostResponse) {
            super.onPostExecute(jobPostResponse);
            loaderStop();
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

    private class LatLngAsyncTask extends BasicLatLngAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loaderStart();
        }

        @Override
        protected void onPostExecute(LatLngAPIHelper latLngAPIHelper) {
            super.onPostExecute(latLngAPIHelper);
            mSearchLat = latLngAPIHelper.getLatitude();
            mSearchLng = latLngAPIHelper.getLongitude();
            Tlog.i("mSearchLatLng Fetched.." + mSearchLat+"/"+mSearchLng);
            if(mSearchLat != null && mSearchLng != null ){
                Tlog.i("trigger job search on lat/lng");
                JobSearchReqInit();
                if(jobFilterRequestBkp != null) {
                    jobFilterRequestBkp.toBuilder().setJobSearchLatitude(mSearchLat);
                    jobFilterRequestBkp.toBuilder().setJobSearchLongitude(mSearchLng);
                    jobSearchRequest.setJobFilterRequest(jobFilterRequestBkp);
                    if(jobRoles != null)jobSearchRequest.setJobSearchByJobRoleRequest(jobRoles);
                    else Tlog.i("no jobRoles found");
                }
                mJobSearchAsyncTask = new JobSearchAsyncTask();
                mJobSearchAsyncTask.execute(jobSearchRequest.build());
            } else {
                showToast("Opps Something went wrong during search. Please try again");
            }

        }
    }

    private void JobSearchReqInit() {
        jobSearchRequest = JobSearchRequest.newBuilder();
        if(mSearchLat!= null)jobSearchRequest.setLatitude(mSearchLat);
        else if(Prefs.candidateHomeLat.get()!=null){
            jobSearchRequest.setLatitude(Double.parseDouble(Prefs.candidateHomeLat.get()));
            mSearchLat = Double.parseDouble(Prefs.candidateHomeLat.get());
        }
        if(mSearchLng!= null)jobSearchRequest.setLongitude(mSearchLng);
        else if(Prefs.candidateHomeLng.get()!=null){
            jobSearchRequest.setLongitude(Double.parseDouble(Prefs.candidateHomeLng.get()));
            mSearchLng = Double.parseDouble(Prefs.candidateHomeLng.get());
        }
        if(Prefs.candidateMobile.get() != null){
            jobSearchRequest.setCandidateMobile(Prefs.candidateMobile.get());
        }
    }

    private void loaderStart() {
        pd = new ProgressDialog(JobActivity.this, R.style.SpinnerTheme);
        pd.setCancelable(false);
        pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pd.show();
    }
    private void loaderStop(){
        if(pd != null){
            pd.cancel();
        }
    }

    private class JobPostAsyncTask extends AsyncTask<JobSearchRequest,
            Void, JobPostResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            loaderStart();
        }

        @Override
        protected JobPostResponse doInBackground(JobSearchRequest... params) {
            return HttpRequest.searchJobs(params[0]);
        }

        @Override
        protected void onPostExecute(JobPostResponse jobPostResponse) {
            super.onPostExecute(jobPostResponse);
            loaderStop();
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

    private class JobRoleAsyncTask extends AsyncTask<Void,
            Void, JobRoleResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JobRoleResponse doInBackground(Void... params) {
            return HttpRequest.getJobRoles();
        }

        @Override
        protected void onPostExecute(final JobRoleResponse jobRoleResponse) {
            super.onPostExecute(jobRoleResponse);
            if(jobRoleResponse!= null){
                Tlog.i("fetched all jobRoles successfully + " + jobRoleResponse.getJobRoleList().size());
                jobRoleObjectList = jobRoleResponse.getJobRoleList();
                 /* Search By Job Roles */
                selectedJobRoleList = new ArrayList<>();
                if(Prefs.candidatePrefJobRoleIdOne.get() != 0){
                    selectedJobRoleList.add(Prefs.candidatePrefJobRoleIdOne.get());
                    checkedItems[invBiMap.get(Prefs.candidatePrefJobRoleIdOne.get())] = true;
                }
                if(Prefs.candidatePrefJobRoleIdTwo.get() != 0){
                    selectedJobRoleList.add(Prefs.candidatePrefJobRoleIdTwo.get());
                    checkedItems[invBiMap.get(Prefs.candidatePrefJobRoleIdOne.get())] = true;
                }
                if(Prefs.candidatePrefJobRoleIdThree.get() != 0){
                    selectedJobRoleList.add(Prefs.candidatePrefJobRoleIdThree.get());
                    checkedItems[invBiMap.get(Prefs.candidatePrefJobRoleIdOne.get())] = true;
                }
            } else {
                showToast("Something went wrong. Please try later");
            }
        }
    }

    public void searchJobsByJobRole() {
        loaderStart();
        JobSearchRequest.Builder jobSearch;
        if(jobSearchRequest != null){
            /* take prev jobSearchReq into account before making a new jobSearch request */
            /* sets lat, lng, mobile, filter */
            jobSearch = jobSearchRequest;
        } else {
            jobSearch = JobSearchRequest.newBuilder();
            jobSearch.setLatitude(mSearchLat);
            jobSearch.setLongitude(mSearchLng);
            jobSearch.setCandidateMobile(Prefs.candidateMobile.get());
        }
        if(jobFilterRequestBkp!=null)jobSearch.setJobFilterRequest(jobFilterRequestBkp);
        if(selectedJobRoleList.size()>0){
            jobRoles = JobSearchByJobRoleRequest.newBuilder();
            if(selectedJobRoleList.size() > 0)jobRoles.setJobRoleIdOne(selectedJobRoleList.get(0));
            if(selectedJobRoleList.size() > 1)jobRoles.setJobRoleIdTwo(selectedJobRoleList.get(1));
            if(selectedJobRoleList.size() > 2)jobRoles.setJobRoleIdThree(selectedJobRoleList.get(2));

            if(selectedJobRoleList.size() > 0)jobSearch.setJobSearchByJobRoleRequest(jobRoles.build());

        }
        JobSearchAsyncTask jobSearchAsyncTask = new JobSearchAsyncTask();
        jobSearchAsyncTask.execute(jobSearch.build());
    }

    private void showJobRolesAlertUI(List<JobRoleObject> jobRoleObjectList) {
        if(biMap == null) {
            biMap = HashBiMap.create();
        }
        final CharSequence[] jobRoleNameList = new CharSequence[jobRoleObjectList.size()];
        jobRoleIdList = new ArrayList<>();
        if(checkedItems == null){
            checkedItems = new boolean[jobRoleNameList.length];
        }
        if (selectedJobRoleList.size() > 0) {
            for (Long jobRoleId : selectedJobRoleList) {
                checkedItems[invBiMap.get(jobRoleId)] = true;
                Tlog.i("checkbox["+invBiMap.get(jobRoleId)+"] marked true for jobroleid:"+jobRoleId);
            }
        }
        for (int i = 0; i < jobRoleObjectList.size(); i++) {
            // create nameList and idList
            jobRoleNameList[i] = jobRoleObjectList.get(i).getJobRoleName();
            jobRoleIdList.add(jobRoleObjectList.get(i).getJobRoleId());
            biMap.put(i, jobRoleObjectList.get(i).getJobRoleId());
        }
        invBiMap = biMap.inverse();
        final List<String> mSelectedJobsName = new ArrayList<>();


            AlertDialog alertDialog = new AlertDialog.Builder(
                this)
                .setCancelable(false)
                .setTitle("Select Job Role preference (Max 3)")
                .setPositiveButton("Done",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                /* if not selected any job roles then don't do anything*/
                                if(selectedJobRoleList!= null && selectedJobRoleList.size() > 0){
                                    for(int j=0; j<selectedJobRoleList.size();j++){
                                        Tlog.i("search job for jobRoles: "+selectedJobRoleList.get(j));
                                        String jobRoleName = jobRoleNameList[invBiMap
                                                .get(selectedJobRoleList.get(j))].toString();
                                        mSelectedJobsName.add(jobRoleName);
                                    }
                                } else {
                                    Tlog.i("clearing selectedJobRoleList");
                                    selectedJobRoleList.clear();
                                    if(jobRoles!=null)jobRoles.clear();
                                    Arrays.fill(checkedItems, false);
                                    mSelectedJobsName.add("All Jobs");
                                }
                                selectedJobRolesNameTxtView.setText(TextUtils.join(", ", mSelectedJobsName));
                                searchJobsByJobRole();
                                dialog.dismiss();
                            }
                })
                .setMultiChoiceItems(jobRoleNameList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                        if(isChecked) {
                            if (selectedJobRoleList.size() < 3) {
                                checkedItems[which] = true;
                                selectedJobRoleList.add(jobRoleIdList.get(which));
                                Tlog.i("checkBox["+which+"] for item:"+jobRoleIdList.get(which));
                            } else {
                                checkedItems[which] = false;
                                dialogInterface.dismiss();
                                searchJobsByJobRole();
                                showToast("Maximum 3 preference allowed.");
                            }
                        } else if(selectedJobRoleList.contains(jobRoleIdList.get(which))) {
                            checkedItems[which] = false;
                            Tlog.i("marked false for checkBox["+which+"] and removed item:"+jobRoleIdList.get(which));
                            selectedJobRoleList.remove(jobRoleIdList.get(which));
                        } else {
                            /* clear all */
                            selectedJobRoleList.clear();
                            Arrays.fill(checkedItems, false);
                            if(jobRoles!=null)jobRoles.clear();
                        }
                        Tlog.i("Total SelectedJobRoleSize:"+selectedJobRoleList.size());
                    }
                })
                .create();
        alertDialog.show();
    }

    /**
     * Shows a toast with the given text.
     */
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public static Double getmSearchLat() {
        return mSearchLat;
    }

    public static Double getmSearchLng() {
        return mSearchLng;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSearchLat = null;
        mSearchLng = null;
    }
}
