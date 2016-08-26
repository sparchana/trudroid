package in.trujobs.dev.trudroid;

import android.os.Bundle;
import android.view.MenuItem;

import in.trujobs.proto.GetCandidateInformationResponse;

public class CandidateProfileActivity extends TruJobsBaseActivity {

    public GetCandidateInformationResponse candidateInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_info);

        ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
        viewProfileFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
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
