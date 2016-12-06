package in.trujobs.dev.trudroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import in.trujobs.dev.trudroid.Adapters.MyPendingJobAdapter;

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
            /**
             * here we are computing the starting index positions of various categories(under review, rescheduled etc)
             * order is like this: rescheduled -> Under review -> Rejected
             */

            int rescheduledStartIndex = -1;
            int underReviewStartIndex = -1;
            int rejectedStartIndex = -1;

            if(jobApplicationActivity.rescheduledList.size() > 0){
                rescheduledStartIndex = 0;
            }

            if(jobApplicationActivity.underReviewInterviewList.size() > 0){
                if(jobApplicationActivity.rescheduledList.size() > 0){
                    underReviewStartIndex = jobApplicationActivity.rescheduledList.size();
                } else{
                    underReviewStartIndex = 0;
                }
            }

            if(jobApplicationActivity.rejectedInterviewList.size() > 0){
                if(jobApplicationActivity.rescheduledList.size() > 0){
                    if(jobApplicationActivity.underReviewInterviewList.size() > 0){
                        rejectedStartIndex = jobApplicationActivity.rescheduledList.size() + jobApplicationActivity.underReviewInterviewList.size();
                    } else{
                        rejectedStartIndex = jobApplicationActivity.rescheduledList.size();
                    }
                } else{
                    if(jobApplicationActivity.underReviewInterviewList.size() > 0){
                        rejectedStartIndex = jobApplicationActivity.underReviewInterviewList.size();
                    } else{
                        rejectedStartIndex = 0;
                    }
                }
            }

            MyPendingJobAdapter myPendingJobAdapter = new MyPendingJobAdapter(getActivity(), jobApplicationActivity.pendingTabList,
                    rescheduledStartIndex, underReviewStartIndex, rejectedStartIndex);
            myUnderReviewJobListView.setAdapter(myPendingJobAdapter);
        } else{
            ImageView noUnderReviewJobImage = (ImageView) rowView.findViewById(R.id.no_under_review_jobs_image);
            noUnderReviewJobImage.setVisibility(View.VISIBLE);
        }

        return rowView;
    }
}
