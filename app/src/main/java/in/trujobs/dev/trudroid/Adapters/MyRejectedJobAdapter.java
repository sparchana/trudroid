package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

import in.trujobs.dev.trudroid.MyCompletedJobAdapter;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.proto.JobPostWorkFlowObject;

/**
 * Created by dodo on 25/11/16.
 */

public class MyRejectedJobAdapter extends ArrayAdapter<JobPostWorkFlowObject> {

    public MyRejectedJobAdapter(Activity context, List<JobPostWorkFlowObject> jobApplicationObjectList) {
        super(context, 0, jobApplicationObjectList);
    }

    public class Holder
    {
        TextView mJobApplicationTitleTextView, mJobApplicationCompanyTextView, mJobApplicationSalaryTextView, mJobApplicationExperienceTextView, mJobApplicationApplyTextView, mJobApplicationLocationTextView;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        MyRejectedJobAdapter.Holder holder = new MyRejectedJobAdapter.Holder();
        final JobPostWorkFlowObject jobApplicationObject = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.my_rejected_job_listview, parent, false);
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

        TextView rejectedTextView = (TextView) rowView.findViewById(R.id.rejected_text_view);
        rejectedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Your application was not shortlisted by the recruiter for the interview!", Toast.LENGTH_LONG).show();
            }
        });

        return rowView;
    }
}
