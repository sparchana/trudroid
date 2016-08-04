package in.trujobs.dev.trudroid.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.proto.ApplyJobRequest;
import in.trujobs.proto.ApplyJobResponse;
import in.trujobs.proto.JobPostObject;

/**
 * Created by batcoder1 on 2/8/16.
 */
public class OtherJobPostAdapter extends ArrayAdapter<JobPostObject>{
    private AsyncTask<ApplyJobRequest, Void, ApplyJobResponse> mAsyncTask;
    public OtherJobPostAdapter(Activity context, List<JobPostObject> jobPostList) {
        super(context, 0, jobPostList);
    }

    public class Holder
    {
        TextView mJobPostTitleTextView, mJobPostSalary, mJobPostLocation;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        Holder holder = new Holder();
        final JobPostObject jobPost = getItem(position);
        if(rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(
                    R.layout.company_other_jobs_list_item, parent, false);
        }

        //set job post title
        holder.mJobPostTitleTextView = (TextView) rowView.findViewById(R.id.company_other_job_title);
        holder.mJobPostTitleTextView.setText(jobPost.getJobPostTitle());

        //set job post salary
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.mJobPostSalary = (TextView) rowView.findViewById(R.id.company_other_job_min_salary);
        if(jobPost.getJobPostMaxSalary() != 0){
            holder.mJobPostSalary.setText("₹" + formatter.format(jobPost.getJobPostMinSalary()) + " - ₹" + formatter.format(jobPost.getJobPostMaxSalary()));
        } else{
            holder.mJobPostSalary.setText("₹" + formatter.format(jobPost.getJobPostMinSalary()));
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.jobPostId.put(jobPost.getJobPostId());
/*                JobDetailActivity.start(getContext(), jobPost.getJobRole().getJobRoleName());*/
            }
        });
        return rowView;
    }

}
