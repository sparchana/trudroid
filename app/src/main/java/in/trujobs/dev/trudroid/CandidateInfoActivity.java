package in.trujobs.dev.trudroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import in.trujobs.proto.GetCandidateInformationResponse;

public class CandidateInfoActivity extends AppCompatActivity {

    public GetCandidateInformationResponse candidateInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_info);

        ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
        viewProfileFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
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


}
