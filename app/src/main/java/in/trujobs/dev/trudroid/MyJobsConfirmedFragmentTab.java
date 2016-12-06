package in.trujobs.dev.trudroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import in.trujobs.dev.trudroid.Adapters.MyConfirmedJobsAdapter;

/**
 * Created by dodo on 25/11/16.
 */

public class MyJobsConfirmedFragmentTab extends Fragment {

    public JobApplicationActivity jobApplicationActivity;
    View rowView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        jobApplicationActivity = (JobApplicationActivity) getActivity();
        rowView = inflater.inflate(R.layout.confirmed_rescheduled_tab, container, false);

        ListView myConfirmedJobListView = (ListView) rowView.findViewById(R.id.my_jobs_confirmed_list_view);

        if(jobApplicationActivity.confirmedTabList.size() > 0){
            /**
             * here we are computing the starting index positions of various categories(today's interviews, upcoming interviews, past interviews)
             * order is like this: today's interviews -> upcoming interviews -> past interviews
             */

            int interviewTodayStartIndex = -1;
            int upcomingInterviewStartIndex = -1;
            int pastInterviewStartIndex = -1;

            if(jobApplicationActivity.todaysInterviewList.size() > 0){
                interviewTodayStartIndex = 0;
            }

            if(jobApplicationActivity.upcomingInterviewList.size() > 0){
                if(jobApplicationActivity.todaysInterviewList.size() > 0){
                    upcomingInterviewStartIndex = jobApplicationActivity.todaysInterviewList.size();
                } else{
                    upcomingInterviewStartIndex = 0;
                }
            }

            if(jobApplicationActivity.pastInterviewList.size() > 0){
                if(jobApplicationActivity.todaysInterviewList.size() > 0){
                    if(jobApplicationActivity.upcomingInterviewList.size() > 0){
                        pastInterviewStartIndex = jobApplicationActivity.todaysInterviewList.size() + jobApplicationActivity.upcomingInterviewList.size();
                    } else{
                        pastInterviewStartIndex = jobApplicationActivity.todaysInterviewList.size();
                    }
                } else{
                    if(jobApplicationActivity.upcomingInterviewList.size() > 0){
                        pastInterviewStartIndex = jobApplicationActivity.upcomingInterviewList.size();
                    } else{
                        pastInterviewStartIndex = 0;
                    }
                }
            }

            MyConfirmedJobsAdapter myConfirmedJobsAdapter = new MyConfirmedJobsAdapter(getActivity(), jobApplicationActivity.confirmedTabList,
                    interviewTodayStartIndex, upcomingInterviewStartIndex, pastInterviewStartIndex);
            myConfirmedJobListView.setAdapter(myConfirmedJobsAdapter);
        } else{
            ImageView noConfirmedJobImage = (ImageView) rowView.findViewById(R.id.no_confirmed_jobs_image);
            noConfirmedJobImage.setVisibility(View.VISIBLE);
        }
        return rowView;
    }
}
