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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.FilterJobFragment;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
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
import in.trujobs.proto.LogoutCandidateRequest;
import in.trujobs.proto.LogoutCandidateResponse;

public class SearchJobsActivity extends TruJobsBaseActivity
        implements View.OnClickListener {

    private ProgressDialog pd;
    private int preScreenLocationIndex = 0;
    private ListView mNavigationItemListView;
    private ListView jobPostListView;
    private AutoCompleteTextView mSearchJobAcTxtView;
    private TextView mSearchJobsByJobRoleTxtView;
    public static String mSearchAddressOutput;
    private String mSearchedPlaceId;
    private DrawerLayout mDrawerLayout;
    private List<NavItem> mNavItems;
    private AsyncTask<LatLngOrPlaceIdRequest, Void, LocalityObjectResponse> mLatLngOrPlaceIdAsyncTask;
    private AsyncTask<LogoutCandidateRequest, Void, LogoutCandidateResponse> mLogoutAsyncTask;

    private static Double mSearchLat;
    private static Double mSearchLng;
    private JobSearchRequest.Builder jobSearchRequest;
    public static JobSearchByJobRoleRequest.Builder jobRolesFilter;
    public static JobFilterRequest.Builder jobFilterRequestBkp;
    private List<JobRoleObject> jobRoleObjectList;
    private List<Long> selectedJobRoleList;
    private List<Long> jobRoleIdList;

    private boolean[] checkedItems = null;
    private BiMap<Integer, Long> biMap = null;
    private BiMap<Long, Integer> invBiMap = null;
    private CharSequence[] jobRoleNameList = null;
    private int externalJobPostStartIndex = -1;

    private boolean doubleBackToExitPressedOnce = false;
    private Long jobRoleIdFromHttpIntent;

    private TextView selectedJobRolesNameTxtView;
    public static ImageView btnFilterJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        // track screen view
        addScreenViewGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS);

        if (getIntent() != null) {
            onNewIntent(getIntent());
        }
        else {
            initializeAndStartSearch();
        }
    }

    private void initializeAndStartSearch() {

        // Set views and elements
        setViewElements();

        selectedJobRolesNameTxtView = (TextView) findViewById(R.id.search_jobs_by_job_role);

        // set listeners
        setListeners();

        // fetch data for jobroles filter
        fetchAllJobRoles();

        // initialize locality text view listeners
        initializeLocalityTextView();

        //getting all matching job posts
        showJobPosts();

        //post login/signup apply
        if(Util.isLoggedIn()){
            if(Prefs.jobToApplyStatus.get() == 1L){
                //apply to the job
                GetJobPostDetailsRequest.Builder requestBuilder = GetJobPostDetailsRequest.newBuilder();
                requestBuilder.setJobPostId(Prefs.getJobToApplyJobId.get());
                requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_POST_LOGIN_APPLY_TO_JOBS);

                AsyncTask<GetJobPostDetailsRequest, Void, GetJobPostDetailsResponse> mJobPostAsyncTask = new JobPostDetailAsyncTask();
                mJobPostAsyncTask.execute(requestBuilder.build());
            }
        }
    }

    private void setViewElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Search jobs");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

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

        pd = CustomProgressDialog.get(SearchJobsActivity.this);
    }

    private void setListeners() {
        //filter_selected button
        btnFilterJob = (ImageView) findViewById(R.id.btn_job_filter);
        btnFilterJob.setOnClickListener(this);

        //job role edit icon
        ImageView editJobRole = (ImageView) findViewById(R.id.edit_job_roles_filter);
        editJobRole.setOnClickListener(this);

        //job role edit icon
        ImageView clearLocationFilter = (ImageView) findViewById(R.id.clear_location_filter);
        clearLocationFilter.setOnClickListener(this);

        mSearchJobsByJobRoleTxtView = (TextView) findViewById(R.id.search_jobs_by_job_role);
        mSearchJobsByJobRoleTxtView.setOnClickListener(this);
    }

    private void fetchAllJobRoles() {
        JobRoleAsyncTask fetchAllJobs = new JobRoleAsyncTask();
        fetchAllJobs.execute();
    }

    private void initializeLocalityTextView() {
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
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchJobAcTxtView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        mSearchJobAcTxtView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.place_autocomplete_list_item));
        mSearchJobAcTxtView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get data associated with the specified position
                // in the list (AdapterView)
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

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_SELECTED_SEARCH_LOCATION);
            }
        });

        String candidateLocalityName = Prefs.candidateHomeLocalityName.get();
        if(!candidateLocalityName.trim().isEmpty()){
            /* TODO: Find a way to make this independent of states */
            mSearchJobAcTxtView.setText(candidateLocalityName);
            mSearchAddressOutput = candidateLocalityName;
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
        TextView userNameTextView = (TextView) findViewById(R.id.userName);
        TextView userMobileTextView = (TextView) findViewById(R.id.userMobile);

        mNavItems.add(new NavItem("Search Job", R.drawable.search_icon));
        if (Util.isLoggedIn()) {
            mNavItems.add(new NavItem("My Profile", R.drawable.profile_icon));
            mNavItems.add(new NavItem("My Applications", R.drawable.list));
            mNavItems.add(new NavItem("Refer friends", R.drawable.refer_icon));
            mNavItems.add(new NavItem("Feedback", R.drawable.ic_rating));
            mNavItems.add(new NavItem("Interview Tips", R.drawable.ic_idea));
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
                    logoutUser();
                }
                openItem(WelcomeScreen.class);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_PROCEED_TO_WELCOME);

                break;
            case 1: break;

            case 2: openItem(CandidateProfileActivity.class);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_OPEN_CANDIDATE_PROFILE);
                break;

            case 3: openItem(JobApplicationActivity.class);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_OPEN_APPLIED_JOBS);
                break;

            case 4: openItem(HomeLocality.class); break;

            case 5: openItem(ReferFriends.class);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_OPEN_REFER_FRIEND);
                break;

            case 6: openItem(FeedbackActivity.class);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_INTERVIEW_TIPS, Constants.GA_ACTION_INTERVIEW_TIPS);
                break;

            case 7: openItem(InterviewTipsActivity.class);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_FEEDBACK, Constants.GA_ACTION_FEEDBACK);
                break;

            default:
                break;
        }
    }

    private void logoutUser() {
        //update candidate token
        LogoutCandidateRequest.Builder requestBuilder = LogoutCandidateRequest.newBuilder();
        requestBuilder.setCandidateId(String.valueOf(Prefs.candidateId.get()));

        if (mLogoutAsyncTask != null) {
            mLogoutAsyncTask.cancel(true);
        }
        mLogoutAsyncTask = new SearchJobsActivity.LogoutCandidateAsyncTask();
        mLogoutAsyncTask.execute(requestBuilder.build());

        //Track this action
        addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_LOGGED_OUT);

    }

    private void showJobPosts() {
        JobSearchReqInit();
        AsyncTask<JobSearchRequest, Void, JobPostResponse> mAsyncTask = new JobSearchAsyncTask();
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

                    //Track this action
                    addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_EXIT);
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                showToast("Please press back again to exit");

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_TRIED_EXIT);

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

    private void openItem(final Class<?> cls){
        mDrawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SearchJobsActivity.this, cls);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                if(cls == WelcomeScreen.class){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
            }
        }, 200);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_job_filter:
                if (findViewById(R.id.overlay_job_filter_fragment_container) != null) {
                    // Create a new Fragment to be placed in the activity layout
                    FilterJobFragment filterJobFragment = new FilterJobFragment();

                    // In case this activity was started with special instructions from an
                    // Intent, pass the Intent's extras to the fragment as arguments
                    filterJobFragment.setArguments(getIntent().getExtras());

                    // Add the fragment to the 'overlay_job_filter_fragment_container' FrameLayout
                    getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                            .add(R.id.overlay_job_filter_fragment_container, filterJobFragment).commit();

                    //Track this action
                    addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_JOB_FILTER);
                }
                break;
            case R.id.fab:
                FetchCandidateAlertRequest.Builder requestBuilder = FetchCandidateAlertRequest.newBuilder();
                requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                loaderStart();

                AsyncTask<FetchCandidateAlertRequest, Void, FetchCandidateAlertResponse> mAlertAsyncTask = new FetchAlertAsyncTask();

                mAlertAsyncTask.execute(requestBuilder.build());

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_ALERT);
                break;
            case R.id.search_jobs_by_job_role:
                showJobRolesAlertUI(jobRoleObjectList);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_SEARCH_BY_JOB_ROLE);
                break;

            case R.id.edit_job_roles_filter:
                showJobRolesAlertUI(jobRoleObjectList);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_JOB_FILTER);
                break;

            case R.id.clear_location_filter:
                mSearchJobAcTxtView.getText().clear();
                mSearchJobAcTxtView.setHint("All Bangalore");
                mSearchAddressOutput = "";
                mSearchLat = 0D;
                mSearchLng = 0D;
                jobSearchRequest.setLatitude(mSearchLat);
                jobSearchRequest.setLongitude(mSearchLng);
                jobSearchRequest.setLocalityName(mSearchAddressOutput);
                if(jobFilterRequestBkp!=null){
                    jobFilterRequestBkp.setJobSearchLatitude(mSearchLat);
                    jobFilterRequestBkp.setJobSearchLongitude(mSearchLng);
                    jobFilterRequestBkp.buildPartial();
                }
                searchJobsByJobRole();
                mSearchJobAcTxtView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_CLEAR_LOCATION);
                break;

            case R.id.search_jobs_by_place:
                mSearchJobAcTxtView.requestFocus();
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchJobAcTxtView, InputMethodManager.SHOW_IMPLICIT);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_EDIT_LOCATION);
                break;

            default:
                break;
        }
    }

    /* ----------------- Activity Required AsyncTasK Below ------------------- */

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

            if(!Util.isConnectedToInternet(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (getJobPostDetailsResponse == null) {
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
                        List<JobPostObject> list = new ArrayList<>();
                        list.add(getJobPostDetailsResponse.getJobPost());
                        JobPostAdapter jobPostAdapter = new JobPostAdapter(SearchJobsActivity.this, list, externalJobPostStartIndex);
                        jobPostAdapter.applyJob(getJobPostDetailsResponse.getJobPost().getJobPostId(), localityId[preScreenLocationIndex], null);
                        // TODO condition to check if response is already applied, or failed , accordingly allow to pass it to prescreen activity

                        dialog.dismiss();

                        //Track this action
                        addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_APPLY_TO_JOB);
                    }
                });
                applyDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        //Track this action
                        addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_CANCEL_APPLY_TO_JOB);
                    }
                });
                applyDialogBuilder.setSingleChoiceItems(localityList, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preScreenLocationIndex = which;

                        //Track this action
                        addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_SELECTED_JOB_LOCATION);
                    }
                });
                final android.support.v7.app.AlertDialog applyDialog = applyDialogBuilder.create();
                applyDialog.show();

            } else {
                showToast("Something went wrong. Unable to fetch job details!");
            }

            // unset the jobToApply status , which is part of applying without logged in
            Prefs.jobToApplyStatus.put(0);
            Prefs.getJobToApplyJobId.put(0L);
        }
    }

    private class JobSearchAsyncTask extends BasicJobSearchAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loaderStart();
            Tlog.i("------[SearchJobs] Before Calling JobSearchAsyncTask -----");
            Tlog.i("before AsyncTask mobile:"+ Prefs.candidateMobile.get());
            if(Prefs.candidateMobile.get() != null || !Prefs.candidateMobile.get().trim().isEmpty()){
                jobSearchRequest.setCandidateMobile(Prefs.candidateMobile.get()).buildPartial();
            }
            Tlog.i("jobFilter status: "+jobSearchRequest.hasJobFilterRequest());
            Tlog.i("jobSearchByJobRoleRequest status: " + jobSearchRequest.hasJobSearchByJobRoleRequest());
            Tlog.i("jobRoleToSearchfor 1: " + jobSearchRequest.getJobSearchByJobRoleRequest().getJobRoleIdOne());
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
            } else{
                updateJobPostUISearch(jobPostResponse.getJobPostList());
            }
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
            mSearchJobAcTxtView.clearFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
            if(localityObjectResponse!=null){
                if (localityObjectResponse.getStatus() == LocalityObjectResponse.Status.SUCCESS
                        && localityObjectResponse.getType() == LocalityObjectResponse.Type.FOR_PLACEID)
                {
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
                        jobSearchRequest.setLocalityName(mSearchJobAcTxtView.getText().toString());


                        AsyncTask<JobSearchRequest, Void, JobPostResponse> mJobSearchAsyncTask = new JobSearchAsyncTask();
                        mJobSearchAsyncTask.execute(jobSearchRequest.build());
                    }
                    else if(!Util.isConnectedToInternet(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                    }
                    else {
                        showToast("Opps Something went wrong during search. Please try again");
                    }
                }
            }
        }
    }

    private class FetchAlertAsyncTask extends AsyncTask<FetchCandidateAlertRequest,
            Void, FetchCandidateAlertResponse> {

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
            } else {
                ViewDialog alert = new ViewDialog();

                if (candidateAlertResponse.getAlertType() == FetchCandidateAlertResponse.Type.COMPLETE_PROFILE) {
                    alert.showDialog(SearchJobsActivity.this,
                            "Complete Your Profile", candidateAlertResponse.getAlertMessage(), "",
                            R.drawable.profile_icon, 1);
                }
                else if (candidateAlertResponse.getAlertType() == FetchCandidateAlertResponse.Type.NEW_JOBS_IN_LOCALITY) {
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
        if(mSearchLat != null) {
            jobSearchRequest.setLatitude(mSearchLat);
        }
        else if(Prefs.candidateHomeLat.get() != null){
            Tlog.i("pref lat : "+Prefs.candidateHomeLat.get());
            jobSearchRequest.setLatitude(Double.parseDouble(Prefs.candidateHomeLat.get()));
            mSearchLat = Double.parseDouble(Prefs.candidateHomeLat.get());
        }
        if(mSearchLng!= null) {
            jobSearchRequest.setLongitude(mSearchLng);
        }
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

        jobSearchRequest.setLocalityName(mSearchJobAcTxtView.getText().toString());

        if(jobRolesFilter == null) {
            jobRolesFilter = JobSearchByJobRoleRequest.newBuilder();
        }

        if (jobRoleIdFromHttpIntent != null) {
            jobRolesFilter.setJobRoleIdOne(jobRoleIdFromHttpIntent);
        }
        if(selectedJobRoleList != null && selectedJobRoleList.size()>0){
            jobRolesFilter.setJobRoleIdOne(selectedJobRoleList.get(0));
            if(selectedJobRoleList.size()>1) jobRolesFilter.setJobRoleIdTwo(selectedJobRoleList.get(1));
            if(selectedJobRoleList.size()>2) jobRolesFilter.setJobRoleIdThree(selectedJobRoleList.get(2));
        }
        else {
            if (Prefs.candidatePrefJobRoleIdOne.get() != null || Prefs.candidatePrefJobRoleIdOne.get() != 0)
                jobRolesFilter.setJobRoleIdOne(Prefs.candidatePrefJobRoleIdOne.get());
            if (Prefs.candidatePrefJobRoleIdTwo.get() != null || Prefs.candidatePrefJobRoleIdTwo.get() != 0)
                jobRolesFilter.setJobRoleIdTwo(Prefs.candidatePrefJobRoleIdTwo.get());
            if (Prefs.candidatePrefJobRoleIdThree.get() != null || Prefs.candidatePrefJobRoleIdThree.get() != 0)
                jobRolesFilter.setJobRoleIdThree(Prefs.candidatePrefJobRoleIdThree.get());
        }

        jobSearchRequest.setJobSearchByJobRoleRequest(jobRolesFilter.buildPartial());
        jobSearchRequest.buildPartial();
    }

    private void loaderStart() {
        pd.show();
    }
    private void loaderStop(){
        if(pd != null){
            pd.cancel();
        }
    }

    private void updateJobPostUISearch(List<JobPostObject> jobPostObjectList) {
        updateJobPostUI(jobPostObjectList);

        //hiding keyboard
        mSearchJobAcTxtView.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    public void updateJobPostUI(List<JobPostObject> jobPostObjectList) {
        jobPostListView = (ListView) findViewById(R.id.jobs_list_view);
        ImageView noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);

        if (jobPostObjectList.size() > 0) {

            boolean isPopularJobAvailable = false;
            externalJobPostStartIndex = -1;

            // find out start index of external job posts and whether we have atleast one popular job
            // IMP: This client assumes that the job posts are sorted from the server in the following manner:
            // All internal jobs in given sort/filter order followed by all external jobs in given sort/filter order
            for (int i = 0; i < jobPostObjectList.size(); i++) {
                if (jobPostObjectList.get(i).getJobPostSource() != 0) {
                    externalJobPostStartIndex = i;
                    break;
                }
                else {
                    isPopularJobAvailable = true;
                }
            }

            // if atleast one popular jobs is found, add 'Popular Jobs' header
            if(isPopularJobAvailable) {
                jobPostListView.removeHeaderView(findViewById(R.id.no_popular_jobs));
                jobPostListView.removeHeaderView(findViewById(R.id.start_of_result));
                jobPostListView.addHeaderView(getLayoutInflater().inflate(R.layout.start_popular_jobs, null));
            }
            else {
                jobPostListView.removeHeaderView(findViewById(R.id.no_popular_jobs));
                jobPostListView.removeHeaderView(findViewById(R.id.start_of_result));
                jobPostListView.addHeaderView(getLayoutInflater().inflate(R.layout.no_popular_jobs, null));
            }

            //adding end of search result footer view
            if(jobPostListView.getFooterViewsCount() == 0){
                jobPostListView.addFooterView(getLayoutInflater().inflate(R.layout.end_of_jobs, null));
            }

            JobPostAdapter jobPostAdapter = new JobPostAdapter(SearchJobsActivity.this,
                    jobPostObjectList, externalJobPostStartIndex);

            if(jobPostListView.getVisibility() == View.GONE
                    || jobPostListView.getVisibility() == View.INVISIBLE){
                jobPostListView.setVisibility(View.VISIBLE);
            }

            jobPostListView.setAdapter(jobPostAdapter);
            noJobsImageView.setVisibility(View.GONE);

        } else if(!Util.isConnectedToInternet(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
        }  else {
            jobPostListView.setVisibility(View.GONE);
            noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);
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

        /*if (selectedJobRoleList.size() > 0) {
            for (Long jobRoleId : selectedJobRoleList) {
                checkedItems[invBiMap.get(jobRoleId)] = true;
                Tlog.i("checkbox["+invBiMap.get(jobRoleId)+"] marked true for jobroleid:"+jobRoleId);
            }
        }*/

        for (int i = 0; i < jobRoleObjectList.size(); i++) {
            // create nameList and idList
            jobRoleNameList[i] = jobRoleObjectList.get(i).getJobRoleName();
            jobRoleIdList.add(jobRoleObjectList.get(i).getJobRoleId());
            biMap.put(i, jobRoleObjectList.get(i).getJobRoleId());
        }
        invBiMap = biMap.inverse();

        if (jobRoleIdFromHttpIntent != null) {
            selectedJobRoleList.add(jobRoleIdFromHttpIntent);
            checkedItems[invBiMap.get(jobRoleIdFromHttpIntent)] = true;
        }
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

    private void searchJobsByJobRole() {
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
        }
        if(Prefs.candidateMobile.get() != null || !Prefs.candidateMobile.get().trim().isEmpty()){
            jobSearch.setCandidateMobile(Prefs.candidateMobile.get());
        }

        if(jobFilterRequestBkp != null){
            Tlog.i("found jobFilterRequestBkp -- Misc JobFilter options set. attaching jobFilterRequestBkp to jobSearch");
            jobSearch.setJobFilterRequest(jobFilterRequestBkp);
        }
        if(selectedJobRoleList!= null && selectedJobRoleList.size()>0) {
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
                jobRolesFilter.clear().build();
                updateJobSearchObject();
            }
        }
        if(mSearchJobAcTxtView!=null) jobSearchRequest.setLocalityName(mSearchJobAcTxtView.getText().toString());

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
        searchByJobRoleBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK &&
                        event.getAction() == KeyEvent.ACTION_UP &&
                        !event.isCanceled()) {
                    if(mSelectedJobsName.size()>0){
                        mSelectedJobsName.clear();
                    }
                    mSelectedJobsName.addAll(setSelectedJobRolesNameTxtView());
                    searchJobsByJobRole();
                    dialog.cancel();
                    return true;
                }
                return false;
            }
        });

        searchByJobRoleBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                /* if not selected any job roles then don't do anything*/
                if(mSelectedJobsName.size()>0){
                    mSelectedJobsName.clear();
                }
                mSelectedJobsName.addAll(setSelectedJobRolesNameTxtView());
                searchJobsByJobRole();

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_SEARCH_BY_JOB_ROLE);
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

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_SELECTION_IN_SEARCH_BY_JOB_ROLE);
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

                        //Track this action
                        addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_CLEAR_JOB_ROLES);
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

    private List<String> setSelectedJobRolesNameTxtView() {
        List<String> mSelectedJobsName = new ArrayList<>();
        if (selectedJobRoleList != null && selectedJobRoleList.size() > 0) {
            for(int j=0; j<selectedJobRoleList.size();j++){
                Tlog.i("search job for jobRoles: " + selectedJobRoleList.get(j));
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
    protected void onNewIntent(Intent intent) {
        // reset this variable everytime a new intent starts
        // A new intent can be initited by the user from within the app or
        // a new intent could be initiated due to deep-linking
        // Only in the deeplinking intent case we will have the intent.getAction()
        // and intent.getDataString() as not null
        jobRoleIdFromHttpIntent = null;

        String action = intent.getAction();
        String data = intent.getDataString();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String[] uriParts = data.split("/");
            addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_DEEPLINK);

            if (uriParts.length > 4) {
                try {
                    jobRoleIdFromHttpIntent = Long.valueOf(data.substring(data.lastIndexOf("/") + 1));
                } catch (java.lang.NumberFormatException nEx) {
                    nEx.printStackTrace();
                }
            }
        }
        initializeAndStartSearch();
    }

    private class LogoutCandidateAsyncTask extends AsyncTask<LogoutCandidateRequest,
            Void, LogoutCandidateResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected LogoutCandidateResponse doInBackground(LogoutCandidateRequest... params) {
            return HttpRequest.logoutCandidate(params[0]);
        }

        @Override
        protected void onPostExecute(LogoutCandidateResponse logoutCandidateResponse) {
            super.onPostExecute(logoutCandidateResponse);
            mLogoutAsyncTask = null;
            pd.cancel();
            if(!Util.isConnectedToInternet(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (logoutCandidateResponse == null) {
                showToast(MessageConstants.FAILED_REQUEST);
                Log.w("","Null signIn Response");
                return;
            }

            Prefs.clearPrefValues();
            Toast.makeText(SearchJobsActivity.this, "Logout Successful",
                    Toast.LENGTH_LONG).show();

        }
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
        else if(title.equals("My Applications"))
            return 3;
        else if(title.equals("My Home Location"))
            return 4;
        else if(title.equals("Refer friends"))
            return 5;
        else if(title.equals("Feedback"))
            return 6;
        else if(title.equals("Interview Tips"))
            return 7;
        else
            return -1;
    }
}