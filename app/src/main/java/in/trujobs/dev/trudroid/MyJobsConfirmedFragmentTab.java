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
            MyConfirmedJobsAdapter myConfirmedJobsAdapter = new MyConfirmedJobsAdapter(getActivity(), jobApplicationActivity.confirmedTabList);
            myConfirmedJobListView.setAdapter(myConfirmedJobsAdapter);
        } else{
            ImageView noConfirmedJobImage = (ImageView) rowView.findViewById(R.id.no_confirmed_jobs_image);
            noConfirmedJobImage.setVisibility(View.VISIBLE);
        }
        return rowView;
    }
}
