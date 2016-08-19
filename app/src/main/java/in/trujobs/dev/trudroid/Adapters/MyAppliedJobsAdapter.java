package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import in.trujobs.dev.trudroid.MyAppliedJobs;
import in.trujobs.dev.trudroid.R;
import in.trujobs.proto.JobApplicationObject;

/**
 * Created by batcoder1 on 8/8/16.
 */
public class MyAppliedJobsAdapter extends ArrayAdapter<JobApplicationObject> {

    public MyAppliedJobsAdapter(Activity context, List<JobApplicationObject> jobApplicationObjectList) {
        super(context, 0, jobApplicationObjectList);
    }

    public class Holder
    {
        TextView mJobApplicationTitleTextView, mJobApplicationCompanyTextView, mJobApplicationSalaryTextView, mJobApplicationExperienceTextView, mJobApplicationApplyTextView, mJobApplicationLocationTextView;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        Holder holder = new Holder();
        final JobApplicationObject jobApplicationObject = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.my_jobs_listview, parent, false);
        }

        //set job Application title
        holder.mJobApplicationTitleTextView = (TextView) rowView.findViewById(R.id.job_post_title_text_view);
        holder.mJobApplicationTitleTextView.setText(jobApplicationObject.getJobPost().getJobPostTitle());

        //set job Application Company
        holder.mJobApplicationCompanyTextView = (TextView) rowView.findViewById(R.id.job_post_company_text_view);
        holder.mJobApplicationCompanyTextView.setText(jobApplicationObject.getJobPost().getJobPostCompanyName());

        //set job Application salary
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.mJobApplicationSalaryTextView = (TextView) rowView.findViewById(R.id.job_post_salary_text_view);
        if(jobApplicationObject.getJobPost().getJobPostMaxSalary() != 0){
            holder.mJobApplicationSalaryTextView.setText("₹" + formatter.format(jobApplicationObject.getJobPost().getJobPostMinSalary()) + " - ₹" + formatter.format(jobApplicationObject.getJobPost().getJobPostMaxSalary()));
        } else{
            holder.mJobApplicationSalaryTextView.setText("₹" + formatter.format(jobApplicationObject.getJobPost().getJobPostMinSalary()));
        }

        //set job Application experience
        holder.mJobApplicationExperienceTextView = (TextView) rowView.findViewById(R.id.job_post_exp_text_view);
        holder.mJobApplicationExperienceTextView.setText(jobApplicationObject.getJobPost().getJobPostExperience().getExperienceType());

        //set job Application localities
        holder.mJobApplicationLocationTextView = (TextView) rowView.findViewById(R.id.job_post_location_text_view);
        if(jobApplicationObject.getPreScreenLocation() != null){
            holder.mJobApplicationLocationTextView.setText(jobApplicationObject.getPreScreenLocation().getLocalityName());
        } else {
            holder.mJobApplicationLocationTextView.setText("Not Specified");
        }

        //set job Application Apply date
        holder.mJobApplicationApplyTextView = (TextView) rowView.findViewById(R.id.job_apply_date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(jobApplicationObject.getJobApplicationAppliedMillis());
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        holder.mJobApplicationApplyTextView.setText("Applied on: " + mDay + "-" + mMonth + "-" + mYear);

        return rowView;
    }
}

