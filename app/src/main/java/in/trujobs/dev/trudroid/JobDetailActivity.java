package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.Adapters.PagerAdapter;
import in.trujobs.dev.trudroid.CustomDialog.ViewDialog;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.GetJobPostDetailsRequest;
import in.trujobs.proto.GetJobPostDetailsResponse;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.LocalityObject;

public class JobDetailActivity extends TruJobsBaseActivity {
    private static String EXTRA_JOB_TITLE = "EXTRA_JOB_TITLE";
    private static final List<LocalityObject> EXTRA_LOCALITY = new ArrayList<LocalityObject>();
    private FloatingActionButton fab;
    Button jobTabApplyBtn;
    ProgressDialog pd;
    int preScreenLocationIndex = 0;
    private AsyncTask<GetJobPostDetailsRequest, Void, GetJobPostDetailsResponse> mAsyncTask;

    public static void start(Context context, String jobRole, List<LocalityObject> jobPostLocalityList) {
        Intent intent = new Intent(context, JobDetailActivity.class);
        EXTRA_LOCALITY.clear();
        EXTRA_JOB_TITLE = jobRole;
        for(LocalityObject localityObject : jobPostLocalityList){
            EXTRA_LOCALITY.add(localityObject);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(EXTRA_JOB_TITLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewDialog alert = new ViewDialog();
                alert.showDialog(JobDetailActivity.this, "Refer Job to your friends", MessageConstants.REFER_MESSAGE, "", R.drawable.refer, 3);
            }
        });

        pd = CustomProgressDialog.get(JobDetailActivity.this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Job"));
        tabLayout.addTab(tabLayout.newTab().setText("Company"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //get details of a jobPost via AsyncTask
        getDetails();

        //pager to contain 2 tabs
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    public void getDetails(){
        GetJobPostDetailsRequest.Builder requestBuilder = GetJobPostDetailsRequest.newBuilder();
        requestBuilder.setJobPostId(Prefs.jobPostId.get());
        if(Util.isLoggedIn()){
            requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
        }
        mAsyncTask = new JobPostDetailAsyncTask();
        mAsyncTask.execute(requestBuilder.build());
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

            ImageView companyLogo = (ImageView) findViewById(R.id.company_logo);

            //job Tab values
            TextView jobPostPostedOn = (TextView) findViewById(R.id.posted_date);
            TextView companyNameJobScreen = (TextView) findViewById(R.id.c_name);
            TextView jobPostJobTitle = (TextView) findViewById(R.id.job_role);
            TextView jobPostLocation = (TextView) findViewById(R.id.job_location_name);
            TextView jobPostSalary = (TextView) findViewById(R.id.job_salary);
            TextView jobPostIncentives = (TextView) findViewById(R.id.job_incentive);
            TextView jobPostExperience = (TextView) findViewById(R.id.job_experience);
            TextView jobPostTimings = (TextView) findViewById(R.id.job_timing);
            TextView jobPostWorkingDays = (TextView) findViewById(R.id.job_off);
            TextView jobPostMinReq = (TextView) findViewById(R.id.job_post_min_requirement);

            //company tab values
            TextView companyName = (TextView) findViewById(R.id.company_name);
            TextView companyLocation = (TextView) findViewById(R.id.company_location);
            TextView companyEmployees = (TextView) findViewById(R.id.company_employees);
            TextView companyType = (TextView) findViewById(R.id.company_type);
            TextView companyWebsite = (TextView) findViewById(R.id.company_website);
            TextView companyDescription = (TextView) findViewById(R.id.company_description);

            pd.cancel();

            if (getJobPostDetailsResponse == null) {
                Toast.makeText(JobDetailActivity.this, "Failed to Fetch details. Please try again.",
                        Toast.LENGTH_LONG).show();
                Tlog.w("","Null signIn Response");
                finish();
                return;
            }

            if(getJobPostDetailsResponse.getStatus() == GetJobPostDetailsResponse.Status.SUCCESS){
                //Set job page

                //breaking a lot of localities in 3 localities and rest as "more"
                String localities = "";
                int localityCount = EXTRA_LOCALITY.size();
                if(localityCount > 3){
                    localityCount = 3;
                }
                for (int i = 0; i < localityCount; i++) {
                    localities += EXTRA_LOCALITY.get(i).getLocalityName();
                    if(i != (localityCount - 1)){
                        localities += ", ";
                    }
                }
                if(localityCount > 3){
                    localities += " more";
                }
                //setting job post details
                jobPostLocation.setText(localities);
                jobPostJobTitle.setText(getJobPostDetailsResponse.getJobPost().getJobPostTitle());
                jobPostExperience.setText(getJobPostDetailsResponse.getJobPost().getJobPostExperience().getExperienceType() + " experience");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(getJobPostDetailsResponse.getJobPost().getJobPostCreationMillis());
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                jobPostPostedOn.setText("Posted on: " + mDay + "-" + mMonth + "-" + mYear);

                //calculating and setting working days
                String workingDays = getJobPostDetailsResponse.getJobPost().getJobPostWorkingDays();
                if(!workingDays.equals("")) {
                    String daysOff = "";
                    if (workingDays.length() > 7) {
                        workingDays = workingDays.substring(2, 8);
                    }
                    for (int i = 0; i < 7; i++) {
                        char c = workingDays.charAt(i);

                        if(c == '0'){ //checking an off day
                            switch (i){
                                case 0: daysOff += "Mon, "; break;
                                case 1: daysOff += "Tue, "; break;
                                case 2: daysOff += "Wed, "; break;
                                case 3: daysOff += "Thu, "; break;
                                case 4: daysOff += "Fri, "; break;
                                case 5: daysOff += "Sat, "; break;
                                case 6: daysOff += "Sun, "; break;
                            }
                        }
                    }
                    if(daysOff.equals("")){
                        jobPostWorkingDays.setText("No holiday");
                    } else{
                        jobPostWorkingDays.setText(daysOff + " holiday");
                    }
                } else{
                    jobPostWorkingDays.setText("Off days: Info not specified");
                }

                //setting min requirements
                jobPostMinReq.setText(getJobPostDetailsResponse.getJobPost().getJobPostMinRequirements());

                //setting start and end time requirements
                if(getJobPostDetailsResponse.getJobPost().getJobPostStartTime() != -1){
                    String start;
                    String end;

                    //conversion from 24 hrs format to 12 hr format
                    //start time conversion
                    if(getJobPostDetailsResponse.getJobPost().getJobPostStartTime() > 12){
                        start = (getJobPostDetailsResponse.getJobPost().getJobPostStartTime() - 12) + "pm";
                    } else{
                        start = getJobPostDetailsResponse.getJobPost().getJobPostStartTime() + "am";
                    }

                    //end time conversion
                    if(getJobPostDetailsResponse.getJobPost().getJobPostEndTime() > 12){
                        end = (getJobPostDetailsResponse.getJobPost().getJobPostEndTime() - 12) + "pm";
                    } else{
                        end = getJobPostDetailsResponse.getJobPost().getJobPostEndTime() + "am";
                    }
                    jobPostTimings.setText(start + " to " + end);
                } else{
                    jobPostTimings.setText("working hours not available");
                }

                //alert dialog to show all the localities with comma separated
                jobPostLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String allLocalities = "";
                        for (int i = 0; i < EXTRA_LOCALITY.size(); i++) {
                            allLocalities += EXTRA_LOCALITY.get(i).getLocalityName();
                            if(i != (EXTRA_LOCALITY.size() - 1)){
                                allLocalities += ", ";
                            }
                        }
                        ViewDialog alert = new ViewDialog();
                        alert.showDialog(JobDetailActivity.this, getJobPostDetailsResponse.getCompany().getCompanyName() + "'s " + getJobPostDetailsResponse.getJobPost().getJobPostTitle() + " job locations:", allLocalities , "", R.drawable.location_round, -1);
                    }
                });

                //converting min and max salaries in #,### format
                DecimalFormat formatter = new DecimalFormat("#,###");
                if(getJobPostDetailsResponse.getJobPost().getJobPostMaxSalary() != 0){
                    jobPostSalary.setText("₹" + formatter.format(getJobPostDetailsResponse.getJobPost().getJobPostMinSalary()) + " - ₹" + formatter.format(getJobPostDetailsResponse.getJobPost().getJobPostMaxSalary()));
                } else{
                    jobPostSalary.setText("₹" + formatter.format(getJobPostDetailsResponse.getJobPost().getJobPostMinSalary()));
                }
                companyNameJobScreen.setText(getJobPostDetailsResponse.getCompany().getCompanyName());
                if(getJobPostDetailsResponse.getJobPost().getJobPostIncentives() != ""){
                    jobPostIncentives.setText(getJobPostDetailsResponse.getJobPost().getJobPostIncentives());
                } else{
                    jobPostIncentives.setText("Incentives: Not Specified");
                }

                TextView otherJobTextView = (TextView) findViewById(R.id.other_job_header);

                LinearLayout otherJobListView = (LinearLayout) findViewById(R.id.other_job_list_view);
                //setting other jobs in Other job section
                if(getJobPostDetailsResponse.getCompany().getCompanyOtherJobsCount() > 0){

                    otherJobTextView.setText("Other Jobs at " + getJobPostDetailsResponse.getCompany().getCompanyName());
                    //set adapter for other jobs
                    for(final JobPostObject jobPostObject : getJobPostDetailsResponse.getCompany().getCompanyOtherJobsList()){
                        LayoutInflater inflater = null;
                        inflater = (LayoutInflater) JobDetailActivity.this
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View mLinearView = inflater.inflate(R.layout.company_other_jobs_list_item, null);
                        TextView mJobPostTitleTextView = (TextView) mLinearView.findViewById(R.id.company_other_job_title);
                        TextView mJobPostSalary = (TextView) mLinearView.findViewById(R.id.company_other_job_min_salary);

                        //setting title of the job Post
                        mJobPostTitleTextView.setText(jobPostObject.getJobPostTitle());

                        //setting salary of the job post
                        if(jobPostObject.getJobPostMaxSalary() != 0){
                            mJobPostSalary.setText("₹" + formatter.format(jobPostObject.getJobPostMinSalary()) + " - ₹" + formatter.format(jobPostObject.getJobPostMaxSalary()));
                        } else{
                            mJobPostSalary.setText("₹" + formatter.format(jobPostObject.getJobPostMinSalary()));
                        }
                        //adding the job view
                        otherJobListView.addView(mLinearView);

                        mLinearView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.e("Other job post", "data: " + jobPostObject);
                                Prefs.jobPostId.put(jobPostObject.getJobPostId());
                                JobDetailActivity.start(JobDetailActivity.this, jobPostObject.getJobRole(), jobPostObject.getJobPostLocalityList());
                            }
                        });
                    }
                } else{
                    //no other jobs found
                    otherJobTextView.setVisibility(View.GONE);
                }



                //setting company tab info
                //setting logo
                if(getJobPostDetailsResponse.getCompany().getCompanyLogo() != null || getJobPostDetailsResponse.getCompany().getCompanyLogo() != ""){
                    Picasso.with(getApplicationContext()).load(getJobPostDetailsResponse.getCompany().getCompanyLogo()).into(companyLogo);
                } else{
                    Picasso.with(getApplicationContext()).load("https://s3.amazonaws.com/trujobs.in/companyLogos/job.png").into(companyLogo);
                }

                companyName.setText(getJobPostDetailsResponse.getCompany().getCompanyName());

                if(getJobPostDetailsResponse.getCompany().getCompanyLocality() != null){
                    companyLocation.setText(getJobPostDetailsResponse.getCompany().getCompanyLocality().getLocalityName());
                } else{
                    companyLocation.setText("Company Location: Info Not Specified");
                }

                if(getJobPostDetailsResponse.getCompany().getCompanyEmployeeCount() != ""){
                    companyEmployees.setText(getJobPostDetailsResponse.getCompany().getCompanyEmployeeCount() + " employees");
                } else{
                    companyEmployees.setText("No. of Employees: Info Not Specified");
                }

                if(getJobPostDetailsResponse.getCompany().getCompanyWebsite() != ""){
                    companyWebsite.setText(getJobPostDetailsResponse.getCompany().getCompanyWebsite());
                } else {
                    companyWebsite.setText("Company website: Info Not Specified");
                }

                if(getJobPostDetailsResponse.getCompany().getCompanyType().getCompanyTypeName() != ""){
                    companyType.setText(getJobPostDetailsResponse.getCompany().getCompanyType().getCompanyTypeName());
                } else{
                    companyType.setText("Company Type: Info Not Specified");
                }

                if(getJobPostDetailsResponse.getCompany().getCompanyDescription() != ""){
                    companyDescription.setText(getJobPostDetailsResponse.getCompany().getCompanyDescription());
                    int len = getJobPostDetailsResponse.getCompany().getCompanyDescription().length();
                    if (len > 150) {
                        companyDescription.setText(getJobPostDetailsResponse.getCompany().getCompanyDescription().substring(0,150) + "...");
                    }
                    companyDescription.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ViewDialog alert = new ViewDialog();
                            alert.showDialog(JobDetailActivity.this, getJobPostDetailsResponse.getCompany().getCompanyName() , getJobPostDetailsResponse.getCompany().getCompanyDescription() , "", R.drawable.company_icon, -1);
                        }
                    });
                } else{
                    companyDescription.setText("Company Description: Info Not Specified");
                }

                jobTabApplyBtn = (Button) findViewById(R.id.job_detail_apply_btn);
                if(getJobPostDetailsResponse.getAlreadyApplied()){
                    jobTabApplyBtn.setText("Applied");
                    jobTabApplyBtn.setBackgroundColor(getResources().getColor(R.color.back_grey_dark));
                    jobTabApplyBtn.setEnabled(false);
                }

                jobTabApplyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Util.isLoggedIn()){
                            preScreenLocationIndex = 0;
                            final CharSequence[] localityList = new CharSequence[EXTRA_LOCALITY.size()];
                            final Long[] localityId = new Long[EXTRA_LOCALITY.size()];
                            for (int i = 0; i < EXTRA_LOCALITY.size(); i++) {
                                localityList[i] = EXTRA_LOCALITY.get(i).getLocalityName();
                                localityId[i] = EXTRA_LOCALITY.get(i).getLocalityId();
                            }

                            LinearLayout customTitleLayout = new LinearLayout(JobDetailActivity.this);
                            customTitleLayout.setPadding(30,30,30,30);
                            TextView customTitle = new TextView(JobDetailActivity.this);
                            String title = "You are applying for <b>" + getJobPostDetailsResponse.getJobPost().getJobPostTitle() + "</b>  job at <b>" + getJobPostDetailsResponse.getJobPost().getJobPostCompanyName()
                                    + "</b>. Please select a job Location";
                            customTitle.setText(Html.fromHtml(title));
                            customTitle.setTextSize(16);
                            customTitleLayout.addView(customTitle);

                            final android.support.v7.app.AlertDialog.Builder applyDialogBuilder = new android.support.v7.app.AlertDialog.Builder(JobDetailActivity.this);
                            applyDialogBuilder.setCancelable(true);
                            applyDialogBuilder.setCustomTitle(customTitleLayout);
                            applyDialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    List<JobPostObject> list = new ArrayList<JobPostObject>();
                                    list.add(getJobPostDetailsResponse.getJobPost());
                                    JobPostAdapter jobPostAdapter = new JobPostAdapter(JobDetailActivity.this, list);
                                    jobPostAdapter.applyJob(getJobPostDetailsResponse.getJobPost().getJobPostId(), localityId[preScreenLocationIndex], jobTabApplyBtn);
                                    dialog.dismiss();
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
                        } else{
                            Toast.makeText(JobDetailActivity.this, "Please login/sign up to apply.",
                                    Toast.LENGTH_LONG).show();
                            Prefs.jobToApplyStatus.put(1);
                            Prefs.getJobToApplyJobId.put(getJobPostDetailsResponse.getJobPost().getJobPostId());
                            Intent intent = new Intent(JobDetailActivity.this, WelcomeScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                            finish();
                        }
                    }
                });

            } else {
                Toast.makeText(JobDetailActivity.this, "Something went wrong. Please try again later!",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}