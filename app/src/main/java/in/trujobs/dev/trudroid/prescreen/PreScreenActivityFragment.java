package in.trujobs.dev.trudroid.prescreen;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.trujobs.dev.trudroid.R;
import in.trujobs.proto.PreScreenPopulateProtoResponse;

/**
 * A placeholder fragment containing a simple view.
 */
public class PreScreenActivityFragment extends Fragment {
    public PreScreenActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pre_screen, container, false);
    }
}
