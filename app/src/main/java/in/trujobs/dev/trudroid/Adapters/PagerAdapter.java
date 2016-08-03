package in.trujobs.dev.trudroid.Adapters;

/**
 * Created by batcoder1 on 2/8/16.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import in.trujobs.dev.trudroid.CompanyFragmentTab;
import in.trujobs.dev.trudroid.JobFragmentTab;
import in.trujobs.dev.trudroid.OtherJobFragmentTab;

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
                JobFragmentTab jobFragmentTab = new JobFragmentTab();
                return jobFragmentTab;
            case 1:
                CompanyFragmentTab companyFragmentTab = new CompanyFragmentTab();
                return companyFragmentTab;
/*            case 2:
                OtherJobFragmentTab otherJobFragmentTab = new OtherJobFragmentTab();
                return otherJobFragmentTab;*/
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}