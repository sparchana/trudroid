package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.CustomDialog.ViewDialog;
import in.trujobs.dev.trudroid.Helper.ApplyJobResponseBundle;
import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.dev.trudroid.interview.InterviewSlotSelectActivity;
import in.trujobs.dev.trudroid.prescreen.PreScreenActivity;
import in.trujobs.proto.ApplyJobRequest;
import in.trujobs.proto.ApplyJobResponse;
import in.trujobs.proto.JobPostObject;

/**
 * Created by batcoder1 on 27/7/16.
 */
public class JobPostAdapter extends ArrayAdapter<JobPostObject> {

    private ApplyJobResponseBundle applyJobResponseBundle;

    private AsyncTask<ApplyJobRequest, Void, ApplyJobResponse> mAsyncTask;

    int preScreenLocationIndex = 0;
    int otherJobsSectionIndex = -1;
    private Long myJobPostId;

    public JobPostAdapter(Activity context, List<JobPostObject> jobPostList, int externalJobSectionIndex) {
        super(context, 0, jobPostList);
        otherJobsSectionIndex = externalJobSectionIndex;
    }

    public class Holder
    {
        TextView mJobPostTitleTextView, mJobPostCompanyTextView, mJobPostSalaryTextView, mJobPostExperienceTextView, mJobPostVacancyTextView, mJobPostLocationTextView, mJobPostPostedOnTextView;
        Button mApplyBtnBackground;
        ImageView mJobColor;
        LinearLayout otherJobsHeader;
    }

    public Button applyingJobButton;
    public Button applyingJobButtonDetail;
    public ImageView applyingJobColor;
    ProgressDialog pd;

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        final Holder holder = new Holder();
        final JobPostObject jobPost = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.job_list_view_item, parent, false);
        }

        holder.mJobColor = (ImageView) rowView.findViewById(R.id.job_color);
        holder.mApplyBtnBackground = (Button) rowView.findViewById(R.id.apply_button_layout);
        holder.otherJobsHeader = (LinearLayout) rowView.findViewById(R.id.other_jobs_result_start);

        pd = CustomProgressDialog.get(parent.getContext());

        // presetting job card element as not applied
        holder.mJobColor.setImageResource(R.drawable.green_dot);
        holder.mApplyBtnBackground.setEnabled(true);
        holder.mApplyBtnBackground.setText("Apply");
        holder.mApplyBtnBackground.setBackgroundResource(R.drawable.rounded_corner_button);

        // by default we dont need to enable the header 'other jobs'
        holder.otherJobsHeader.setVisibility(View.GONE);
        // enable other jobs header only if this list view element corresponds to the the first
        // element in other jobs list
        if (position == otherJobsSectionIndex && otherJobsSectionIndex != -1) {
            holder.otherJobsHeader.setVisibility(View.VISIBLE);
        }

        if(jobPost.getIsApplied() == 1) {
            holder.mJobColor.setImageResource(R.drawable.orange_dot);
            holder.mApplyBtnBackground.setEnabled(false);
            holder.mApplyBtnBackground.setText("Applied");
            holder.mApplyBtnBackground.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
        }

        //when user is not logged in, show all jobs as not applied
        if(!Util.isLoggedIn()){
            holder.mJobColor.setImageResource(R.drawable.green_dot);
            holder.mApplyBtnBackground.setEnabled(true);
            holder.mApplyBtnBackground.setText("Apply");
            holder.mApplyBtnBackground.setBackgroundResource(R.drawable.rounded_corner_button);
        }

        holder.mJobColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(jobPost.getIsApplied() == 1){
                    Toast.makeText(getContext(), "You have already applied to this job",
                            Toast.LENGTH_LONG).show();
                } else{
                    Toast.makeText(getContext(), "You have not applied to this job",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

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

        if (jobPost.getVacancies() != 0) {
            holder.mJobPostVacancyTextView.setText(jobPost.getVacancies() + " vacancies");
        }
        else {
            holder.mJobPostVacancyTextView.setText(" Vacancies not specified");
        }

        //set Posted on
        holder.mJobPostPostedOnTextView = (TextView) rowView.findViewById(R.id.job_post_date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(jobPost.getJobPostCreationMillis());
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

        holder.mJobPostPostedOnTextView.setText("Posted on: " + cDay + "-" + cMonth + "-" + mYear);

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
        String more = "<font color='#2196f3'> + more</font>";

        if(jobPost.getJobPostLocalityCount() > 3){
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
                    alert.showDialog(getContext(), jobPost.getJobPostCompanyName() + "'s " + jobPost.getJobPostTitle() + " job locations:", allLocalities , "", R.drawable.location_round, -1);


                }
            });
        }
        if(jobPost.getJobPostLocalityCount() > 3){
            holder.mJobPostLocationTextView.setText(Html.fromHtml(localities + more));
        } else{
            holder.mJobPostLocationTextView.setText(localities);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.jobPostId.put(jobPost.getJobPostId());
                JobDetailActivity.start(getContext(), jobPost.getJobRole(), jobPost.getJobPostLocalityList());

            }
        });

        holder.mApplyBtnBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyingJobButton = holder.mApplyBtnBackground;
                applyingJobColor = holder.mJobColor;
                showJobLocality(jobPost);
            }
        });

        return rowView;
    }

    public void showJobLocality(final JobPostObject jobPost){
        if(Util.isLoggedIn()){
            preScreenLocationIndex = 0;
            final CharSequence[] localityList = new CharSequence[jobPost.getJobPostLocalityCount()];
            final Long[] localityId = new Long[jobPost.getJobPostLocalityCount()];
            for (int i = 0; i < jobPost.getJobPostLocalityCount(); i++) {
                localityList[i] = jobPost.getJobPostLocality(i).getLocalityName();
                localityId[i] = jobPost.getJobPostLocality(i).getLocalityId();
            }

            LinearLayout customTitleLayout = new LinearLayout(getContext());
            customTitleLayout.setPadding(30,30,30,30);
            TextView customTitle = new TextView(getContext());
            String title = "You are applying for <b>" + jobPost.getJobPostTitle() + "</b>  job at <b>" + jobPost.getJobPostCompanyName()
                    + "</b>. Please select a job Location";
            customTitle.setText(Html.fromHtml(title));
            customTitle.setTextSize(16);
            customTitleLayout.addView(customTitle);

            final android.support.v7.app.AlertDialog.Builder applyDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
            applyDialogBuilder.setCancelable(true);
            applyDialogBuilder.setCustomTitle(customTitleLayout);
            applyDialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    applyJob(jobPost.getJobPostId(), localityId[preScreenLocationIndex], null);
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
        } else {
            Toast.makeText(getContext(), "Please login/sign up to apply.",
                    Toast.LENGTH_LONG).show();
            Prefs.jobToApplyStatus.put(1);
            Prefs.getJobToApplyJobId.put(jobPost.getJobPostId());
            ((Activity)getContext()).finish();
        }
    }

    public ApplyJobResponseBundle applyJob(Long jobPostId, Long localityId, Button detailPageApplyBtn){
        if(detailPageApplyBtn != null){
            applyingJobButtonDetail = detailPageApplyBtn;
        }
        myJobPostId = jobPostId;
        ApplyJobRequest.Builder requestBuilder = ApplyJobRequest.newBuilder();
        requestBuilder.setJobPostId(jobPostId);
        requestBuilder.setLocalityId(localityId);
        requestBuilder.setCandidateMobile(String.valueOf(Prefs.candidateMobile.get()));

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        if(applyJobResponseBundle == null) {
            applyJobResponseBundle = new ApplyJobResponseBundle();
        }
        applyJobResponseBundle.setApplyingJobColor(applyingJobColor);
        applyJobResponseBundle.setApplyingJobButton(applyingJobButton);
        applyJobResponseBundle.setApplyingJobButtonDetail(applyingJobButtonDetail);

        mAsyncTask = new ApplyJobAsyncTask();
        mAsyncTask.execute(requestBuilder.build());

        return applyJobResponseBundle;
    }

    private class ApplyJobAsyncTask extends AsyncTask<ApplyJobRequest,
            Void, ApplyJobResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(getContext());
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
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (applyJobResponse == null) {
                Toast.makeText(getContext(), "Something went wrong. Please try again later.",
                        Toast.LENGTH_LONG).show();
                Tlog.w("Null Response");
                return;
            }
            applyJobResponseBundle.setApplyJobResponse(applyJobResponse);
            if(applyJobResponse.getIsPreScreenAvailable()){
                Tlog.i("pre screen is available");
                PreScreenActivity.start(getContext(), applyJobResponse.getJobPostId(), applyJobResponseBundle);
            } else if(applyJobResponse.getIsInterviewAvailable()) {
                Tlog.i("interview is available");
                InterviewSlotSelectActivity.start(getContext(),
                        applyJobResponse.getJobPostId(),
                        applyJobResponse.getCompanyName(),
                        applyJobResponse.getJobRoleTitle(),
                        applyJobResponse.getJobTitle());
            } else {

                ViewDialog alert = new ViewDialog();
                if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_SUCCESS){
                    alert.showDialog(getContext(), "Application Sent", "Your Application has been sent to the recruiter", "You can track your application in \"My Jobs\" option in the Menu", R.drawable.sent, 5);
                    //setting "already applied" to apply button of the jobs list
                    try {
                        applyingJobColor.setImageResource(R.drawable.orange_dot);
                        applyingJobButton.setEnabled(false);
                        applyingJobButton.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
                        applyingJobButton.setText("Applied");
                    } catch (Exception ignored){}

                    //setting "already applied" to job detail activity button
                    try{
                        applyingJobButtonDetail.setText("Applied");
                        applyingJobButtonDetail.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
                        applyingJobButtonDetail.setEnabled(false);
                    } catch (Exception ignored){}
                } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_ALREADY_APPLIED){
                    alert.showDialog(getContext(), "Already Applied", "Looks like you have already applied to this job", "", R.drawable.sent, 5);
                    try {
                        applyingJobButton.setEnabled(false);
                        applyingJobButton.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
                        applyingJobButton.setText("Applied");
                    } catch (Exception ignored){}
                } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_JOB){
                    alert.showDialog(getContext(), "No Job Found", "Looks like the job is no more active", "", R.drawable.sent, 0);
                } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_CANDIDATE){
                    alert.showDialog(getContext(), "Candidate doesn't exists", "Please login to continue", "", R.drawable.sent, 0);
                } else if(!Util.isConnectedToInternet(getContext())) {
                    Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                } else{
                    alert.showDialog(getContext(), "Something went wrong! Please try again", "Unable to contact our servers", "",  R.drawable.sent, 0);
                }
            }
        }
    }


}
