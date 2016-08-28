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

import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.CustomDialog.ViewDialog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.ApplyJobRequest;
import in.trujobs.proto.ApplyJobResponse;
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
        TextView mJobPostApplyBtn, mJobPostTitleTextView, mJobPostCompanyTextView, mJobPostSalaryTextView, mJobPostExperienceTextView, mJobPostVacancyTextView, mJobPostLocationTextView, mJobPostPostedOnTextView, mApplyingJobBtnTextView;
        LinearLayout mApplyBtnBackground, applyBtn;
        ImageView mJobColor;
    }
    public LinearLayout applyingJobButton;
    public TextView applyingJobBtnTextView;
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
        holder.mJobPostApplyBtn = (TextView) rowView.findViewById(R.id.apply_button);

        holder.mApplyBtnBackground = (LinearLayout) rowView.findViewById(R.id.apply_button_layout);
        holder.applyBtn = (LinearLayout) rowView.findViewById(R.id.apply_button_layout);

        pd = CustomProgressDialog.get(parent.getContext());

        //presetting job card element as not applied
        holder.applyBtn.setEnabled(true);
        holder.mJobPostApplyBtn.setText("Apply");
        holder.mJobColor.setImageResource(R.drawable.green_dot);
        holder.mApplyBtnBackground.setBackgroundResource(R.drawable.rounded_corner_button);

        holder.mApplyingJobBtnTextView = (TextView) rowView.findViewById(R.id.apply_button);

        if(jobPost.getIsApplied() == 1){
            holder.applyBtn.setEnabled(false);
            holder.mJobColor.setImageResource(R.drawable.orange_dot);
            holder.mJobPostApplyBtn.setText("Already Applied");
            holder.mApplyBtnBackground.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
        }

        //when user is not logged in, show all jobs as not applied
        if(!Util.isLoggedIn()){
            holder.applyBtn.setEnabled(true);
            holder.mJobColor.setImageResource(R.drawable.green_dot);
            holder.mJobPostApplyBtn.setText("Apply");
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
                    alert.showDialog(getContext(), jobPost.getJobPostCompanyName() + "'s " + jobPost.getJobPostTitle() + " job locations:", allLocalities , "", R.drawable.location_round, -1);
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

        holder.applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyingJobButton = holder.mApplyBtnBackground;
                applyingJobBtnTextView = holder.mApplyingJobBtnTextView;
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
        } else{
            Toast.makeText(getContext(), "Please login/sign up to apply.",
                    Toast.LENGTH_LONG).show();
            Prefs.jobToApplyStatus.put(1);
            Prefs.getJobToApplyJobId.put(jobPost.getJobPostId());
            ((Activity)getContext()).finish();
        }
    }

    public void applyJob(Long jobPostId, Long localityId, Button detailPageApplyBtn){
        if(detailPageApplyBtn != null){
            applyingJobButtonDetail = detailPageApplyBtn;
        }
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
            if (applyJobResponse == null) {
                Toast.makeText(getContext(), "Something went wrong. Please try again later.",
                        Toast.LENGTH_LONG).show();
                Tlog.w("Null Response");
                return;
            }
            ViewDialog alert = new ViewDialog();
            if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_SUCCESS){
                alert.showDialog(getContext(), "Application Sent", "Your Application has been sent to the recruiter", "You can track your application in \"My Jobs\" option in the Menu", R.drawable.sent, 5);
                //setting "already applied" to apply button of the jobs list
                try {
                    applyingJobColor.setImageResource(R.drawable.orange_dot);
                    applyingJobButton.setEnabled(false);
                    applyingJobButton.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
                    applyingJobBtnTextView.setText("Already Applied");
                } catch (Exception ignored){}

                //setting "already applied" to job detail activity button
                try{
                    applyingJobButtonDetail.setText("Already Applied");
                    applyingJobButtonDetail.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
                    applyingJobButtonDetail.setEnabled(false);
                } catch (Exception ignored){}
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_ALREADY_APPLIED){
                alert.showDialog(getContext(), "Already Applied", "Looks like you have already applied to this job", "", R.drawable.sent, 5);
                try {
                    applyingJobButton.setEnabled(false);
                    applyingJobButton.setBackgroundColor(getContext().getResources().getColor(R.color.back_grey_dark));
                    applyingJobBtnTextView.setText("Already Applied");
                } catch (Exception ignored){}
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_JOB){
                alert.showDialog(getContext(), "No Job Found", "Looks like the job is no more active", "", R.drawable.sent, 0);
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_CANDIDATE){
                alert.showDialog(getContext(), "Candidate doesn't exists", "Please login to continue", "", R.drawable.sent, 0);
            } else{
                alert.showDialog(getContext(), "Something went wrong! Please try again", "Unable to contact out servers", "",  R.drawable.sent, 0);
            }
        }
    }

}
