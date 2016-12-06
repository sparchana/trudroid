package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.JobApplicationActivity;
import in.trujobs.dev.trudroid.JobApplicationDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.JobPostWorkFlowObject;
import in.trujobs.proto.UpdateCandidateStatusRequest;
import in.trujobs.proto.UpdateCandidateStatusResponse;
import in.trujobs.proto.UpdateInterviewRequest;
import in.trujobs.proto.UpdateInterviewResponse;

/**
 * Created by batcoder1 on 8/8/16.
 */
public class MyConfirmedJobsAdapter extends ArrayAdapter<JobPostWorkFlowObject> {

    private int todayInterviewStartIndex;
    private int upcomingInterviewStartIndex;
    private int pastInterviewStartIndex;

    private Activity ctx;
    public MyConfirmedJobsAdapter(Activity context, List<JobPostWorkFlowObject> jobApplicationObjectList,
                                  int todayInterviewStartIndex, int upcomingInterviewStartIndex, int pastInterviewStartIndex) {
        super(context, 0, jobApplicationObjectList);
        ctx = context;
        this.todayInterviewStartIndex = todayInterviewStartIndex;
        this.upcomingInterviewStartIndex = upcomingInterviewStartIndex;
        this.pastInterviewStartIndex = pastInterviewStartIndex;
    }

    public class Holder
    {
        TextView mJobApplicationTitleTextView, mJobApplicationCompanyTextView, mJobApplicationSalaryTextView, mJobApplicationExperienceTextView, mJobApplicationInterviewSchedule, mCurrentStatus;
        LinearLayout todayInterviewHeader, upcomingInterviewHeader, pastInterviewHeader;
    }

    private ProgressDialog pd;
    private Integer globalCandidateStatus = 0;
    private Integer selectedNotGoingReasonIndex = 0;
    private Long globalJpId = 0L;
    private AsyncTask<UpdateCandidateStatusRequest, Void, UpdateCandidateStatusResponse> mCandidateStatusAsyncTask;

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        Holder holder = new Holder();
        final JobPostWorkFlowObject jobApplicationObject = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.my_jobs_listview, parent, false);
        }

        pd = CustomProgressDialog.get(parent.getContext());

        LinearLayout candidateStatusPanel = (LinearLayout) rowView.findViewById(R.id.candidate_status_panel);
        TextView selectStatusLabel = (TextView) rowView.findViewById(R.id.select_status_label);
        selectStatusLabel.setVisibility(View.GONE);

        holder.todayInterviewHeader = (LinearLayout) rowView.findViewById(R.id.todays_interview_header);
        holder.upcomingInterviewHeader = (LinearLayout) rowView.findViewById(R.id.upcoming_interview_header);
        holder.pastInterviewHeader = (LinearLayout) rowView.findViewById(R.id.past_interview_header);

        holder.todayInterviewHeader.setVisibility(View.GONE);
        holder.upcomingInterviewHeader.setVisibility(View.GONE);
        holder.pastInterviewHeader.setVisibility(View.GONE);

        if(position == todayInterviewStartIndex && todayInterviewStartIndex != -1){
            holder.todayInterviewHeader.setVisibility(View.VISIBLE);
        }

        if(position == upcomingInterviewStartIndex && upcomingInterviewStartIndex != -1){
            holder.upcomingInterviewHeader.setVisibility(View.VISIBLE);
        }

        if(position == pastInterviewStartIndex && pastInterviewStartIndex != -1){
            holder.pastInterviewHeader.setVisibility(View.VISIBLE);
        }

        android.support.v7.widget.CardView cardView = (android.support.v7.widget.CardView) rowView.findViewById(R.id.card_view);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JobApplicationDetailActivity.showDetail(getContext(), jobApplicationObject);
            }
        });

        ImageView navigateIcon = (ImageView) rowView.findViewById(R.id.navigate_icon);
        TextView navigateText = (TextView) rowView.findViewById(R.id.navigate_text);

        LinearLayout statusOptionLayout = (LinearLayout) rowView.findViewById(R.id.status_options);
        LinearLayout notGoingLayout = (LinearLayout) rowView.findViewById(R.id.not_going);
        LinearLayout delayedLayout = (LinearLayout) rowView.findViewById(R.id.delayed);
        LinearLayout startedLayout = (LinearLayout) rowView.findViewById(R.id.started);
        LinearLayout reachedLayout = (LinearLayout) rowView.findViewById(R.id.reached);

        if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() >= ServerConstants.JWF_STATUS_INTERVIEW_RESCHEDULE && jobApplicationObject.getCandidateInterviewStatus().getStatusId() < ServerConstants.JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_SELECTED){ //confirmed and/or has current status

            if(jobApplicationObject.getInterviewLat() != 0.0){
                navigateIcon.setVisibility(View.VISIBLE);
                navigateText.setVisibility(View.VISIBLE);
                navigateIcon.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View v) {
                        double destinationLatitude = jobApplicationObject.getInterviewLat();
                        double destinationLongitude = jobApplicationObject.getInterviewLng();

                        String url = "http://maps.google.com/maps?f=d&daddr="+ destinationLatitude+","+destinationLongitude+"&dirflg=d&layer=t";
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        getContext().startActivity(intent);
                    }
                });
            } else{
                navigateIcon.setVisibility(View.GONE);
                navigateText.setVisibility(View.GONE);
            }

        } else{
            navigateIcon.setVisibility(View.GONE);
            navigateText.setVisibility(View.GONE);
        }

        Calendar now = Calendar.getInstance();

        Calendar interviewCalendar = Calendar.getInstance();

        interviewCalendar.setTimeInMillis(jobApplicationObject.getInterviewDateMillis());
        int interviewYear = interviewCalendar.get(Calendar.YEAR);
        int interviewMonth = interviewCalendar.get(Calendar.MONTH) + 1;
        int interviewDay = interviewCalendar.get(Calendar.DAY_OF_MONTH);

        if((interviewDay == now.get(Calendar.DATE)) && (interviewMonth) == (now.get(Calendar.MONTH) + 1) && interviewYear == now.get(Calendar.YEAR)){
            if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() > ServerConstants.JWF_STATUS_INTERVIEW_RESCHEDULE && jobApplicationObject.getCandidateInterviewStatus().getStatusId() < ServerConstants.JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_SELECTED){
                candidateStatusPanel.setVisibility(View.VISIBLE);
                selectStatusLabel.setVisibility(View.VISIBLE);
                holder.mCurrentStatus = (TextView) rowView.findViewById(R.id.current_status);
                holder.mCurrentStatus.setText(jobApplicationObject.getCandidateInterviewStatus().getStatusTitle());
                if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_NOT_GOING || jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_DELAYED){
                    holder.mCurrentStatus.setTextColor(Color.RED);
                } else{
                    holder.mCurrentStatus.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
                }

                if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_REACHED){ //reached
                    statusOptionLayout.setVisibility(View.GONE);
                } else{
                    statusOptionLayout.setVisibility(View.VISIBLE);

                    if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_CONFIRMED || jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_NOT_GOING){
                        if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_CONFIRMED){
                            notGoingLayout.setVisibility(View.VISIBLE);
                        } else{
                            notGoingLayout.setVisibility(View.GONE);
                        }
                        delayedLayout.setVisibility(View.VISIBLE);
                        startedLayout.setVisibility(View.VISIBLE);
                        reachedLayout.setVisibility(View.VISIBLE);
                    } else if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_DELAYED){
                        notGoingLayout.setVisibility(View.GONE);
                        delayedLayout.setVisibility(View.GONE);
                        startedLayout.setVisibility(View.VISIBLE);
                        reachedLayout.setVisibility(View.VISIBLE);

                    } else if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_STARTED) {
                        notGoingLayout.setVisibility(View.GONE);
                        startedLayout.setVisibility(View.GONE);
                        delayedLayout.setVisibility(View.VISIBLE);
                        reachedLayout.setVisibility(View.VISIBLE);
                    }
                }
            } else{
                candidateStatusPanel.setVisibility(View.GONE);
            }
        } else{
            candidateStatusPanel.setVisibility(View.GONE);
        }

        notGoingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCandidateStatus(jobApplicationObject.getJobPostObject().getJobPostId(), ServerConstants.CANDIDATE_STATUS_NOT_GOING_VAL);
            }
        });

        delayedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCandidateStatus(jobApplicationObject.getJobPostObject().getJobPostId(), ServerConstants.CANDIDATE_STATUS_DELAYED_VAL);
            }
        });

        startedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCandidateStatus(jobApplicationObject.getJobPostObject().getJobPostId(), ServerConstants.CANDIDATE_STATUS_STARTED_VAL);
            }
        });

        reachedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCandidateStatus(jobApplicationObject.getJobPostObject().getJobPostId(), ServerConstants.CANDIDATE_STATUS_REACHED_VAL);
            }
        });

        //set job Application title
        holder.mJobApplicationTitleTextView = (TextView) rowView.findViewById(R.id.job_post_title_text_view);
        holder.mJobApplicationTitleTextView.setText(jobApplicationObject.getJobPostObject().getJobPostTitle());

        //set job Application Company
        holder.mJobApplicationCompanyTextView = (TextView) rowView.findViewById(R.id.job_post_company_text_view);
        holder.mJobApplicationCompanyTextView.setText(jobApplicationObject.getJobPostObject().getJobPostCompanyName());

        //set job Application salary
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.mJobApplicationSalaryTextView = (TextView) rowView.findViewById(R.id.job_post_salary_text_view);
        if(jobApplicationObject.getJobPostObject().getJobPostMaxSalary() != 0){
            holder.mJobApplicationSalaryTextView.setText("₹" + formatter.format(jobApplicationObject.getJobPostObject().getJobPostMinSalary()) + " - ₹" + formatter.format(jobApplicationObject.getJobPostObject().getJobPostMaxSalary()));
        } else{
            holder.mJobApplicationSalaryTextView.setText("₹" + formatter.format(jobApplicationObject.getJobPostObject().getJobPostMinSalary()));
        }

        //set job Application experience
        holder.mJobApplicationExperienceTextView = (TextView) rowView.findViewById(R.id.job_post_exp_text_view);
        holder.mJobApplicationExperienceTextView.setText(jobApplicationObject.getJobPostObject().getJobPostExperience().getExperienceType());

        holder.mJobApplicationInterviewSchedule = (TextView) rowView.findViewById(R.id.interview_schedule_text_view);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(jobApplicationObject.getInterviewDateMillis());
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        String cDay = mDay + "";
        String cMonth = (mMonth) + "";

        if(mDay < 10){
            cDay = "0" + mDay;
        }
        if(mMonth < 10){
            cMonth = "0" + mMonth;
        }

        holder.mJobApplicationInterviewSchedule.setText("Interview: " + cDay + "-" + cMonth + "-" + mYear + " @ " + jobApplicationObject.getInterviewTimeSlotObject().getSlotTitle());
        return rowView;
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

    private class UpdateCandidateStatusAsyncTask extends AsyncTask<UpdateCandidateStatusRequest,
            Void, UpdateCandidateStatusResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(getContext());
            pd.show();
        }

        @Override
        protected UpdateCandidateStatusResponse doInBackground(UpdateCandidateStatusRequest... params) {
            return HttpRequest.updateCandidateStatus(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateCandidateStatusResponse updateCandidateStatusResponse) {
            super.onPostExecute(updateCandidateStatusResponse);
            mCandidateStatusAsyncTask = null;
            pd.cancel();
            if(updateCandidateStatusResponse.getStatus().getNumber() == UpdateCandidateStatusResponse.Status.SUCCESS_VALUE){

                //showing not going reason dialog
                if(globalCandidateStatus == ServerConstants.CANDIDATE_STATUS_NOT_GOING_VAL){
                    AsyncTask<Void, Void, in.trujobs.proto.NotGoingReasonResponse> reasonAsyncTask = new NotGoingReasonResponse();
                    reasonAsyncTask.execute();
                } else{
                    Toast.makeText(getContext(), "Status Updated!", Toast.LENGTH_LONG).show();
                    ctx.finish();
                    Intent intent = new Intent(ctx, JobApplicationActivity.class);
                    ctx.startActivity(intent);
                }
            } else{
                Toast.makeText(getContext(), "Something went wrong. Please try again later!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class NotGoingReasonResponse extends AsyncTask<Void,Void,in.trujobs.proto.NotGoingReasonResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(getContext());
            pd.show();
        }

        @Override
        protected in.trujobs.proto.NotGoingReasonResponse doInBackground(Void... params) {
            return HttpRequest.getAllNotGoingReason();
        }

        @Override
        protected void onPostExecute(in.trujobs.proto.NotGoingReasonResponse notGoingReasonResponse) {
            super.onPostExecute(notGoingReasonResponse);
            mCandidateStatusAsyncTask = null;
            pd.cancel();

            //initializing the list of reason
            final CharSequence[] reasonList = new CharSequence[notGoingReasonResponse.getReasonObjectCount()];
            final Long[] reasonIdList = new Long[notGoingReasonResponse.getReasonObjectCount()];
            for (int i = 0; i < notGoingReasonResponse.getReasonObjectCount(); i++) {
                reasonList[i] = notGoingReasonResponse.getReasonObject(i).getReasonTitle();
                reasonIdList[i] = notGoingReasonResponse.getReasonObject(i).getReasonId();
            }

            final android.support.v7.app.AlertDialog.Builder applyDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
            applyDialogBuilder.setCancelable(true);
            applyDialogBuilder.setTitle("Select reason for not going:");
            applyDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(selectedNotGoingReasonIndex > 0){
                        UpdateCandidateStatusRequest.Builder updateCandidateStatusRequestBuilder = UpdateCandidateStatusRequest.newBuilder();
                        updateCandidateStatusRequestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                        updateCandidateStatusRequestBuilder.setCandidateStatus(globalCandidateStatus);
                        updateCandidateStatusRequestBuilder.setJpId(globalJpId);
                        updateCandidateStatusRequestBuilder.setNotGoingReason(reasonIdList[selectedNotGoingReasonIndex]);
                        globalCandidateStatus = 0;

                        if (mCandidateStatusAsyncTask != null) {
                            mCandidateStatusAsyncTask.cancel(true);
                        }

                        mCandidateStatusAsyncTask = new UpdateCandidateStatusAsyncTask();
                        mCandidateStatusAsyncTask.execute(updateCandidateStatusRequestBuilder.build());

                    } else{
                        Toast.makeText(getContext(), "Please select a reason for not going!", Toast.LENGTH_LONG).show();
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
}