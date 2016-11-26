package in.trujobs.dev.trudroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import in.trujobs.dev.trudroid.Adapters.MyUnderReviewJobAdapter;

/**
 * Created by dodo on 25/11/16.
 */

public class MyJobsCompletedFragmentTab extends Fragment {
    public JobApplicationActivity jobApplicationActivity;
    View rowView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        jobApplicationActivity = (JobApplicationActivity) getActivity();
        rowView = inflater.inflate(R.layout.completed_job_tab, container, false);

        ListView myCompletedJobListView = (ListView) rowView.findViewById(R.id.my_completed_jobs_list_view);

        if(jobApplicationActivity.completedInterviewList.size() > 0){
            MyCompletedJobAdapter myUnderReviewJobAdapter = new MyCompletedJobAdapter(getActivity(), jobApplicationActivity.completedInterviewList);
            myCompletedJobListView.setAdapter(myUnderReviewJobAdapter);
        } else{
            ImageView noCompletedJobImage = (ImageView) rowView.findViewById(R.id.no_completed_jobs_image);
            noCompletedJobImage.setVisibility(View.VISIBLE);
        }

        return rowView;
    }
}
