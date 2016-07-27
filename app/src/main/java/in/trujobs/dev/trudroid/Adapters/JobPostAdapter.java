package in.trujobs.dev.trudroid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import in.trujobs.dev.trudroid.JobActivity;
import in.trujobs.dev.trudroid.R;

/**
 * Created by batcoder1 on 27/7/16.
 */
public class JobPostAdapter extends BaseAdapter {
    String[] jobPostTitle, jobPostCompany, jobPostSalary;
    Context context;
    private static LayoutInflater inflater=null;
    public JobPostAdapter(JobActivity jobActivity, String[] jobPostTitleRes, String[] jobPostCompanyRes, String[] jobPostSalaryRes) {
        jobPostTitle = jobPostTitleRes;
        jobPostCompany = jobPostCompanyRes;
        jobPostSalary = jobPostSalaryRes;
        context = jobActivity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public class Holder
    {
        TextView mJobPostTitleTextView, mJobPostCompanyTextView, mJobPostSalaryTextView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.job_list_view_item, null);

        //set job post title
        holder.mJobPostTitleTextView = (TextView) rowView.findViewById(R.id.job_post_title_text_view);
        holder.mJobPostTitleTextView.setText(jobPostTitle[position]);

        //set job post Company
        holder.mJobPostCompanyTextView = (TextView) rowView.findViewById(R.id.job_post_company_text_view);
        holder.mJobPostCompanyTextView.setText(jobPostCompany[position]);

        //set job post salary
        holder.mJobPostSalaryTextView = (TextView) rowView.findViewById(R.id.job_post_salary_text_view);
        holder.mJobPostSalaryTextView.setText(jobPostSalary[position]);
        return rowView;
    }


}
