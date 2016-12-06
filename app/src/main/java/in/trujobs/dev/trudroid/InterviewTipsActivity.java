package in.trujobs.dev.trudroid;

import android.os.Bundle;
import android.view.MenuItem;

public class InterviewTipsActivity extends TruJobsBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_tips);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Interview Tips");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
