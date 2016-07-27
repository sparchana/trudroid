package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import in.trujobs.dev.trudroid.R;
import in.trujobs.proto.JobPost;

/**
 * Created by batcoder1 on 27/7/16.
 */
public class JobPostAdapter extends ArrayAdapter<JobPost> {

    public JobPostAdapter(Activity context, List<JobPost> jobPostList) {
        super(context, 0, jobPostList);
    }
    public class Holder
    {
        TextView mJobPostTitleTextView, mJobPostCompanyTextView, mJobPostSalaryTextView;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        Holder holder = new Holder();
        JobPost jobPost= getItem(position);
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
        return rowView;
    }


}
