package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.ViewDialog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.ApplyJobRequest;
import in.trujobs.proto.ApplyJobResponse;
import in.trujobs.proto.JobPost;

/**
 * Created by batcoder1 on 27/7/16.
 */
public class JobPostAdapter extends ArrayAdapter<JobPost> {

    private AsyncTask<ApplyJobRequest, Void, ApplyJobResponse> mAsyncTask;
    public JobPostAdapter(Activity context, List<JobPost> jobPostList) {
        super(context, 0, jobPostList);
    }
    public class Holder
    {
        TextView mJobPostTitleTextView, mJobPostCompanyTextView, mJobPostSalaryTextView, mJobPostExperienceTextView, mJobPostVacancyTextView, mJobPostLocationTextView;
    }
    ProgressDialog pd;

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        Holder holder = new Holder();
        final JobPost jobPost= getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.job_list_view_item, parent, false);
        }

        //set job post title
        holder.mJobPostTitleTextView = (TextView) rowView.findViewById(R.id.job_post_title_text_view);
        holder.mJobPostTitleTextView.setText(jobPost.getJobPostTitle());

        //set job post Company
        holder.mJobPostCompanyTextView = (TextView) rowView.findViewById(R.id.job_post_company_text_view);
        holder.mJobPostCompanyTextView.setText(jobPost.getJobPostCompanyName());

        //set job post salary
        holder.mJobPostSalaryTextView = (TextView) rowView.findViewById(R.id.job_post_salary_text_view);
        holder.mJobPostSalaryTextView.setText(jobPost.getJobPostMinSalary() + " - " + jobPost.getJobPostMaxSalary());

        //set job post experience
        holder.mJobPostExperienceTextView = (TextView) rowView.findViewById(R.id.job_post_location_text_view);
        holder.mJobPostExperienceTextView.setText(jobPost.getJobPostExperience().getExperienceType());

        //set job post vacancy
        holder.mJobPostVacancyTextView = (TextView) rowView.findViewById(R.id.job_post_vacancy_text_view);
        holder.mJobPostVacancyTextView.setText(jobPost.getVacancies() + " vacancies");

        //set job post localities
        holder.mJobPostLocationTextView = (TextView) rowView.findViewById(R.id.job_post_location_text_view);
        String localities = "";
        for (int i = 0; i < jobPost.getJobPostLocalityCount(); i++) {
            localities += jobPost.getJobPostLocality(i).getLocalityName() + ", ";
        }
        holder.mJobPostLocationTextView.setText(localities);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Job id = " + jobPost.getJobPostId(), Toast.LENGTH_LONG).show();
                JobDetailActivity.start(getContext(), jobPost.getJobPostTitle());
            }
        });

        LinearLayout applyBtn = (LinearLayout) rowView.findViewById(R.id.apply_btn);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final CharSequence[] localityList = new CharSequence[jobPost.getJobPostLocalityCount()];
                final Long[] localityId = new Long[jobPost.getJobPostLocalityCount()];
                for (int i = 0; i < jobPost.getJobPostLocalityCount(); i++) {
                    localityList[i] = jobPost.getJobPostLocality(i).getLocalityName();
                    localityId[i] = jobPost.getJobPostLocality(i).getLocalityId();
                }

                final AlertDialog alertDialog = new AlertDialog.Builder(
                        getContext())
                        .setCancelable(true)
                        .setTitle("Choose Job Locality")
                        .setSingleChoiceItems(localityList, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                applyJob(jobPost.getJobPostId(), localityId[which]);
                                dialog.dismiss();
                            }
                        }).create();
                alertDialog.show();
            }
        });

        return rowView;
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
                Log.w("","Null Response");
                return;
            }
            ViewDialog alert = new ViewDialog();
            if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_SUCCESS){
                alert.showDialog(getContext(), "Application Sent");
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_ALREADY_APPLIED){
                alert.showDialog(getContext(), "Already Applied");
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_JOB){
                alert.showDialog(getContext(), "No Job Found");
            } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_CANDIDATE){
                alert.showDialog(getContext(), "Candidate doesn't exists");
            } else{
                alert.showDialog(getContext(), "Something went wrong! Please try again");
            }
        }
    }
}
