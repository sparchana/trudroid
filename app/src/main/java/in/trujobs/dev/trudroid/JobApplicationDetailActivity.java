package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Calendar;

import in.trujobs.dev.trudroid.Adapters.MyConfirmedJobsAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.JobPostWorkFlowObject;
import in.trujobs.proto.NotGoingReasonRequest;
import in.trujobs.proto.UpdateCandidateStatusRequest;
import in.trujobs.proto.UpdateCandidateStatusResponse;
import in.trujobs.proto.UpdateInterviewRequest;
import in.trujobs.proto.UpdateInterviewResponse;

public class JobApplicationDetailActivity extends TruJobsBaseActivity {

    private ProgressDialog pd;
    private Integer globalCandidateStatus = 0;
    private Integer selectedNotGoingReasonIndex = 0;
    private Integer PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private Long globalJpId = 0L;
    private AsyncTask<UpdateCandidateStatusRequest, Void, UpdateCandidateStatusResponse> mCandidateStatusAsyncTask;
    AsyncTask<UpdateInterviewRequest, Void, UpdateInterviewResponse> mAsyncTask;

    private NotGoingReasonResponse mNotGoingAsyncTask;
    private static JobPostWorkFlowObject JPWFObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_application_detail);

        CollapsingToolbarLayout collapsingToolbarLayout;

        ImageView companyLogo = (ImageView) findViewById(R.id.company_logo);

        LinearLayout callHotline = (LinearLayout) findViewById(R.id.call_hotline);
        LinearLayout interviewTips = (LinearLayout) findViewById(R.id.interview_tips);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(JPWFObject.getJobPostObject().getJobPostTitle());

        Picasso.with(JobApplicationDetailActivity.this).load(JPWFObject.getJobPostObject().getJobPostCompanyLogo()).into(companyLogo);

        TextView companyNameTv = (TextView) findViewById(R.id.company_name);
        TextView companyAddressTv = (TextView) findViewById(R.id.company_address);
        TextView jpSalaryTv = (TextView) findViewById(R.id.jp_salary);
        TextView jpExperienceTv = (TextView) findViewById(R.id.jp_experience);
        TextView jpEducationTv = (TextView) findViewById(R.id.jp_education);
        TextView applicationStatusTv = (TextView) findViewById(R.id.application_status);
        TextView recNameTv = (TextView) findViewById(R.id.rec_name);
        TextView interviewDateTv = (TextView) findViewById(R.id.interview_date);
        TextView interviewTimeTv = (TextView) findViewById(R.id.interview_time);
        TextView currentStatusTv = (TextView) findViewById(R.id.current_status);

        LinearLayout navPanel = (LinearLayout) findViewById(R.id.navigate_panel);
        LinearLayout acceptRejectPanel = (LinearLayout) findViewById(R.id.reschedule_panel);
        LinearLayout statusPanel = (LinearLayout) findViewById(R.id.candidate_status_panel);
        LinearLayout statusOptions = (LinearLayout) findViewById(R.id.status_options);
        LinearLayout interviewDateSlot = (LinearLayout) findViewById(R.id.interview_date_slot);

        callHotline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:8880007799"));
                if (ActivityCompat.checkSelfPermission(JobApplicationDetailActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(JobApplicationDetailActivity.this,
                            new String[]{android.Manifest.permission.CALL_PHONE},
                            PERMISSIONS_REQUEST_CALL_PHONE);
                    return;
                }
                startActivity(callIntent);
            }
        });

        interviewTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobApplicationDetailActivity.this, InterviewTipsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });

        //candidate status options
        LinearLayout notGoingLayout = (LinearLayout) findViewById(R.id.not_going);
        LinearLayout delayedLayout = (LinearLayout) findViewById(R.id.delayed);
        LinearLayout startedLayout = (LinearLayout) findViewById(R.id.started);
        LinearLayout reachedLayout = (LinearLayout) findViewById(R.id.reached);

        // accept/reject interview options
        LinearLayout acceptInterview = (LinearLayout) findViewById(R.id.accept_interview);
        LinearLayout rejectInterview = (LinearLayout) findViewById(R.id.reject_interview);

        navPanel.setVisibility(View.GONE);
        acceptRejectPanel.setVisibility(View.GONE);
        statusPanel.setVisibility(View.GONE);
        statusOptions.setVisibility(View.GONE);

        String title = "<b>" + JPWFObject.getJobPostObject().getJobPostCompanyName() + "</b>";
        companyNameTv.setText(Html.fromHtml(title));

        companyAddressTv.setText(JPWFObject.getJobPostObject().getJobPostAddress());

        //set job Application salary
        DecimalFormat formatter = new DecimalFormat("#,###");
        if(JPWFObject.getJobPostObject().getJobPostMaxSalary() != 0){
            jpSalaryTv.setText("₹" + formatter.format(JPWFObject.getJobPostObject().getJobPostMinSalary()) + " - ₹" + formatter.format(JPWFObject.getJobPostObject().getJobPostMaxSalary()));
        } else{
            jpSalaryTv.setText("₹" + formatter.format(JPWFObject.getJobPostObject().getJobPostMinSalary()));
        }

        //set job Application experience
        jpExperienceTv.setText(JPWFObject.getJobPostObject().getJobPostExperience().getExperienceType());

        //set job Application education
        if(JPWFObject.getJobPostObject().getEducation().isInitialized()){
            jpEducationTv.setText(JPWFObject.getJobPostObject().getEducation().getEducationName());
        }

        //set job recruiter name
        recNameTv.setText(JPWFObject.getJobPostObject().getRecruiterName());

        if(JPWFObject.getInterviewDateMillis() != 0){
            //set interview date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(JPWFObject.getInterviewDateMillis());
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH) + 1;
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            int mToday = calendar.get(Calendar.DAY_OF_WEEK);

            String cDay = mDay + "";

            if(mDay < 10){
                cDay = "0" + mDay;
            }

            String finalDate = Util.getDay(mToday) + ", " + cDay + " " + Util.getMonth(mMonth) + " " + mYear;

            interviewDateTv.setText(finalDate);

            //set interview time slot
            interviewTimeTv.setText(JPWFObject.getInterviewTimeSlotObject().getSlotTitle());
            interviewDateSlot.setVisibility(View.VISIBLE);
        } else{
            interviewDateSlot.setVisibility(View.GONE);
        }

        //set job Application status
        if(JPWFObject.getCandidateInterviewStatus() != null){
            if(JPWFObject.getCandidateInterviewStatus().getStatusId() > ServerConstants.JWF_STATUS_INTERVIEW_RESCHEDULE && JPWFObject.getCandidateInterviewStatus().getStatusId() < ServerConstants.JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_SELECTED){
                applicationStatusTv.setText("Interview Confirmed");

                //show direction option
                navPanel.setVisibility(View.VISIBLE);
                if(JPWFObject.getInterviewLat() != 0.0){
                    navPanel.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onClick(View v) {
                            double destinationLatitude = JPWFObject.getInterviewLat();
                            double destinationLongitude = JPWFObject.getInterviewLng();

                            String url = "http://maps.google.com/maps?f=d&daddr="+ destinationLatitude+","+destinationLongitude+"&dirflg=d&layer=t";
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                            startActivity(intent);
                        }
                    });
                } else{
                    navPanel.setVisibility(View.GONE);
                }

                //checking candidate status
                Calendar now = Calendar.getInstance();

                Calendar interviewCalendar = Calendar.getInstance();

                interviewCalendar.setTimeInMillis(JPWFObject.getInterviewDateMillis());
                int interviewYear = interviewCalendar.get(Calendar.YEAR);
                int interviewMonth = interviewCalendar.get(Calendar.MONTH) + 1;
                int interviewDay = interviewCalendar.get(Calendar.DAY_OF_MONTH);

                //checking today interview
                if((interviewDay == now.get(Calendar.DATE)) && (interviewMonth) == (now.get(Calendar.MONTH) + 1) && interviewYear == now.get(Calendar.YEAR)){
                    recNameTv.setText(JPWFObject.getJobPostObject().getRecruiterName() + " [" + JPWFObject.getJobPostObject().getRecruiterMobile() + "] ");
                    if(JPWFObject.getCandidateInterviewStatus().getStatusId() > ServerConstants.JWF_STATUS_INTERVIEW_RESCHEDULE
                            && JPWFObject.getCandidateInterviewStatus().getStatusId() < ServerConstants.JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_SELECTED)
                    {

                        statusPanel.setVisibility(View.VISIBLE);
                        if(JPWFObject.getCandidateInterviewStatus().getStatusId() > ServerConstants.JWF_STATUS_INTERVIEW_CONFIRMED){
                            currentStatusTv.setText(JPWFObject.getCandidateInterviewStatus().getStatusTitle());

                            if(JPWFObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_NOT_GOING || JPWFObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_DELAYED){
                                currentStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                            } else{
                                currentStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                            }
                        } else{
                            currentStatusTv.setText("Status not updated");
                        }

                        if(JPWFObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_REACHED){ //reached

                            //if already reached, don't show the options
                            statusOptions.setVisibility(View.GONE);
                        } else {

                            //else show the options
                            statusOptions.setVisibility(View.VISIBLE);
                        }

                        //on click methods
                        notGoingLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateCandidateStatus(JPWFObject.getJobPostObject().getJobPostId(), ServerConstants.CANDIDATE_STATUS_NOT_GOING_VAL);
                            }
                        });

                        delayedLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateCandidateStatus(JPWFObject.getJobPostObject().getJobPostId(), ServerConstants.CANDIDATE_STATUS_DELAYED_VAL);
                            }
                        });

                        startedLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateCandidateStatus(JPWFObject.getJobPostObject().getJobPostId(), ServerConstants.CANDIDATE_STATUS_STARTED_VAL);
                            }
                        });

                        reachedLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateCandidateStatus(JPWFObject.getJobPostObject().getJobPostId(), ServerConstants.CANDIDATE_STATUS_REACHED_VAL);
                            }
                        });

                    } else{
                        statusPanel.setVisibility(View.GONE);
                    }
                } else{
                    statusPanel.setVisibility(View.GONE);
                }

                applicationStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
            } else if(JPWFObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_RESCHEDULE){

                acceptRejectPanel.setVisibility(View.VISIBLE);
                // accept/reject options
                acceptInterview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateRescheduledInterviewConfirmation(ServerConstants.CANDIDATE_STATUS_RESCHEDULED_INTERVIEW_ACCEPT, JPWFObject.getJobPostObject().getJobPostId());
                    }
                });

                rejectInterview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateRescheduledInterviewConfirmation(ServerConstants.CANDIDATE_STATUS_RESCHEDULED_INTERVIEW_REJECT, JPWFObject.getJobPostObject().getJobPostId());
                    }
                });

                applicationStatusTv.setText("Interview Rescheduled");
                applicationStatusTv.setTextColor(getResources().getColor(R.color.colorLightOrange));
            } else if(JPWFObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_CANDIDATE ||
                    JPWFObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_RECRUITER_SUPPORT){
                applicationStatusTv.setText("Not Shortlisted");
                applicationStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
            } else if(JPWFObject.getCandidateInterviewStatus().getStatusId() > ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_REACHED){
                if(JPWFObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_SELECTED){
                    applicationStatusTv.setText("Selected");
                    applicationStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                } else{
                    applicationStatusTv.setText("Rejected");
                    applicationStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                }
            } else{
                applicationStatusTv.setText("Under Review");
                applicationStatusTv.setTextColor(getResources().getColor(R.color.colorLightOrange));
            }
        }

    }

    private void updateRescheduledInterviewConfirmation(Integer value, Long jpId){
        UpdateInterviewRequest.Builder updateInterviewRequestBuilder = UpdateInterviewRequest.newBuilder();
        updateInterviewRequestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
        updateInterviewRequestBuilder.setInterviewStatus(value);
        updateInterviewRequestBuilder.setJpId(jpId);

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }

        mAsyncTask = new UpdateInterviewAsyncTask();
        mAsyncTask.execute(updateInterviewRequestBuilder.build());
    }

    public class UpdateInterviewAsyncTask extends AsyncTask<UpdateInterviewRequest,
            Void, UpdateInterviewResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(JobApplicationDetailActivity.this);
            pd.show();
        }

        @Override
        protected UpdateInterviewResponse doInBackground(UpdateInterviewRequest... params) {
            return HttpRequest.updateInterview(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateInterviewResponse updateInterviewResponse) {
            super.onPostExecute(updateInterviewResponse);
            mAsyncTask = null;
            pd.cancel();
            if(updateInterviewResponse.getStatus().getNumber() == UpdateInterviewResponse.Status.SUCCESS_VALUE){
                showToast("Interview Updated!");
                finish();
                Intent intent = new Intent(JobApplicationDetailActivity.this, JobApplicationActivity.class);
                startActivity(intent);
            } else{
                showToast("Something went wrong. Please try again later!");
            }
        }
    }

    private void updateCandidateStatus(Long jpId, Integer val){
        globalCandidateStatus = val;
        UpdateCandidateStatusRequest.Builder updateCandidateStatusRequestBuilder = UpdateCandidateStatusRequest.newBuilder();
        updateCandidateStatusRequestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
        updateCandidateStatusRequestBuilder.setCandidateStatus(val);
        updateCandidateStatusRequestBuilder.setJpId(jpId);
        updateCandidateStatusRequestBuilder.setNotGoingReason(0);
        globalJpId = jpId;

        if (mCandidateStatusAsyncTask != null) {
            mCandidateStatusAsyncTask.cancel(true);
        }

        mCandidateStatusAsyncTask = new UpdateCandidateStatusAsyncTask();
        mCandidateStatusAsyncTask.execute(updateCandidateStatusRequestBuilder.build());
    }

    public static void showDetail(Context context, JobPostWorkFlowObject jobApplicationObject) {
        Intent intent = new Intent(context, JobApplicationDetailActivity.class);
        JPWFObject = jobApplicationObject;
        context.startActivity(intent);
    }

    public class UpdateCandidateStatusAsyncTask extends AsyncTask<UpdateCandidateStatusRequest,
            Void, UpdateCandidateStatusResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(JobApplicationDetailActivity.this);
            pd.show();
        }

        @Override
        protected UpdateCandidateStatusResponse doInBackground(UpdateCandidateStatusRequest... params) {
            return HttpRequest.updateCandidateStatus(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateCandidateStatusResponse updateCandidateStatusResponse) {
            super.onPostExecute(updateCandidateStatusResponse);
            mAsyncTask = null;
            pd.cancel();
            if(updateCandidateStatusResponse.getStatus().getNumber() == UpdateCandidateStatusResponse.Status.SUCCESS_VALUE){

                if(globalCandidateStatus == ServerConstants.CANDIDATE_STATUS_NOT_GOING_VAL){
                    NotGoingReasonRequest.Builder notGoingReasonRequestBuilder = NotGoingReasonRequest.newBuilder();
                    notGoingReasonRequestBuilder.setTypeId(ServerConstants.INTERVIEW_NOT_GOING_TYPE_REASON);

                    if (mNotGoingAsyncTask != null) {
                        mNotGoingAsyncTask.cancel(true);
                    }
                    mNotGoingAsyncTask = new JobApplicationDetailActivity.NotGoingReasonResponse();
                    mNotGoingAsyncTask.execute(notGoingReasonRequestBuilder.build());

                } else if(globalCandidateStatus == ServerConstants.CANDIDATE_STATUS_DELAYED_VAL ||
                        globalCandidateStatus == ServerConstants.CANDIDATE_STATUS_STARTED_VAL){

                    // for delayed and started status
                    NotGoingReasonRequest.Builder notGoingReasonRequestBuilder = NotGoingReasonRequest.newBuilder();
                    notGoingReasonRequestBuilder.setTypeId(ServerConstants.CANDIDATE_ETA);

                    if (mNotGoingAsyncTask != null) {
                        mAsyncTask.cancel(true);
                    }
                    mNotGoingAsyncTask = new JobApplicationDetailActivity.NotGoingReasonResponse();
                    mNotGoingAsyncTask.execute(notGoingReasonRequestBuilder.build());

                }  else{
                    showToast("Status Updated!");
                    finish();
                    Intent intent = new Intent(JobApplicationDetailActivity.this, JobApplicationActivity.class);
                    startActivity(intent);
                }
            } else{
                showToast("Something went wrong. Please try again later!");
            }
        }
    }

    private class NotGoingReasonResponse extends AsyncTask<NotGoingReasonRequest,Void,in.trujobs.proto.NotGoingReasonResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(JobApplicationDetailActivity.this);
            pd.show();
        }

        @Override
        protected in.trujobs.proto.NotGoingReasonResponse doInBackground(NotGoingReasonRequest... params) {
            return HttpRequest.getAllNotGoingReason(params[0]);
        }


        @Override
        protected void onPostExecute(in.trujobs.proto.NotGoingReasonResponse notGoingReasonResponse) {
            super.onPostExecute(notGoingReasonResponse);
            pd.cancel();

            //initializing the list of reason
            final CharSequence[] reasonList = new CharSequence[notGoingReasonResponse.getReasonObjectCount()];
            final Long[] reasonIdList = new Long[notGoingReasonResponse.getReasonObjectCount()];
            for (int i = 0; i < notGoingReasonResponse.getReasonObjectCount(); i++) {
                reasonList[i] = notGoingReasonResponse.getReasonObject(i).getReasonTitle();
                reasonIdList[i] = notGoingReasonResponse.getReasonObject(i).getReasonId();
            }

            final android.support.v7.app.AlertDialog.Builder applyDialogBuilder = new android.support.v7.app.AlertDialog.Builder(JobApplicationDetailActivity.this);
            applyDialogBuilder.setCancelable(true);
            if(globalCandidateStatus == ServerConstants.CANDIDATE_STATUS_NOT_GOING_VAL){
                applyDialogBuilder.setTitle("Select reason for not going:");
            } else{
                applyDialogBuilder.setTitle("Reaching for interview in:");
            }

            applyDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(selectedNotGoingReasonIndex > 0){
                        UpdateCandidateStatusRequest.Builder updateCandidateStatusRequestBuilder = UpdateCandidateStatusRequest.newBuilder();
                        updateCandidateStatusRequestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                        updateCandidateStatusRequestBuilder.setCandidateStatus(globalCandidateStatus);
                        updateCandidateStatusRequestBuilder.setJpId(globalJpId);
                        updateCandidateStatusRequestBuilder.setNotGoingReason(reasonIdList[selectedNotGoingReasonIndex]);
                        globalCandidateStatus = 0;

                        if (mAsyncTask != null) {
                            mAsyncTask.cancel(true);
                        }

                        mCandidateStatusAsyncTask = new UpdateCandidateStatusAsyncTask();
                        mCandidateStatusAsyncTask.execute(updateCandidateStatusRequestBuilder.build());

                    } else{
                        showToast("Please select a reason for not going!");
                    }
                }
            });
            applyDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            applyDialogBuilder.setSingleChoiceItems(reasonList, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedNotGoingReasonIndex = which;
                }
            });
            final android.support.v7.app.AlertDialog applyDialog = applyDialogBuilder.create();
            applyDialog.show();
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