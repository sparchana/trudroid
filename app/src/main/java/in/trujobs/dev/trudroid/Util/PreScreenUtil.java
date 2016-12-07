package in.trujobs.dev.trudroid.Util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import in.trujobs.dev.trudroid.R;

/**
 * Created by hawk on 7/12/16.
 */
public class PreScreenUtil extends Fragment {

    /**
     * This method is a utlility method called by all fragments associated with pre-screen /
     * data collection activity from a candidate when they apply to a job
     *
     * This method creates a layout of dots indicated progress steps. If there are 5 fragments,
     * the layout returned will have 5 dots and current fragment's dot will be bigger in size
     *
     * @param context The current application context
     * @param view The view associated with the fragment
     * @param totalFragments This gives the count of total number of fragements / steps
     * @param currPosition This indicates the position of the current fragment
     *
     * @return a LinearLayout which has a group of dots with the current position dot bigger in size
     */
    public static LinearLayout getProgressDotLayout (Context context, View view,
                                                    int totalFragments, int currPosition)
    {
        LinearLayout progressLayout = (LinearLayout) view.findViewById(R.id.progressCount);

        // iterate on total fragments and create a progress dot for every count
        for(int i= 1; i<=totalFragments; i++) {

            ImageView progressDot = new ImageView(context);
            progressDot.setBackgroundResource(R.drawable.circle_small);

            // if we are iterating on the count associated with the current fragment, then lets set
            // a bigger size
            // if not set a smaller size
            if(i == currPosition) {
                progressDot.setLayoutParams(new LinearLayout.LayoutParams(25, 25));
            } else {
                progressDot.setLayoutParams(new LinearLayout.LayoutParams(10, 10));
            }

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) progressDot.getLayoutParams();
            lp.setMargins(5,35,5,35);
            progressDot.setLayoutParams(lp);

            // Keep adding the dot image view into the overall layout
            progressLayout.addView(progressDot);
        }

        return progressLayout;
    }
}