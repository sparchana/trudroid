package in.trujobs.dev.trudroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.Adapters.OtherJobPostAdapter;
import in.trujobs.dev.trudroid.Adapters.PagerAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.GetJobPostDetailsRequest;
import in.trujobs.proto.GetJobPostDetailsResponse;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.LocalityObject;

public class JobDetailActivity extends AppCompatActivity {
    private static final String EXTRA_JOB_TITLE = "EXTRA_JOB_TITLE";
    private static final List<LocalityObject> EXTRA_LOCALITY = new ArrayList<LocalityObject>();
    private FloatingActionButton fab;
    ListView jobPostListView;
    Button jobTabApplyBtn;
    ProgressDialog pd;
    int preScreenLocationIndex = 0;
    private AsyncTask<GetJobPostDetailsRequest, Void, GetJobPostDetailsResponse> mAsyncTask;

    public static void start(Context context, String jobRole, List<LocalityObject> jobPostLocalityList) {
        Intent intent = new Intent(context, JobDetailActivity.class);
        intent.putExtra(EXTRA_JOB_TITLE, jobRole);
        for(LocalityObject localityObject : jobPostLocalityList){
            EXTRA_LOCALITY.add(localityObject);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);
        setTitle(getIntent().getStringExtra(EXTRA_JOB_TITLE));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewDialog alert = new ViewDialog();
                alert.showDialog(JobDetailActivity.this, "Refer Job to your friends", "If one of your friends gets hired, you get Rs. 50 recharge coupon!", "", R.drawable.refer, 3);
            }
        });
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Job"));
        tabLayout.addTab(tabLayout.newTab().setText("Company"));
/*        tabLayout.addTab(tabLayout.newTab().setText("Other Jobs"));*/
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        getDetails();
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
        mAsyncTask = new JobPostDetailAsyncTask();
        mAsyncTask.execute(requestBuilder.build());
    }

    private class JobPostDetailAsyncTask extends AsyncTask<GetJobPostDetailsRequest,
            Void, GetJobPostDetailsResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(JobDetailActivity.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
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

            TextView companyName = (TextView) findViewById(R.id.company_name);
            TextView companyLocation = (TextView) findViewById(R.id.company_location);
            TextView companyEmployees = (TextView) findViewById(R.id.company_employees);
            TextView companyType = (TextView) findViewById(R.id.company_type);
            TextView companyWebsite = (TextView) findViewById(R.id.company_website);
            TextView companyDescription = (TextView) findViewById(R.id.company_description);

            pd.cancel();

            Log.e("jobActivity", "Data: "+ getJobPostDetailsResponse);
            if (getJobPostDetailsResponse == null) {
                Toast.makeText(JobDetailActivity.this, "Failed to Fetch details. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                finish();
                return;
            }

            if(getJobPostDetailsResponse.getStatusValue() == ServerConstants.SUCCESS){

                //Set job page
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
                jobPostLocation.setText(localities);
                jobPostJobTitle.setText(getJobPostDetailsResponse.getJobPost().getJobPostTitle());
                jobPostExperience.setText(getJobPostDetailsResponse.getJobPost().getJobPostExperience().getExperienceType() + " experience");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(getJobPostDetailsResponse.getJobPost().getJobPostCreationMillis());
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                jobPostPostedOn.setText("Posted on: " + mDay + "-" + mMonth + "-" + mYear);

                String workingDays = getJobPostDetailsResponse.getJobPost().getJobPostWorkingDays();
                if(workingDays.length() > 6) {
                    String daysOff = "";
                    Log.e("workingDays:", workingDays.length() + "");
                    if (workingDays.length() > 7) {
                        workingDays = workingDays.substring(2, 8);
                    }

                    for (int i = 0; i < 7; i++) {
                        char c = workingDays.charAt(i);
                        if (c == '0') { //checking an off day
                            if (i == 0) {
                                daysOff += "Mon,";
                            } else if (i == 1) {
                                daysOff += "Tue,";
                            } else if (i == 2) {
                                daysOff += "Wed,";
                            } else if (i == 3) {
                                daysOff += "Thu,";
                            } else if (i == 4) {
                                daysOff += "Fri,";
                            } else if (i == 5) {
                                daysOff += "Sat,";
                            } else if (i == 6) {
                                daysOff += "Sun,";
                            }
                        }
                    }
                    jobPostWorkingDays.setText(daysOff.substring(0, (daysOff.length() - 1)) + " holiday");
                } else{
                    jobPostWorkingDays.setText("Off days not available");
                }

                jobPostMinReq.setText(getJobPostDetailsResponse.getJobPost().getJobPostMinRequirements());

                if(getJobPostDetailsResponse.getJobPost().getJobPostStartTime() != -1){
                    String start = "";
                    String end = "";

                    if(getJobPostDetailsResponse.getJobPost().getJobPostStartTime() > 12){
                        start = (getJobPostDetailsResponse.getJobPost().getJobPostStartTime() - 12) + "pm";
                    } else{
                        start = getJobPostDetailsResponse.getJobPost().getJobPostStartTime() + "am";
                    }

                    if(getJobPostDetailsResponse.getJobPost().getJobPostEndTime() > 12){
                        end = (getJobPostDetailsResponse.getJobPost().getJobPostEndTime() - 12) + "pm";
                    } else{
                        end = getJobPostDetailsResponse.getJobPost().getJobPostEndTime() + "am";
                    }
                    jobPostTimings.setText(start + " to " + end);
                } else{
                    jobPostTimings.setText("working hours not available");
                }
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
                        alert.showDialog(JobDetailActivity.this, getJobPostDetailsResponse.getCompany().getCompanyName() + "'s " + getJobPostDetailsResponse.getJobPost().getJobPostTitle() + " job locations:", allLocalities , "", R.drawable.location_round, 2);
                    }
                });

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
                    jobPostIncentives.setText("Information not available");
                }

                //set company page
                //set adapter for other jobs
                jobPostListView = (ListView) findViewById(R.id.company_other_jobs_list_view);
                OtherJobPostAdapter otherJobPostAdapter = new OtherJobPostAdapter(JobDetailActivity.this, getJobPostDetailsResponse.getCompany().getCompanyOtherJobsList());
/*                jobPostListView.setAdapter(otherJobPostAdapter);*/

                if(getJobPostDetailsResponse.getCompany().getCompanyLogo() != null || getJobPostDetailsResponse.getCompany().getCompanyLogo() != ""){
                    Picasso.with(getApplicationContext()).load(getJobPostDetailsResponse.getCompany().getCompanyLogo()).into(companyLogo);
                } else{
                    Picasso.with(getApplicationContext()).load("https://s3.amazonaws.com/trujobs.in/companyLogos/job.png").into(companyLogo);
                }

                companyName.setText(getJobPostDetailsResponse.getCompany().getCompanyName());
                if(getJobPostDetailsResponse.getCompany().getCompanyLocality() != null){
                    companyLocation.setText(getJobPostDetailsResponse.getCompany().getCompanyLocality().getLocalityName());
                } else{
                    companyLocation.setText("Info not available");
                }
                if(getJobPostDetailsResponse.getCompany().getCompanyEmployeeCount() != ""){
                    companyEmployees.setText(getJobPostDetailsResponse.getCompany().getCompanyEmployeeCount() + " employees");
                } else{
                    companyEmployees.setText("Info not available");
                }
                if(getJobPostDetailsResponse.getCompany().getCompanyWebsite() != ""){
                    companyWebsite.setText(getJobPostDetailsResponse.getCompany().getCompanyWebsite());
                } else {
                    companyWebsite.setText("website not available");
                }
                if(getJobPostDetailsResponse.getCompany().getCompanyType() != null){
                    companyType.setText(getJobPostDetailsResponse.getCompany().getCompanyType().getCompanyTypeName());
                } else{
                    companyType.setText("Info not Available");
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
                            alert.showDialog(JobDetailActivity.this, getJobPostDetailsResponse.getCompany().getCompanyName() , getJobPostDetailsResponse.getCompany().getCompanyDescription() , "", R.drawable.company_icon, 2);
                        }
                    });
                } else{
                    companyDescription.setText("Description not available");
                }

                jobTabApplyBtn = (Button) findViewById(R.id.job_detail_apply_btn);
                jobTabApplyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Util.isLoggedIn() == true){
                            preScreenLocationIndex = 0;
                            final CharSequence[] localityList = new CharSequence[EXTRA_LOCALITY.size()];
                            final Long[] localityId = new Long[EXTRA_LOCALITY.size()];
                            for (int i = 0; i < EXTRA_LOCALITY.size(); i++) {
                                localityList[i] = EXTRA_LOCALITY.get(i).getLocalityName();
                                localityId[i] = EXTRA_LOCALITY.get(i).getLocalityId();
                            }


                            final AlertDialog alertDialog = new AlertDialog.Builder(
                                    JobDetailActivity.this)
                                    .setCancelable(true)
                                    .setTitle("You are applying for " + getJobPostDetailsResponse.getJobPost().getJobPostTitle() + " job at " + getJobPostDetailsResponse.getCompany().getCompanyName() + ". Please select a job Location" )
                                    .setPositiveButton("Apply",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    List<JobPostObject> list = new ArrayList<JobPostObject>();
                                                    list.add(getJobPostDetailsResponse.getJobPost());
                                                    JobPostAdapter jobPostAdapter = new JobPostAdapter(JobDetailActivity.this, list);
                                                    jobPostAdapter.applyJob(getJobPostDetailsResponse.getJobPost().getJobPostId(), localityId[preScreenLocationIndex]);
                                                    dialog.dismiss();
                                                }
                                            })
                                    .setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .setSingleChoiceItems(localityList, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            preScreenLocationIndex = which;
                                        }
                                    }).create();
                            alertDialog.show();
                        } else{
                            Prefs.loginCheckStatus.put(1);
                            Intent intent = new Intent(JobDetailActivity.this, WelcomeScreen.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                            Prefs.loginCheckStatus.put(0);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}