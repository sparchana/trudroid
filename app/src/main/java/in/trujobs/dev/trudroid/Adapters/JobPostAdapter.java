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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Trudroid;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.CustomDialog.ViewDialog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
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
        TextView mJobPostTitleTextView, mJobPostCompanyTextView, mJobPostSalaryTextView, mJobPostExperienceTextView, mJobPostVacancyTextView, mJobPostLocationTextView, mJobPostPostedOnTextView;
        Button mApplyBtnBackground;
        ImageView mJobColor;
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

        pd = CustomProgressDialog.get(parent.getContext());

        //presetting job card element as not applied
        holder.mJobColor.setImageResource(R.drawable.green_dot);
        holder.mApplyBtnBackground.setEnabled(true);
        holder.mApplyBtnBackground.setText("Apply");
        holder.mApplyBtnBackground.setBackgroundResource(R.drawable.rounded_corner_button);

        if(jobPost.getIsApplied() == 1){
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
                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_JOB_APPLIED_STATUS);
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


                    //Track this action
                    addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_SHOW_ALL_JOB_POST_LOCATION);
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

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_SHOW_JOB_POST_DETAIL);
            }
        });

        holder.mApplyBtnBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyingJobButton = holder.mApplyBtnBackground;
                applyingJobColor = holder.mJobColor;
                showJobLocality(jobPost);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_TRIED_TO_APPLY_FOR_JOB);

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
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (applyJobResponse == null) {
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

    /* Analytics params and methods */
    private Tracker mTracker;

    public void addScreenViewGA(String screenName) {

        Trudroid application = (Trudroid) getContext();
        mTracker = application.getDefaultTracker();

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void addActionGA(String screenName, String actionName) {

        // Obtain the shared Tracker instance.
        Trudroid application = (Trudroid) getContext();
        mTracker = application.getDefaultTracker();

        // Track this action
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction(actionName)
                .build());
    }

}
