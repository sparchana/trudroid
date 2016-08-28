package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.Adapters.NavigationListAdapter;
import in.trujobs.dev.trudroid.Adapters.PlacesAutoCompleteAdapter;
import in.trujobs.dev.trudroid.CustomAsyncTask.BasicJobSearchAsyncTask;
import in.trujobs.dev.trudroid.CustomAsyncTask.BasicLatLngOrPlaceIdAsyncTask;
import in.trujobs.dev.trudroid.CustomDialog.ViewDialog;
import in.trujobs.dev.trudroid.Helper.PlaceAPIHelper;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.FilterJobFragment;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.FetchCandidateAlertRequest;
import in.trujobs.proto.FetchCandidateAlertResponse;
import in.trujobs.proto.GetJobPostDetailsRequest;
import in.trujobs.proto.GetJobPostDetailsResponse;
import in.trujobs.proto.JobFilterRequest;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobRoleObject;
import in.trujobs.proto.JobRoleResponse;
import in.trujobs.proto.JobSearchByJobRoleRequest;
import in.trujobs.proto.JobSearchRequest;
import in.trujobs.proto.LatLngOrPlaceIdRequest;
import in.trujobs.proto.LocalityObjectResponse;

public class SearchJobsActivity extends TruJobsBaseActivity
        implements View.OnClickListener {

    private AsyncTask<JobSearchRequest, Void, JobPostResponse> mAsyncTask;
    private AsyncTask<GetJobPostDetailsRequest, Void, GetJobPostDetailsResponse> mJobPostAsyncTask;
    private AsyncTask<FetchCandidateAlertRequest, Void, FetchCandidateAlertResponse> mAlertAsyncTask;

    ProgressDialog pd;
    int preScreenLocationIndex = 0;
    private ListView mNavigationItemListView;
    public ListView jobPostListView;
    public AutoCompleteTextView mSearchJobAcTxtView;
    public TextView mSearchJobsByJobRoleTxtView, userNameTextView, userMobileTextView;
    public String mSearchAddressOutput;
    public String mSearchedPlaceId;
    private DrawerLayout mDrawerLayout;
    private FloatingActionButton fab;
    private List<NavItem> mNavItems;
    private AsyncTask<LatLngOrPlaceIdRequest, Void, LocalityObjectResponse> mLatLngOrPlaceIdAsyncTask;
    private AsyncTask<JobSearchRequest, Void, JobPostResponse> mJobSearchAsyncTask;

    private FilterJobFragment filterJobFragment;
    private static Double mSearchLat;
    private static Double mSearchLng;
    private JobSearchRequest.Builder jobSearchRequest;
    public static JobSearchByJobRoleRequest.Builder jobRolesFilter;
    public static JobFilterRequest.Builder jobFilterRequestBkp;
    public List<JobRoleObject> jobRoleObjectList;
    public List<Long> selectedJobRoleList;
    public List<Long> jobRoleIdList;

    public boolean[] checkedItems = null;
    public BiMap<Integer, Long> biMap = null;
    public BiMap<Long, Integer> invBiMap = null;
    public CharSequence[] jobRoleNameList = null;

    boolean doubleBackToExitPressedOnce = false;

    TextView selectedJobRolesNameTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Search jobs");

        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Show the floating action button only if the user is logged-in
        if (Util.isLoggedIn()) {
            fab.setOnClickListener(this);
        }
        else {
            fab.setVisibility(View.GONE);
        }

        mNavigationItemListView = (ListView) findViewById(R.id.list_view_main_activity_list_view);

        //defining navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_main_activity);

        //setting side ham burger icon :P
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        //setting side navigation items
        setNavigationItems();

        //on side navigation item select
        mNavigationItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });

        selectedJobRolesNameTxtView = (TextView) findViewById(R.id.search_jobs_by_job_role);

        pd = CustomProgressDialog.get(SearchJobsActivity.this);

        //filter_selected button
        ImageView btnFilterJob = (ImageView) findViewById(R.id.btn_job_filter);
        btnFilterJob.setOnClickListener(this);

        //job role edit icon
        ImageView editJobRole = (ImageView) findViewById(R.id.edit_job_roles_filter);
        editJobRole.setOnClickListener(this);

        //job role edit icon
        ImageView clearLocationFilter = (ImageView) findViewById(R.id.clear_location_filter);
        clearLocationFilter.setOnClickListener(this);

        JobRoleAsyncTask fetchAllJobs = new JobRoleAsyncTask();
        fetchAllJobs.execute();

        mSearchJobsByJobRoleTxtView = (TextView) findViewById(R.id.search_jobs_by_job_role);
        mSearchJobsByJobRoleTxtView.setOnClickListener(this);

        /* Filter Actions */
        mSearchJobAcTxtView = (AutoCompleteTextView) findViewById(R.id.search_jobs_by_place);
        mSearchJobAcTxtView.setOnClickListener(this);
        mSearchJobAcTxtView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b && mSearchJobAcTxtView.getHint().toString().trim().equalsIgnoreCase("All Bangalore")){
                    mSearchJobAcTxtView.setHint("Start typing a location in Bangalore");
                } else if(mSearchJobAcTxtView.getHint().toString().trim().equalsIgnoreCase("Start typing a location in Bangalore")){
                    mSearchJobAcTxtView.setHint("All Bangalore");
                }
            }
        });

        //changing font from assets folder

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
                mSearchJobAcTxtView.setText(mSearchAddressOutput);
                mLatLngOrPlaceIdAsyncTask = new LatLngOrPlaceIdAsyncTask();
                LatLngOrPlaceIdRequest.Builder latLngOrPlaceIdRequest = LatLngOrPlaceIdRequest.newBuilder();
                if(!mSearchedPlaceId.trim().isEmpty()){
                    latLngOrPlaceIdRequest.setPlaceId(mSearchedPlaceId);
                }
                mLatLngOrPlaceIdAsyncTask.execute(latLngOrPlaceIdRequest.build());
            }
        });

        String candidateLocalityName = Prefs.candidateHomeLocalityName.get();
        if(!candidateLocalityName.trim().isEmpty()){
            /* TODO: Find a way to make this independent of states */
            mSearchJobAcTxtView.setText(candidateLocalityName);
        }
        //getting all the job posts
        showJobPosts();

        //post login/signup apply
        if(Util.isLoggedIn()){
            if(Prefs.jobToApplyStatus.get() == 1L){
                //apply to the job
                GetJobPostDetailsRequest.Builder requestBuilder = GetJobPostDetailsRequest.newBuilder();
                requestBuilder.setJobPostId(Prefs.getJobToApplyJobId.get());
                requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());

                mJobPostAsyncTask = new JobPostDetailAsyncTask();
                mJobPostAsyncTask.execute(requestBuilder.build());
            }
        }
    }

    private class JobPostDetailAsyncTask extends AsyncTask<GetJobPostDetailsRequest,
            Void, GetJobPostDetailsResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected GetJobPostDetailsResponse doInBackground(GetJobPostDetailsRequest... params) {
            return HttpRequest.getJobPostDetails(params[0]);
        }

        @Override
        protected void onPostExecute(final GetJobPostDetailsResponse getJobPostDetailsResponse) {
            super.onPostExecute(getJobPostDetailsResponse);

            pd.cancel();

            if (getJobPostDetailsResponse == null) {
                Toast.makeText(SearchJobsActivity.this, "Failed to Fetch details. Please try again.",
                        Toast.LENGTH_LONG).show();
                Tlog.w("","Null signIn Response");
                finish();
                return;
            }

            if(getJobPostDetailsResponse.getStatus() == GetJobPostDetailsResponse.Status.SUCCESS){
                preScreenLocationIndex = 0;
                final CharSequence[] localityList = new CharSequence[getJobPostDetailsResponse.getJobPost().getJobPostLocalityCount()];
                final Long[] localityId = new Long[getJobPostDetailsResponse.getJobPost().getJobPostLocalityCount()];
                for (int i = 0; i < getJobPostDetailsResponse.getJobPost().getJobPostLocalityCount(); i++) {
                    localityList[i] = getJobPostDetailsResponse.getJobPost().getJobPostLocality(i).getLocalityName();
                    localityId[i] = getJobPostDetailsResponse.getJobPost().getJobPostLocality(i).getLocalityId();
                }

                LinearLayout customTitleLayout = new LinearLayout(SearchJobsActivity.this);
                customTitleLayout.setPadding(30,30,30,30);
                TextView customTitle = new TextView(SearchJobsActivity.this);
                String title = "You are applying for <b>" + getJobPostDetailsResponse.getJobPost().getJobPostTitle() + "</b>  job at <b>" + getJobPostDetailsResponse.getJobPost().getJobPostCompanyName()
                        + "</b>. Please select a job Location";
                customTitle.setText(Html.fromHtml(title));
                customTitle.setTextSize(16);
                customTitleLayout.addView(customTitle);

                final android.support.v7.app.AlertDialog.Builder applyDialogBuilder = new android.support.v7.app.AlertDialog.Builder(SearchJobsActivity.this);
                applyDialogBuilder.setCancelable(true);
                applyDialogBuilder.setCustomTitle(customTitleLayout);
                applyDialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        List<JobPostObject> list = new ArrayList<JobPostObject>();
                        list.add(getJobPostDetailsResponse.getJobPost());
                        JobPostAdapter jobPostAdapter = new JobPostAdapter(SearchJobsActivity.this, list);
                        jobPostAdapter.applyJob(getJobPostDetailsResponse.getJobPost().getJobPostId(), localityId[preScreenLocationIndex], null);
                        dialog.dismiss();
                        Prefs.jobToApplyStatus.put(0);
                        Prefs.getJobToApplyJobId.put(0L);
                    }
                });
                applyDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                applyDialogBuilder.setSingleChoiceItems(localityList, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preScreenLocationIndex = which;
                    }
                });
                final android.support.v7.app.AlertDialog applyDialog = applyDialogBuilder.create();
                applyDialog.show();

                Prefs.jobToApplyStatus.put(0);
                Prefs.getJobToApplyJobId.put(0L);
            } else {
                showToast("Something went wrong. Unable to fetch job details!");
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SearchJobsActivity.jobFilterRequestBkp = null;
        SearchJobsActivity.jobRolesFilter = null;
    }

    private void setNavigationItems() {
        mNavItems = new ArrayList<>();
        userNameTextView = (TextView) findViewById(R.id.userName);
        userMobileTextView = (TextView) findViewById(R.id.userMobile);

        mNavItems.add(new NavItem("Search Job", R.drawable.search_icon));
        if (Util.isLoggedIn()) {
            mNavItems.add(new NavItem("My Profile", R.drawable.profile_icon));
            mNavItems.add(new NavItem("My Jobs", R.drawable.list));
            mNavItems.add(new NavItem("My Home Location", R.drawable.location_icon));
            mNavItems.add(new NavItem("Refer friends", R.drawable.refer_icon));
            mNavItems.add(new NavItem("Logout", R.drawable.login_icon));

            userNameTextView.setText(Prefs.firstName.get());
            userMobileTextView.setText(Prefs.candidateMobile.get());
        } else{
            mNavItems.add(new NavItem("Login/Sign Up", R.drawable.login_icon));

            userNameTextView.setText("Guest User");
            userMobileTextView.setText("Candidate not logged in");
        }
        mNavigationItemListView.setAdapter(new NavigationListAdapter(this, mNavItems));
    }

    private void selectItemFromDrawer(int position) {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }

        switch (get_index(position)) {
            case 0:
                if (Util.isLoggedIn()) {
                    Prefs.clearPrefValues();
                    Toast.makeText(SearchJobsActivity.this, "Logout Successful",
                            Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(SearchJobsActivity.this, WelcomeScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_up, R.anim.no_change); break;
            case 1: break;

            case 2: intent = new Intent(SearchJobsActivity.this, CandidateProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change); break;

            case 3: intent = new Intent(SearchJobsActivity.this, MyAppliedJobs.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change); break;

            case 4: intent = new Intent(SearchJobsActivity.this, HomeLocality.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change); break;

            case 5: intent = new Intent(SearchJobsActivity.this, ReferFriends.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change); break;

            default:
                break;
        }
    }

    private void showJobPosts() {
        JobSearchReqInit();
        mAsyncTask = new JobSearchAsyncTask();
        mAsyncTask.execute(jobSearchRequest.build());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main_activity);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //double press exit
            if(Util.isLoggedIn()){
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                showToast("Please press back again to exit");

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2500);
            }
            else{
                this.finish();
            }
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
                    filterJobFragment = new FilterJobFragment();

                    // In case this activity was started with special instructions from an
                    // Intent, pass the Intent's extras to the fragment as arguments
                    filterJobFragment.setArguments(getIntent().getExtras());

                    // Add the fragment to the 'overlay_job_filter_fragment_container' FrameLayout
                    getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                            .add(R.id.overlay_job_filter_fragment_container, filterJobFragment).commit();
                }
                break;
            case R.id.fab:
                FetchCandidateAlertRequest.Builder requestBuilder = FetchCandidateAlertRequest.newBuilder();
                requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                loaderStart();

                mAlertAsyncTask = new FetchAlertAsyncTask();

                mAlertAsyncTask.execute(requestBuilder.build());
                break;
            case R.id.search_jobs_by_job_role:
                showJobRolesAlertUI(jobRoleObjectList);
                break;

            case R.id.edit_job_roles_filter:
                showJobRolesAlertUI(jobRoleObjectList);
                break;

            case R.id.clear_location_filter:
                mSearchJobAcTxtView.getText().clear();
                mSearchJobAcTxtView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchJobAcTxtView, InputMethodManager.SHOW_IMPLICIT); break;

            case R.id.search_jobs_by_place:
                mSearchJobAcTxtView.requestFocus();
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchJobAcTxtView, InputMethodManager.SHOW_IMPLICIT); break;

            default:
                break;
        }
    }

    /* ----------------- Activity Required AsyncTasK Below ------------------- */

    private class JobSearchAsyncTask extends BasicJobSearchAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loaderStart();
            Tlog.i("before AsyncTask mobile:"+ Prefs.candidateMobile.get());
            Tlog.i("------[SearchJobs] Before Calling JobSearchAsyncTask -----");
            Tlog.i("jobFilter status: "+jobSearchRequest.hasJobFilterRequest());
            Tlog.i("jobSearchByJobRoleRequest status: " + jobSearchRequest.hasJobSearchByJobRoleRequest());
            Tlog.i("lat/lng status: " + jobSearchRequest.getLatitude() + "/" + jobSearchRequest.getLongitude());
            if(jobPostListView != null) {
                jobPostListView.clearChoices();
            }
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

    private class LatLngOrPlaceIdAsyncTask extends BasicLatLngOrPlaceIdAsyncTask {
        @Override
        protected void onPreExecute() {
            loaderStart();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(LocalityObjectResponse localityObjectResponse) {
            super.onPostExecute(localityObjectResponse);
            if(localityObjectResponse!=null){
                if (localityObjectResponse.getStatus() == LocalityObjectResponse.Status.SUCCESS
                        && localityObjectResponse.getType() == LocalityObjectResponse.Type.FOR_PLACEID) {
                    mSearchLat = localityObjectResponse.getLocality().getLat();
                    mSearchLng = localityObjectResponse.getLocality().getLng();
                    mSearchAddressOutput = localityObjectResponse.getLocality().getLocalityName();
                    Tlog.i("mSearchLatLng Fetched.." + mSearchLat + "/" + mSearchLng);
                    if (mSearchLat != null && mSearchLng != null) {
                        Tlog.i("trigger job search on lat/lng");
                        JobSearchReqInit();
                        if (jobFilterRequestBkp != null) {
                            jobFilterRequestBkp.setJobSearchLatitude(mSearchLat);
                            jobFilterRequestBkp.setJobSearchLongitude(mSearchLng);
                            jobSearchRequest.setJobFilterRequest(jobFilterRequestBkp);
                            if (jobRolesFilter != null) {
                                jobSearchRequest.setJobSearchByJobRoleRequest(jobRolesFilter);
                                Tlog.d("setting the jobSearchReq obj: "
                                        + jobSearchRequest.getJobSearchByJobRoleRequest().getJobRoleIdOne()
                                        + " | actual jobRoleFitlerObj: " + jobRolesFilter.getJobRoleIdOne());
                            } else Tlog.i("no jobRolesFilter found");
                        }
                        /* search by location input ui update */
                        mSearchJobAcTxtView.setText(mSearchAddressOutput);
                        mSearchJobAcTxtView.dismissDropDown();

                        mJobSearchAsyncTask = new JobSearchAsyncTask();
                        mJobSearchAsyncTask.execute(jobSearchRequest.build());
                    } else {
                        showToast("Opps Something went wrong during search. Please try again");
                    }
                }
            }
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
            loaderStop();
            super.onPostExecute(candidateAlertResponse);
            if (candidateAlertResponse == null) {
                Tlog.e("Null Candidate Alert Response");
                return;
            } else {
                ViewDialog alert = new ViewDialog();

                if (candidateAlertResponse.getAlertType() == FetchCandidateAlertResponse.Type.COMPLETE_PROFILE) {
                    alert.showDialog(SearchJobsActivity.this,
                            "Complete Your Profile", candidateAlertResponse.getAlertMessage(), "",
                            R.drawable.profile_icon, 1);
                } else if (candidateAlertResponse.getAlertType() == FetchCandidateAlertResponse.Type.NEW_JOBS_IN_LOCALITY) {
                    alert.showDialog(SearchJobsActivity.this,
                            "New Jobs Posted", candidateAlertResponse.getAlertMessage(), "",
                            R.drawable.job_apply, 2);
                }
                else if (candidateAlertResponse.getAlertType() == FetchCandidateAlertResponse.Type.COMPLETE_ASSESSMENT) {
                    alert.showDialog(SearchJobsActivity.this,
                            "Complete Skill Assessment", candidateAlertResponse.getAlertMessage(),
                            "Call us to know more",
                            R.drawable.assesment, 4);
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
                Tlog.i("fetched all jobRolesFilter successfully + " + jobRoleResponse.getJobRoleList().size());
                jobRoleObjectList = jobRoleResponse.getJobRoleList();
                 /* Search By Job Roles */
                 initJobRoleVars();
                 setJobRoleIdFromPrefs();
                 setSelectedJobRolesNameTxtView();
            } else {
                showToast("Something went wrong. Please try later");
            }
        }
    }


    private void JobSearchReqInit() {
        jobSearchRequest = JobSearchRequest.newBuilder();
        if(mSearchLat!= null)jobSearchRequest.setLatitude(mSearchLat);
        else if(Prefs.candidateHomeLat.get() != null){
            Tlog.i("pref lat : "+Prefs.candidateHomeLat.get());
            jobSearchRequest.setLatitude(Double.parseDouble(Prefs.candidateHomeLat.get()));
            mSearchLat = Double.parseDouble(Prefs.candidateHomeLat.get());
        }
        if(mSearchLng!= null)jobSearchRequest.setLongitude(mSearchLng);
        else if(Prefs.candidateHomeLng.get() != null){
            Tlog.i("pref lng : "+Prefs.candidateHomeLng.get());
            jobSearchRequest.setLongitude(Double.parseDouble(Prefs.candidateHomeLng.get()));
            mSearchLng = Double.parseDouble(Prefs.candidateHomeLng.get());
        }
        if(Prefs.candidateMobile.get() != null && !Prefs.candidateMobile.get().trim().isEmpty()){
            Tlog.i("mobile set:"+Prefs.candidateMobile.get());
            jobSearchRequest.setCandidateMobile(Prefs.candidateMobile.get());
        } else {
            Tlog.e("Candidate Mobile Null in Prefs");
        }
    }

    private void loaderStart() {
        pd.show();
    }
    private void loaderStop(){
        if(pd != null){
            pd.cancel();
        }
    }

    private void updateJobPostUI(List<JobPostObject> jobPostObjectList) {
        jobPostListView = (ListView) findViewById(R.id.jobs_list_view);

        Tlog.w("Job Search Response received...");
        if (jobPostObjectList.size() > 0) {
            Tlog.i("DataSize: " + jobPostObjectList.size());
            //adding end of search result footer view
            if(jobPostListView.getFooterViewsCount() == 0){
                jobPostListView.addFooterView(getLayoutInflater().inflate(R.layout.end_of_jobs, null));
            }
            JobPostAdapter jobPostAdapter = new JobPostAdapter(SearchJobsActivity.this, jobPostObjectList);
            if(jobPostListView.getVisibility() == View.GONE
                    || jobPostListView.getVisibility() == View.INVISIBLE){
                jobPostListView.setVisibility(View.VISIBLE);
            }
            jobPostListView.setAdapter(jobPostAdapter);
        } else {
            jobPostListView.setVisibility(View.GONE);
            ImageView noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);
            noJobsImageView.setVisibility(View.VISIBLE);
            showToast("No jobs found !!");
        }
    }

    private void initJobRoleVars() {
        if(biMap == null) {
            biMap = HashBiMap.create();
        }
        jobRoleIdList = new ArrayList<>();
        selectedJobRoleList = new ArrayList<>();
        jobRoleNameList = new CharSequence[jobRoleObjectList.size()];
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
    }

    private void setJobRoleIdFromPrefs() {
        Tlog.i("setting jobroleId from pref");
        if(selectedJobRoleList.size() == 0){
            if(Prefs.candidatePrefJobRoleIdOne.get() != 0){
                Tlog.i("found candidate pref job Role one..");
                selectedJobRoleList.add(Prefs.candidatePrefJobRoleIdOne.get());
                checkedItems[invBiMap.get(Prefs.candidatePrefJobRoleIdOne.get())] = true;
            }
            if(Prefs.candidatePrefJobRoleIdTwo.get() != 0){
                Tlog.i("found candidate pref job Role two..");
                selectedJobRoleList.add(Prefs.candidatePrefJobRoleIdTwo.get());
                checkedItems[invBiMap.get(Prefs.candidatePrefJobRoleIdTwo.get())] = true;
            }
            if(Prefs.candidatePrefJobRoleIdThree.get() != 0){
                Tlog.i("found candidate pref job Role three..");
                selectedJobRoleList.add(Prefs.candidatePrefJobRoleIdThree.get());
                checkedItems[invBiMap.get(Prefs.candidatePrefJobRoleIdThree.get())] = true;
            } else {
                Tlog.i("pref jobRole val are one: " + Prefs.candidatePrefJobRoleIdOne.get() +
                        "two " + Prefs.candidatePrefJobRoleIdTwo.get() +
                        "three " + Prefs.candidatePrefJobRoleIdThree.get()
                );
            }
        }
    }

    public void searchJobsByJobRole() {
        loaderStart();
        JobSearchRequest.Builder jobSearch;
        if(jobSearchRequest != null){
            /* take prev jobSearchReq into account before making a new jobSearch request */
            /* sets lat, lng, mobile, filter_selected */
            Tlog.i("found jobSearchRequest");
            jobSearch = jobSearchRequest;
        } else {
            Tlog.i("Not found jobSearchRequest. Hence created one");
            jobSearch = JobSearchRequest.newBuilder();
            jobSearch.setLatitude(mSearchLat);
            jobSearch.setLongitude(mSearchLng);
            jobSearch.setCandidateMobile(Prefs.candidateMobile.get());
        }
        if(jobFilterRequestBkp != null){
            Tlog.i("found jobFilterRequestBkp -- Misc JobFilter options set. attaching jobFilterRequestBkp to jobSearch");
            jobSearch.setJobFilterRequest(jobFilterRequestBkp);
        }
        if(selectedJobRoleList.size()>0) {
            Tlog.i("found selected jobroles, selectedJobRoleList size:"+selectedJobRoleList.size());
            jobRolesFilter = JobSearchByJobRoleRequest.newBuilder();
            if(selectedJobRoleList.size() > 0) jobRolesFilter.setJobRoleIdOne(selectedJobRoleList.get(0));
            if(selectedJobRoleList.size() > 1) jobRolesFilter.setJobRoleIdTwo(selectedJobRoleList.get(1));
            if(selectedJobRoleList.size() > 2) jobRolesFilter.setJobRoleIdThree(selectedJobRoleList.get(2));

            if(selectedJobRoleList.size() > 0){
                jobSearch.setJobSearchByJobRoleRequest(jobRolesFilter.build());
                Tlog.i("setting the jobSearchReq obj: "
                        +jobSearchRequest.getJobSearchByJobRoleRequest().getJobRoleIdOne()
                        +" | actual jobRoleFitlerObj: "+jobRolesFilter.getJobRoleIdOne());
            }
        } else {
            Tlog.i("No selected jobroles");
            if(jobRolesFilter != null){
                jobRolesFilter.clear();
                updateJobSearchObject();
            }
        }
        JobSearchAsyncTask jobSearchAsyncTask = new JobSearchAsyncTask();
        jobSearchAsyncTask.execute(jobSearch.build());
    }

    private void updateJobSearchObject() {
        if(jobSearchRequest.hasJobSearchByJobRoleRequest()){
            jobSearchRequest.setJobSearchByJobRoleRequest(jobRolesFilter);
        }
    }

    private void showJobRolesAlertUI(List<JobRoleObject> jobRoleObjectList) {
        if(jobRoleObjectList == null || jobRoleObjectList.size() == 0){
            JobRoleAsyncTask fetchAllJobs = new JobRoleAsyncTask();
            fetchAllJobs.execute();
        }

        final List<String> mSelectedJobsName = new ArrayList<>();
        final AlertDialog.Builder searchByJobRoleBuilder = new AlertDialog.Builder(this);
        searchByJobRoleBuilder.setCancelable(false);
        searchByJobRoleBuilder.setTitle("Select Job Role preference (Max 3)");
        searchByJobRoleBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                /* if not selected any job roles then don't do anything*/
                if(mSelectedJobsName.size()>0){
                    mSelectedJobsName.clear();
                }
                mSelectedJobsName.addAll(setSelectedJobRolesNameTxtView());
                searchJobsByJobRole();
            }
        });
        searchByJobRoleBuilder.setNeutralButton("Clear All", null);
        searchByJobRoleBuilder.setMultiChoiceItems(jobRoleNameList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                if(isChecked) {
                    if (selectedJobRoleList.size() < 3) {
                        checkedItems[which] = true;
                        selectedJobRoleList.add(jobRoleIdList.get(which));
                        Tlog.i("checkBox["+which+"] for item:"+jobRoleIdList.get(which));
                    } else {
                        checkedItems[which] = false;
                        ((AlertDialog) dialogInterface).getListView().setItemChecked(which, false);
                        showToast("Please select only maximum of 3 job roles.");
                    }
                } else if(selectedJobRoleList.contains(jobRoleIdList.get(which))) {
                    checkedItems[which] = false;
                    Tlog.i("marked false for checkBox["+which+"] and removed item:"+jobRoleIdList.get(which));
                    selectedJobRoleList.remove(jobRoleIdList.get(which));
                }
                Tlog.i("Total SelectedJobRoleSize:" + selectedJobRoleList.size());
            }
        });
        final AlertDialog searchByJobRoleDialog = searchByJobRoleBuilder.create();

        searchByJobRoleDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = searchByJobRoleDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedJobRoleList.clear();
                        Arrays.fill(checkedItems, false);
                        selectedJobRolesNameTxtView.setText("");
                        jobPostListView.setVisibility(View.INVISIBLE);
                        showToast("Selection Cleared.");
                        for(int which=0; which<checkedItems.length; which++){
                            ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                        }
                        //Dismiss once everything is OK.
                    }
                });
            }
        });
        searchByJobRoleDialog.show();
    }

    public static Double getmSearchLat() {
        return mSearchLat;
    }

    public static Double getmSearchLng() {
        return mSearchLng;
    }

    public List<String> setSelectedJobRolesNameTxtView() {
        List<String> mSelectedJobsName = new ArrayList<>();
        if (selectedJobRoleList != null && selectedJobRoleList.size() > 0) {
            for(int j=0; j<selectedJobRoleList.size();j++){
                Tlog.i("search job for jobRolesFilter: "+selectedJobRoleList.get(j));
                String jobRoleName = jobRoleNameList[invBiMap
                        .get(selectedJobRoleList.get(j))].toString();
                mSelectedJobsName.add(jobRoleName);
            }
        } else {
            Tlog.i("clearing selectedJobRoleList");
            selectedJobRoleList.clear();
            if(jobRolesFilter != null){
                jobRolesFilter.clear();
                updateJobSearchObject();
                Tlog.i("after clearing jobRolesFilter: size -> "+ jobRolesFilter.getJobRoleIdOne());
            }
            Tlog.i("after clearing selectedJobRoleList: size -> "+ selectedJobRoleList.size());
            Arrays.fill(checkedItems, false);
            mSelectedJobsName.add(ServerConstants.ALL_JOBS);
        }
        selectedJobRolesNameTxtView.setText(TextUtils.join(", ", mSelectedJobsName));

        return mSelectedJobsName;
    }
    @Override
    protected void onStop() {
        super.onStop();
        mSearchLat = null;
        mSearchLng = null;
    }

    private int get_index(int position){
        String title = mNavItems.get(position).mTitle;
        if(title.equals("Logout"))
            return 0;
        else if(title.equals("Login/Sign Up"))
            return 0;
        else if(title.equals("Search Job"))
            return 1;
        else if(title.equals("My Profile"))
            return 2;
        else if(title.equals("My Jobs"))
            return 3;
        else if(title.equals("My Home Location"))
            return 4;
        else if(title.equals("Refer friends"))
            return 5;
        else
            return -1;
    }
}