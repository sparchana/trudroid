package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.CandidateInformationRequest;
import in.trujobs.proto.GetCandidateInformationResponse;

public class CandidateInfoActivity extends AppCompatActivity {

    private AsyncTask<CandidateInformationRequest, Void, GetCandidateInformationResponse> mAsyncTask;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getCandidateInfo();
    }

    public void getCandidateInfo(){
        CandidateInformationRequest.Builder candidateInfoBuilder = CandidateInformationRequest.newBuilder();
        candidateInfoBuilder.setCandidateMobile(Prefs.candidateMobile.get());
        mAsyncTask = new CandidateAsyncTask();
        mAsyncTask.execute(candidateInfoBuilder.build());
    }

    private class CandidateAsyncTask extends AsyncTask<CandidateInformationRequest,
            Void, GetCandidateInformationResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(CandidateInfoActivity.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected GetCandidateInformationResponse doInBackground(CandidateInformationRequest... params) {
            return HttpRequest.getCandidateInfo(params[0]);
        }

        @Override
        protected void onPostExecute(GetCandidateInformationResponse getCandidateInformationResponse) {
            super.onPostExecute(getCandidateInformationResponse);
            pd.cancel();
            if (getCandidateInformationResponse == null) {
                Toast.makeText(CandidateInfoActivity.this, "Null Candidate returned",
                        Toast.LENGTH_LONG).show();
                Tlog.w("Null candidate Response");
                return;
            } else {
                Toast.makeText(CandidateInfoActivity.this, "Name: "+ getCandidateInformationResponse.getCandidate().getCandidateFirstName() + " =======> ",
                        Toast.LENGTH_LONG).show();
                Tlog.e("Name: "+ getCandidateInformationResponse.getCandidate().getCandidateFirstName() + " =======> ");
            }
        }
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
