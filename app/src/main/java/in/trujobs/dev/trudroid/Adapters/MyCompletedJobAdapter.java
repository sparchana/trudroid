package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import in.trujobs.dev.trudroid.JobApplicationDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.JobPostWorkFlowObject;

/**
 * Created by dodo on 25/11/16.
 */

public class MyCompletedJobAdapter extends ArrayAdapter<JobPostWorkFlowObject> {

    private Activity ctx;
    private int completedApplicationStartIndex;
    public MyCompletedJobAdapter(Activity context, List<JobPostWorkFlowObject> jobApplicationObjectList, int completedApplicationStartIndex) {
        super(context, 0, jobApplicationObjectList);
        ctx = context;
        this.completedApplicationStartIndex = completedApplicationStartIndex;
    }

    public class Holder
    {
        TextView mJobApplicationTitleTextView, mJobApplicationCompanyTextView, mJobApplicationSalaryTextView, mJobApplicationExperienceTextView, mInterviewResultTextView, mJobApplicationLocationTextView;
        LinearLayout completedInterviewHeader;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        MyCompletedJobAdapter.Holder holder = new MyCompletedJobAdapter.Holder();
        final JobPostWorkFlowObject jobApplicationObject = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.my_completed_job_listview, parent, false);
        }

        ImageView applicationStatusIcon = (ImageView) rowView.findViewById(R.id.application_status_icon);
        TextView applicationStatusText = (TextView) rowView.findViewById(R.id.application_status);

        holder.completedInterviewHeader = (LinearLayout) rowView.findViewById(R.id.completed_interview_header);
        holder.completedInterviewHeader.setVisibility(View.GONE);

        android.support.v7.widget.CardView cardView = (android.support.v7.widget.CardView) rowView.findViewById(R.id.card_view);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JobApplicationDetailActivity.showDetail(getContext(), jobApplicationObject);
            }
        });

        if(position == completedApplicationStartIndex && completedApplicationStartIndex != -1){
            holder.completedInterviewHeader.setVisibility(View.VISIBLE);
        }
        //set job Application title
        holder.mJobApplicationTitleTextView = (TextView) rowView.findViewById(R.id.job_post_title_text_view);
        holder.mJobApplicationTitleTextView.setText(jobApplicationObject.getJobPostObject().getJobPostTitle());

        //set job Application Company
        holder.mJobApplicationCompanyTextView = (TextView) rowView.findViewById(R.id.job_post_company_text_view);

        String title = "<b>" + jobApplicationObject.getJobPostObject().getJobPostCompanyName() + "</b>";
        holder.mJobApplicationCompanyTextView.setText(Html.fromHtml(title));

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

        //set interview result
        holder.mInterviewResultTextView = (TextView) rowView.findViewById(R.id.interview_result_status);
        holder.mInterviewResultTextView.setText(jobApplicationObject.getCandidateInterviewStatus().getStatusTitle());
        if(jobApplicationObject.getCandidateInterviewStatus().getStatusId() == ServerConstants.JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_SELECTED){
            applicationStatusIcon.setBackgroundResource(R.drawable.ic_correct);
            applicationStatusText.setText("Selected");
            applicationStatusText.setTextColor(getContext().getResources().getColor(R.color.colorGreen));

            holder.mInterviewResultTextView.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
        } else{
            applicationStatusIcon.setBackgroundResource(R.drawable.ic_error);
            applicationStatusText.setText("Rejected");
            applicationStatusText.setTextColor(getContext().getResources().getColor(R.color.colorRed));

            holder.mInterviewResultTextView.setTextColor(Color.RED);
        }

        return rowView;
    }
}