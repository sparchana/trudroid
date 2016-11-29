package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.JobPostWorkFlowObject;

/**
 * Created by dodo on 25/11/16.
 */

public class MyUnderReviewJobAdapter extends ArrayAdapter<JobPostWorkFlowObject> {

    public MyUnderReviewJobAdapter(Activity context, List<JobPostWorkFlowObject> jobApplicationObjectList) {
        super(context, 0, jobApplicationObjectList);
    }

    public class Holder
    {
        TextView mJobApplicationTitleTextView, mJobApplicationCompanyTextView, mJobApplicationSalaryTextView, mJobApplicationExperienceTextView, mLastUpdateTextView;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        MyUnderReviewJobAdapter.Holder holder = new MyUnderReviewJobAdapter.Holder();
        final JobPostWorkFlowObject jobApplicationObject = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.my_job_under_review_listview, parent, false);
        }

        ImageView applicationStatusIcon = (ImageView) rowView.findViewById(R.id.application_status_icon);
        TextView applicationStatusText = (TextView) rowView.findViewById(R.id.application_status);

        if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_CANDIDATE ||
                jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_INTERVIEW_REJECTED_BY_RECRUITER_SUPPORT){

            applicationStatusIcon.setBackgroundResource(R.drawable.ic_error);
            applicationStatusText.setText("Not Shortlisted");
            applicationStatusText.setTextColor(getContext().getResources().getColor(R.color.colorRed));

        } else {
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

        return rowView;
    }
}


