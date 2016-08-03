package in.trujobs.dev.trudroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by batcoder1 on 2/8/16.
 */
public class JobFragmentTab extends Fragment {

    public class Holder
    {
        LinearLayout jobApplyLayout;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.job_tab, container, false);
    }
}
