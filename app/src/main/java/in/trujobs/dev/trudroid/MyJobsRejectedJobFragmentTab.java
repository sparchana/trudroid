package in.trujobs.dev.trudroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import in.trujobs.dev.trudroid.Adapters.MyRejectedJobAdapter;

/**
 * Created by dodo on 25/11/16.
 */

public class MyJobsRejectedJobFragmentTab extends Fragment {
    public JobApplicationActivity jobApplicationActivity;
    View rowView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        jobApplicationActivity = (JobApplicationActivity) getActivity();
        rowView = inflater.inflate(R.layout.rejected_job_tab, container, false);

        ListView myRejectedJobListView = (ListView) rowView.findViewById(R.id.my_rejected_jobs_list_view);

        if(jobApplicationActivity.rejectedInterviewList.size() > 0){
            MyRejectedJobAdapter myRejectedJobAdapter = new MyRejectedJobAdapter(getActivity(), jobApplicationActivity.rejectedInterviewList);
            myRejectedJobListView.setAdapter(myRejectedJobAdapter);
        } else{
            ImageView noRejectedJobImage = (ImageView) rowView.findViewById(R.id.no_rejected_jobs_image);
            noRejectedJobImage.setVisibility(View.VISIBLE);
        }

        return rowView;
    }
}
