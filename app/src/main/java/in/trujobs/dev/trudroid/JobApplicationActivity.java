package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.Adapters.AppliedJobsAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.CandidateAppliedJobPostWorkFlowResponse;
import in.trujobs.proto.CandidateAppliedJobsRequest;
import in.trujobs.proto.JobPostWorkFlowObject;

public class JobApplicationActivity extends TruJobsBaseActivity {

    ProgressDialog pd;
    public CandidateAppliedJobPostWorkFlowResponse jobApplications;

    public List<JobPostWorkFlowObject> pendingTabList;
    public List<JobPostWorkFlowObject> confirmedTabList;

    public List<JobPostWorkFlowObject> rescheduledList;
    public List<JobPostWorkFlowObject> underReviewInterviewList;
    public List<JobPostWorkFlowObject> rejectedInterviewList;
    public List<JobPostWorkFlowObject> todaysInterviewList;
    public List<JobPostWorkFlowObject> upcomingInterviewList;
    public List<JobPostWorkFlowObject> pastInterviewList;
    public List<JobPostWorkFlowObject> completedInterviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_application);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("My Applied Jobs");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // track screen view
        addScreenViewGA(Constants.GA_SCREEN_NAME_APPLIED_JOBS);

        pd = CustomProgressDialog.get(JobApplicationActivity.this);

        pendingTabList = new ArrayList<>();
        confirmedTabList = new ArrayList<>();

        rescheduledList = new ArrayList<>();
        underReviewInterviewList = new ArrayList<>();
        rejectedInterviewList = new ArrayList<>();
        todaysInterviewList = new ArrayList<>();
        upcomingInterviewList = new ArrayList<>();
        pastInterviewList = new ArrayList<>();
        completedInterviewList = new ArrayList<>();

        //get details of a jobPost via AsyncTask
        getMyJobs();

    }

    public void getMyJobs(){
        CandidateAppliedJobsRequest.Builder candidateAppliedJobPostBuilder = CandidateAppliedJobsRequest.newBuilder();
        candidateAppliedJobPostBuilder.setCandidateMobile(Prefs.candidateMobile.get());
        AsyncTask<CandidateAppliedJobsRequest, Void, CandidateAppliedJobPostWorkFlowResponse> mAsyncTask = new MyAppliedJobPostAsyncTask();
        mAsyncTask.execute(candidateAppliedJobPostBuilder.build());
    }

    private class MyAppliedJobPostAsyncTask extends AsyncTask<CandidateAppliedJobsRequest,
            Void, CandidateAppliedJobPostWorkFlowResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected CandidateAppliedJobPostWorkFlowResponse doInBackground(CandidateAppliedJobsRequest... params) {
            return HttpRequest.getMyJobs(params[0]);
        }

        @Override
        protected void onPostExecute(CandidateAppliedJobPostWorkFlowResponse candidateAppliedJobPostWorkFlowResponse) {
            super.onPostExecute(candidateAppliedJobPostWorkFlowResponse);
            pd.cancel();

            if (candidateAppliedJobPostWorkFlowResponse == null) {
                Log.w("","Null my jobs Response");
                return;
            } else {
                jobApplications = candidateAppliedJobPostWorkFlowResponse;

                Calendar now = Calendar.getInstance();

                Calendar interviewCalendar = Calendar.getInstance();

                for(JobPostWorkFlowObject jwpf : candidateAppliedJobPostWorkFlowResponse.getJobPostWorkFlowObjectList()){
                    if(jwpf.getCandidateInterviewStatus() != null){
                        interviewCalendar.setTimeInMillis(jwpf.getInterviewDateMillis());
                        int interviewYear = interviewCalendar.get(Calendar.YEAR);
                        int interviewMonth = interviewCalendar.get(Calendar.MONTH) + 1;
                        int interviewDay = interviewCalendar.get(Calendar.DAY_OF_MONTH);
                        Tlog.e(jwpf.getInterviewDateMillis() + " ----- " + now.getTimeInMillis() + "------------------");

                        if(jwpf.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_RESCHEDULE){
                            rescheduledList.add(jwpf);
                        } else if(jwpf.getCandidateInterviewStatus().getStatusId() < ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_RECRUITER_SUPPORT){
                            underReviewInterviewList.add(jwpf);
                        } else if(jwpf.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_RECRUITER_SUPPORT || jwpf.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_CANDIDATE){
                            rejectedInterviewList.add(jwpf);
                        } else if(jwpf.getCandidateInterviewStatus().getStatusId() > ServerConstants.JWF_STATUS_INTERVIEW_RESCHEDULE && jwpf.getCandidateInterviewStatus().getStatusId() < ServerConstants.JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_SELECTED){
                            if((interviewDay == now.get(Calendar.DATE)) && (interviewMonth) == (now.get(Calendar.MONTH) + 1) && interviewYear == now.get(Calendar.YEAR)){
                                todaysInterviewList.add(jwpf);
                            } else if(jwpf.getInterviewDateMillis() > now.getTimeInMillis()){
                                upcomingInterviewList.add(jwpf);
                            } else{
                                pastInterviewList.add(jwpf);
                            }
                        } else if(jwpf.getCandidateInterviewStatus().getStatusId() > ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_REACHED){
                            completedInterviewList.add(jwpf);
                        }
                    } else{
                        underReviewInterviewList.add(jwpf);
                    }
                }


                //adding job application in pending list
                for(JobPostWorkFlowObject jwpf: rescheduledList){
                    pendingTabList.add(jwpf);
                }

                for(JobPostWorkFlowObject jwpf: underReviewInterviewList){
                    pendingTabList.add(jwpf);
                }

                for(JobPostWorkFlowObject jwpf: rejectedInterviewList){
                    pendingTabList.add(jwpf);
                }

                //adding job application in confirmed list
                for(JobPostWorkFlowObject jwpf: todaysInterviewList){
                    confirmedTabList.add(jwpf);
                }

                for(JobPostWorkFlowObject jwpf: upcomingInterviewList){
                    confirmedTabList.add(jwpf);
                }

                for(JobPostWorkFlowObject jwpf: pastInterviewList){
                    confirmedTabList.add(jwpf);
                }


                TabLayout tabLayout = (TabLayout) findViewById(R.id.my_jobs_tab_layout);
                tabLayout.addTab(tabLayout.newTab().setText("Pending Confirmation"));
                tabLayout.addTab(tabLayout.newTab().setText("Confirmed"));
                tabLayout.addTab(tabLayout.newTab().setText("Completed"));
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

                //pager to contain 4 tabs
                final ViewPager viewPager = (ViewPager) findViewById(R.id.my_jobs_pager);
                final AppliedJobsAdapter adapter = new AppliedJobsAdapter
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