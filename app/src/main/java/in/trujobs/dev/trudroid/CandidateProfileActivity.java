package in.trujobs.dev.trudroid;

import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import in.trujobs.proto.GetCandidateInformationResponse;

public class CandidateProfileActivity extends AppCompatActivity {

    public GetCandidateInformationResponse candidateInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_info);

        ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
        viewProfileFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .add(R.id.main_profile, viewProfileFragment).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            this.finish();
        } else {
           getFragmentManager().popBackStack();
            super.onBackPressed();
        }
    }

}
