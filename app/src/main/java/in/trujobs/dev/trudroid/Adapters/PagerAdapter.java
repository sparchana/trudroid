package in.trujobs.dev.trudroid.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import in.trujobs.dev.trudroid.CompanyTabFragment;
import in.trujobs.dev.trudroid.JobTabFragment;

/**
 * Created by batcoder1 on 28/7/16.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                JobTabFragment jobTabFragment = new JobTabFragment();
                return jobTabFragment;
            case 1:
                CompanyTabFragment companyTabFragment = new CompanyTabFragment();
                return companyTabFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}