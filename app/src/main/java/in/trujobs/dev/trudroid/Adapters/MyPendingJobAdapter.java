package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
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
import in.trujobs.proto.UpdateInterviewRequest;
import in.trujobs.proto.UpdateInterviewResponse;

import static in.trujobs.dev.trudroid.api.ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_RECRUITER_SUPPORT;

/**
 * Created by dodo on 25/11/16.
 */

public class MyPendingJobAdapter extends ArrayAdapter<JobPostWorkFlowObject> {

    private Activity ctx;
    private ProgressDialog pd;

    private int rescheduledStartIndex;
    private int underReviewStartIndex;
    private int rejectedStartIndex;

    private AsyncTask<UpdateInterviewRequest, Void, UpdateInterviewResponse> mAsyncTask;

    public MyPendingJobAdapter(Activity context, List<JobPostWorkFlowObject> jobApplicationObjectList, int rescheduledStartIndex,
                               int underReviewStartIndex, int rejectedStartIndex) {
        super(context, 0, jobApplicationObjectList);
        ctx = context;
        this.rescheduledStartIndex = rescheduledStartIndex;
        this.underReviewStartIndex = underReviewStartIndex;
        this.rejectedStartIndex = rejectedStartIndex;
    }

    public class Holder
    {
        TextView mJobApplicationTitleTextView, mJobApplicationCompanyTextView, mJobApplicationSalaryTextView,
                mJobApplicationExperienceTextView, mLastUpdateTextView, mJobApplicationInterviewSchedule, mInterviewDate;
        LinearLayout rescheduledHeader, underReviewHeader, rejectedHeader, interviewDateView;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        MyPendingJobAdapter.Holder holder = new MyPendingJobAdapter.Holder();
        final JobPostWorkFlowObject jobApplicationObject = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.my_job_under_review_listview, parent, false);
        }

        pd = CustomProgressDialog.get(parent.getContext());
        ImageView applicationStatusIcon = (ImageView) rowView.findViewById(R.id.application_status_icon);
        TextView applicationStatusText = (TextView) rowView.findViewById(R.id.application_status);

        android.support.v7.widget.CardView cardView = (android.support.v7.widget.CardView) rowView.findViewById(R.id.card_view);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JobApplicationDetailActivity.showDetail(getContext(), jobApplicationObject);
            }
        });

        LinearLayout reschedulePanel = (LinearLayout) rowView.findViewById(R.id.reschedule_panel);
        holder.rescheduledHeader = (LinearLayout) rowView.findViewById(R.id.rescheduled_header);
        holder.underReviewHeader = (LinearLayout) rowView.findViewById(R.id.under_review_header);
        holder.rejectedHeader = (LinearLayout) rowView.findViewById(R.id.rejected_header);

        holder.rescheduledHeader.setVisibility(View.GONE);
        holder.underReviewHeader.setVisibility(View.GONE);
        holder.rejectedHeader.setVisibility(View.GONE);

        holder.interviewDateView = (LinearLayout) rowView.findViewById(R.id.scheduled_interview_date);
        holder.interviewDateView.setVisibility(View.GONE);

        if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_RESCHEDULE){
            applicationStatusIcon.setBackgroundResource(R.drawable.ic_delayed);
            applicationStatusText.setText("Rescheduled");
            applicationStatusText.setTextColor(getContext().getResources().getColor(R.color.colorLightOrange));

            LinearLayout acceptImageView = (LinearLayout) rowView.findViewById(R.id.accept_interview);
            LinearLayout rejectImageView = (LinearLayout) rowView.findViewById(R.id.reject_interview);

            if(position == rescheduledStartIndex && rescheduledStartIndex != -1){
                holder.rescheduledHeader.setVisibility(View.VISIBLE);
            }
            reschedulePanel.setVisibility(View.VISIBLE);

            acceptImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateRescheduledInterviewConfirmation(ServerConstants.CANDIDATE_STATUS_RESCHEDULED_INTERVIEW_ACCEPT, jobApplicationObject.getJobPostObject().getJobPostId());
                }
            });

            rejectImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateRescheduledInterviewConfirmation(ServerConstants.CANDIDATE_STATUS_RESCHEDULED_INTERVIEW_REJECT, jobApplicationObject.getJobPostObject().getJobPostId());
                }
            });

        } else if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_CANDIDATE ||
                jobApplicationObject.getCandidateInterviewStatus().getStatusId() == JWF_STATUS_INTERVIEW_REJECTED_BY_RECRUITER_SUPPORT){

            if(position == rejectedStartIndex && rejectedStartIndex != -1){
                holder.rejectedHeader.setVisibility(View.VISIBLE);
            }

            applicationStatusIcon.setBackgroundResource(R.drawable.ic_error);
            applicationStatusText.setText("Not Shortlisted");
            applicationStatusText.setTextColor(getContext().getResources().getColor(R.color.colorRed));

        } else {
            if(position == underReviewStartIndex && underReviewStartIndex != -1){
                holder.underReviewHeader.setVisibility(View.VISIBLE);
            }

            if(jobApplicationObject.getInterviewDateMillis() != 0){
                holder.interviewDateView.setVisibility(View.VISIBLE);
            }
            applicationStatusIcon.setBackgroundResource(R.drawable.ic_delayed);
            applicationStatusText.setText("Under Review");
            applicationStatusText.setTextColor(getContext().getResources().getColor(R.color.colorLightOrange));

        }

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


        //last update
        holder.mLastUpdateTextView = (TextView) rowView.findViewById(R.id.last_update_date);
        Calendar lastUpdateCalendar = Calendar.getInstance();
        lastUpdateCalendar.setTimeInMillis(jobApplicationObject.getCreationTimeMillis());
        int mYear = lastUpdateCalendar.get(Calendar.YEAR);
        int mMonth = lastUpdateCalendar.get(Calendar.MONTH) + 1;
        int mDay = lastUpdateCalendar.get(Calendar.DAY_OF_MONTH);

        String cDay = mDay + "";
        String cMonth = (mMonth) + "";

        if(mDay < 10){
            cDay = "0" + mDay;
        }
        if(mMonth < 10){
            cMonth = "0" + mMonth;
        }

        holder.mLastUpdateTextView.setText(cDay + "-" + cMonth + "-" + mYear);

        holder.mJobApplicationInterviewSchedule = (TextView) rowView.findViewById(R.id.interview_schedule_text_view);
        holder.mInterviewDate = (TextView) rowView.findViewById(R.id.interview_date);

        Calendar calendar = Calendar.getInstance();
        if(jobApplicationObject.getInterviewDateMillis() != 0){
            calendar.setTimeInMillis(jobApplicationObject.getInterviewDateMillis());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH) + 1;
            mDay = calendar.get(Calendar.DAY_OF_MONTH);

            cDay = mDay + "";
            cMonth = (mMonth) + "";

            if(mDay < 10){
                cDay = "0" + mDay;
            }
            if(mMonth < 10){
                cMonth = "0" + mMonth;
            }

            holder.mJobApplicationInterviewSchedule.setText("Interview: " + cDay + "-" + cMonth + "-" + mYear + " @ " + jobApplicationObject.getInterviewTimeSlotObject().getSlotTitle());
            holder.mInterviewDate.setText(cDay + "-" + cMonth + "-" + mYear + " @ " + jobApplicationObject.getInterviewTimeSlotObject().getSlotTitle());
        } else{
            holder.mJobApplicationInterviewSchedule.setVisibility(View.GONE);
        }

        return rowView;
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
            pd = CustomProgressDialog.get(getContext());
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
                Toast.makeText(getContext(), "Updated!", Toast.LENGTH_LONG).show();
                ctx.finish();
                Intent intent = new Intent(ctx, JobApplicationActivity.class);
                ctx.startActivity(intent);
            } else{
                Toast.makeText(getContext(), "Something went wrong. Please try again later!", Toast.LENGTH_LONG).show();
            }
        }
    }

}


