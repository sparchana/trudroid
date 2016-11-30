package in.trujobs.dev.trudroid.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import in.trujobs.dev.trudroid.MyJobsCompletedFragmentTab;
import in.trujobs.dev.trudroid.MyJobsConfirmedFragmentTab;
import in.trujobs.dev.trudroid.MyJobsUnderReviewFragmentTab;

/**
 * Created by dodo on 25/11/16.
 */

public class AppliedJobsAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;

    public AppliedJobsAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MyJobsConfirmedFragmentTab();
            case 1:
                return new MyJobsCompletedFragmentTab();
            case 2:
                return new MyJobsUnderReviewFragmentTab();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}