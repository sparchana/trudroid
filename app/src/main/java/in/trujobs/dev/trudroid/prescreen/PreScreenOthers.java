package in.trujobs.dev.trudroid.prescreen;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import in.trujobs.dev.trudroid.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreScreenOthers extends Fragment {


    public PreScreenOthers() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        View v = new View(getActivity());
        return textView;
    }
}
