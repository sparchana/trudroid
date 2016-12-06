package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
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
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            Intent intent = new Intent(CandidateProfileActivity.this, SearchJobsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            this.finish();
        } else {
           getFragmentManager().popBackStack();
            super.onBackPressed();
        }
    }

}
