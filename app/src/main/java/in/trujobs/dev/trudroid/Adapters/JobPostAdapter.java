package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.CustomDialog.ViewDialog;
import in.trujobs.dev.trudroid.WelcomeScreen;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.ApplyJobRequest;
import in.trujobs.proto.ApplyJobResponse;
import in.trujobs.proto.CandidateAppliedJobsResponse;
import in.trujobs.proto.JobApplicationObject;
import in.trujobs.proto.JobPostObject;

/**
 * Created by batcoder1 on 27/7/16.
 */
public class JobPostAdapter extends ArrayAdapter<JobPostObject> {

    private AsyncTask<ApplyJobRequest, Void, ApplyJobResponse> mAsyncTask;
    int preScreenLocationIndex = 0;

    public JobPostAdapter(Activity context, List<JobPostObject> jobPostList) {
        super(context, 0, jobPostList);
    }
    public class Holder
    {
        TextView mJobPostTitleTextView, mJobPostCompanyTextView, mJobPostSalaryTextView, mJobPostExperienceTextView, mJobPostVacancyTextView, mJobPostLocationTextView, mJobPostPostedOnTextView, mJobPostApplyBtn;
        LinearLayout mApplyBtnBackground;
    }
    ProgressDialog pd;

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        Holder holder = new Holder();
        final JobPostObject jobPost = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.job_list_view_item, parent, false);
        }
        holder.mJobPostApplyBtn = (TextView) rowView.findViewById(R.id.apply_button);
        holder.mApplyBtnBackground = (LinearLayout) rowView.findViewById(R.id.apply_button_layout);

        LinearLayout applyBtn = (LinearLayout) rowView.findViewById(R.id.apply_btn);

        applyBtn.setEnabled(true);
        holder.mJobPostApplyBtn.setText("Apply");
        holder.mApplyBtnBackground.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));

        if(jobPost.getIsApplied() == 1){
            applyBtn.setEnabled(false);
            holder.mJobPostApplyBtn.setText("Already Applied");
            holder.mApplyBtnBackground.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
        }

        //set job post title
        holder.mJobPostTitleTextView = (TextView) rowView.findViewById(R.id.job_post_title_text_view);
        holder.mJobPostTitleTextView.setText(jobPost.getJobPostTitle());

        //set job post Company
        holder.mJobPostCompanyTextView = (TextView) rowView.findViewById(R.id.job_post_company_text_view);
        holder.mJobPostCompanyTextView.setText(jobPost.getJobPostCompanyName());

        //set job post salary
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.mJobPostSalaryTextView = (TextView) rowView.findViewById(R.id.job_post_salary_text_view);
        if(jobPost.getJobPostMaxSalary() != 0){
            holder.mJobPostSalaryTextView.setText("₹" + formatter.format(jobPost.getJobPostMinSalary()) + " - ₹" + formatter.format(jobPost.getJobPostMaxSalary()));
        } else {
            holder.mJobPostSalaryTextView.setText("₹" + formatter.format(jobPost.getJobPostMinSalary()));
        }

        //set job post experience
        holder.mJobPostExperienceTextView = (TextView) rowView.findViewById(R.id.job_post_exp_text_view);
        holder.mJobPostExperienceTextView.setText(jobPost.getJobPostExperience().getExperienceType());

        //set job post vacancy
        holder.mJobPostVacancyTextView = (TextView) rowView.findViewById(R.id.job_post_vacancy_text_view);
        holder.mJobPostVacancyTextView.setText(jobPost.getVacancies() + " vacancies");

        //set Posted on
        holder.mJobPostPostedOnTextView = (TextView) rowView.findViewById(R.id.job_post_date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(jobPost.getJobPostCreationMillis());
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        holder.mJobPostPostedOnTextView.setText("Posted on: " + mDay + "-" + mMonth + "-" + mYear);

        //set job post localities
        holder.mJobPostLocationTextView = (TextView) rowView.findViewById(R.id.job_post_location_text_view);
        String localities = "";
        int localityCount = jobPost.getJobPostLocalityCount();
        if(localityCount > 3){
            localityCount = 3;
        }
        for (int i = 0; i < localityCount; i++) {
            localities += jobPost.getJobPostLocality(i).getLocalityName();
            if(i != (localityCount - 1)){
                localities += ", ";
            }
        }
        if(jobPost.getJobPostLocalityCount() > 3){
            localities += " more";
            holder.mJobPostLocationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String allLocalities = "";
                    for (int i = 0; i < jobPost.getJobPostLocalityCount(); i++) {
                        allLocalities += jobPost.getJobPostLocality(i).getLocalityName();
                        if(i != (jobPost.getJobPostLocalityCount() - 1)){
                            allLocalities += ", ";
                        }
                    }
                    ViewDialog alert = new ViewDialog();
                    alert.showDialog(getContext(), jobPost.getJobPostCompanyName() + "'s " + jobPost.getJobPostTitle() + " job locations:", allLocalities , "", R.drawable.location_round, 2);
                }
            });
        }
        holder.mJobPostLocationTextView.setText(localities);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.jobPostId.put(jobPost.getJobPostId());
                JobDetailActivity.start(getContext(), jobPost.getJobRole(), jobPost.getJobPostLocalityList());
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJobLocality(jobPost);
            }
        });
        return rowView;
    }

    public void showJobLocality(final JobPostObject jobPost){
        if(Util.isLoggedIn() == true){
            preScreenLocationIndex = 0;
            final CharSequence[] localityList = new CharSequence[jobPost.getJobPostLocalityCount()];
            final Long[] localityId = new Long[jobPost.getJobPostLocalityCount()];
            for (int i = 0; i < jobPost.getJobPostLocalityCount(); i++) {
                localityList[i] = jobPost.getJobPostLocality(i).getLocalityName();
                localityId[i] = jobPost.getJobPostLocality(i).getLocalityId();
            }

            final AlertDialog alertDialog = new AlertDialog.Builder(
                    getContext())
                    .setCancelable(true)
                    .setTitle("You are applying for " + jobPost.getJobPostTitle() + " job at " + jobPost.getJobPostCompanyName() + ". Please select a job Location" )
                    .setPositiveButton("Apply",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    applyJob(jobPost.getJobPostId(), localityId[preScreenLocationIndex]);
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
            Intent intent = new Intent(getContext(), WelcomeScreen.class);
            getContext().startActivity(intent);
            Prefs.loginCheckStatus.put(0);
        }
    }

    public void applyJob(Long jobPostId, Long localityId){
        ApplyJobRequest.Builder requestBuilder = ApplyJobRequest.newBuilder();
        requestBuilder.setJobPostId(jobPostId);
        requestBuilder.setLocalityId(localityId);
        requestBuilder.setCandidateMobile(String.valueOf(Prefs.candidateMobile.get()));

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        mAsyncTask = new ApplyJobAsyncTask();
        mAsyncTask.execute(requestBuilder.build());
    }

    private class ApplyJobAsyncTask extends AsyncTask<ApplyJobRequest,
            Void, ApplyJobResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getContext(),R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected ApplyJobResponse doInBackground(ApplyJobRequest... params) {
            return HttpRequest.applyJob(params[0]);
        }

        @Override
        protected void onPostExecute(ApplyJobResponse applyJobResponse) {
            super.onPostExecute(applyJobResponse);
            mAsyncTask = null;
            pd.cancel();
            if (applyJobResponse == null) {
                Toast.makeText(getContext(), "Something went wrong. Please try again later.",
                        Toast.LENGTH_LONG).show();
                Tlog.w("Null Response");
                return;
            }
            ViewDialog alert = new ViewDialog();
            if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_SUCCESS){
                alert.showDialog(getContext(), "Application Sent", "Your Application has been sent to the recruiter", "You can track your application in \"My Jobs\" option in the Menu", R.drawable.sent, 2);
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_ALREADY_APPLIED){
                alert.showDialog(getContext(), "Already Applied", "Looks like you have already applied to this job", "", R.drawable.sent, 2);
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_JOB){
                alert.showDialog(getContext(), "No Job Found", "Looks like the job is no more active", "", R.drawable.sent, 2);
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_CANDIDATE){
                alert.showDialog(getContext(), "Candidate doesn't exists", "Please login to continue", "", R.drawable.sent, 2);
            } else{
                alert.showDialog(getContext(), "Something went wrong! Please try again", "Unable to contact out servers", "",  R.drawable.sent, 2);
            }
        }
    }
}
