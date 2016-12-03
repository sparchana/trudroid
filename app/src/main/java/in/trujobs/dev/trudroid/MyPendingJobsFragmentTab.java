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

public class MyPendingJobsFragmentTab extends Fragment {

    public JobApplicationActivity jobApplicationActivity;
    View rowView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        jobApplicationActivity = (JobApplicationActivity) getActivity();
        rowView = inflater.inflate(R.layout.under_review_tab, container, false);

        ListView myUnderReviewJobListView = (ListView) rowView.findViewById(R.id.my_jobs_under_review_list_view);

        if(jobApplicationActivity.pendingTabList.size() > 0){
            MyUnderReviewJobAdapter myUnderReviewJobAdapter = new MyUnderReviewJobAdapter(getActivity(), jobApplicationActivity.pendingTabList);
            myUnderReviewJobListView.setAdapter(myUnderReviewJobAdapter);
        } else{
            ImageView noUnderReviewJobImage = (ImageView) rowView.findViewById(R.id.no_under_review_jobs_image);
            noUnderReviewJobImage.setVisibility(View.VISIBLE);
        }

        return rowView;
    }
}
