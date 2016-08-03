package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Stack;

import in.trujobs.dev.trudroid.Adapters.JobRoleAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.AddJobRoleRequest;
import in.trujobs.proto.AddJobRoleResponse;
import in.trujobs.proto.JobRoleResponse;

public class JobPreference extends AppCompatActivity {

    private AsyncTask<Void, Void, JobRoleResponse> mAsyncTask;
    private AsyncTask<AddJobRoleRequest, Void, AddJobRoleResponse> mSaveJobPrefAsyncTask;
    GridView grid;
    boolean jobPrefOptionOne, jobPrefOptionTwo, jobPrefOptionThree;
    Stack jobPrefStack = new Stack();
    ProgressDialog pd;
    Long jobPrefOne = 0L, jobPrefTwo = 0L, jobPrefThree = 0L;
    ImageView jobPrefRemoveOne, jobPrefRemoveTwo, jobPrefRemoveThree;

    FrameLayout mJobPrefOne, mJobPrefTwo, mJobPrefThree;
    LinearLayout jobRoleGridViewLayout;
    ImageView jobPrefImageView, jobPrefOneImage, jobPrefTwoImage, jobPrefThreeImage;
    TextView mJobPrefOneText, mJobPrefTwoText, mJobPrefThreeText;

    Button saveJobPrefBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_preference);
        setTitle("Job Role Preference");

        // Set job all job preference as none
        jobPrefOptionOne = false;
        jobPrefOptionTwo = false;
        jobPrefOptionThree = false;

        jobPrefRemoveOne = (ImageView) findViewById(R.id.job_pref_one_remove);
        jobPrefRemoveTwo = (ImageView) findViewById(R.id.job_pref_two_remove);
        jobPrefRemoveThree = (ImageView) findViewById(R.id.job_pref_three_remove);

        jobRoleGridViewLayout = (LinearLayout) findViewById(R.id.job_role_grid_view_layout);
        jobPrefImageView = (ImageView) findViewById(R.id.job_pref_image_view);

        jobPrefRemoveOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeJobPref(1);
            }
        });

        jobPrefRemoveTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeJobPref(2);
            }
        });

        jobPrefRemoveThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeJobPref(3);
            }
        });

        mJobPrefOne = (FrameLayout) findViewById(R.id.job_pref_one);
        mJobPrefTwo = (FrameLayout) findViewById(R.id.job_pref_two);
        mJobPrefThree = (FrameLayout) findViewById(R.id.job_pref_three);

        jobRoleGridViewLayout.setVisibility(View.GONE);

        mJobPrefOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrRemoveJobRoles(1);
            }
        });

        mJobPrefTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrRemoveJobRoles(2);
            }
        });

        mJobPrefThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrRemoveJobRoles(3);
            }
        });

        saveJobPrefBtn = (Button) findViewById(R.id.add_job_role_pref_btn);
        saveJobPrefBtn.setEnabled(false);
        saveJobPrefBtn.setBackgroundResource(R.color.back_grey_dark);
        saveJobPrefBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveJobPreferences();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                Prefs.onLogout();
                Toast.makeText(JobPreference.this, "Logout Successful",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(JobPreference.this, WelcomeScreen.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showOrRemoveJobRoles(int preference){
        int check = 0;
        if(preference == 1){
            if(jobPrefOptionOne == true){
                removeJobPref(1);
            } else{
                check = 1;
            }
        } else if(preference == 2){
            if(jobPrefOptionTwo == true){
                removeJobPref(2);
            } else{
                check = 1;
            }
        } else{
            if(jobPrefOptionThree == true){
                removeJobPref(3);
            } else{
                check = 1;
            }
        }
        if(check == 1){
            jobPrefImageView.setVisibility(View.GONE);
            mAsyncTask = new JobRoleAsyncTask();
            mAsyncTask.execute();
        }
    }

    private class JobRoleAsyncTask extends AsyncTask<Void,
            Void, JobRoleResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(JobPreference.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected JobRoleResponse doInBackground(Void... params) {
            return HttpRequest.getJobRoles();
        }

        @Override
        protected void onPostExecute(final JobRoleResponse jobRoleResponse) {
            super.onPostExecute(jobRoleResponse);
            mAsyncTask = null;
            pd.cancel();
            ImageView defaultImage = (ImageView) findViewById(R.id.job_pref_image_view);
            ImageView errorImageView = (ImageView) findViewById(R.id.something_went_wrong_image);
            if (jobRoleResponse == null) {
                errorImageView.setVisibility(View.VISIBLE);
                defaultImage.setVisibility(View.GONE);
                Log.w("","Null jobRole Response");
                return;
            }

            else {
                errorImageView.setVisibility(View.GONE);
                jobRoleGridViewLayout.setVisibility(View.VISIBLE);
                jobPrefOneImage = (ImageView) findViewById(R.id.job_pref_one_image_view);
                jobPrefTwoImage = (ImageView) findViewById(R.id.job_pref_two_image_view);
                jobPrefThreeImage = (ImageView) findViewById(R.id.job_pref_three_image_view);

                mJobPrefOneText = (TextView) findViewById(R.id.job_pref_one_text_view);
                mJobPrefTwoText = (TextView) findViewById(R.id.job_pref_two_text_view);
                mJobPrefThreeText = (TextView) findViewById(R.id.job_pref_three_text_view);
                Log.e("jobPreference: ", "Data: "+ jobRoleResponse.getJobRoleList().get(0));

                final JobRoleAdapter adapter = new JobRoleAdapter(JobPreference.this, jobRoleResponse.getJobRoleList());
                grid = (GridView)findViewById(R.id.grid);
                grid.setAdapter(adapter);
                grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        addJobPref(jobRoleResponse, + position);
                        return;
                    }
                });
            }
        }
    }

    public void saveJobPreferences(){
        AddJobRoleRequest.Builder requestBuilder = AddJobRoleRequest.newBuilder();
        requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
        requestBuilder.setJobRolePrefOneId(jobPrefOne);
        requestBuilder.setJobRolePrefTwoId(jobPrefTwo);
        requestBuilder.setJobRolePrefThreeId(jobPrefThree);

        mSaveJobPrefAsyncTask = new SaveJobRolePrefAsyncTask();
        mSaveJobPrefAsyncTask.execute(requestBuilder.build());
    }

    private class SaveJobRolePrefAsyncTask extends AsyncTask<AddJobRoleRequest,
            Void, AddJobRoleResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(JobPreference.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected AddJobRoleResponse doInBackground(AddJobRoleRequest... params) {
            return HttpRequest.addJobPrefs(params[0]);
        }

        @Override
        protected void onPostExecute(final AddJobRoleResponse addJobRoleResponse) {
            super.onPostExecute(addJobRoleResponse);
            mAsyncTask = null;
            pd.cancel();
            if (addJobRoleResponse == null) {
                Toast.makeText(JobPreference.this, "Request Failed. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                return;
            }

            else {
                if(addJobRoleResponse.getStatusValue() == 1){
                    Intent intent;
                    Prefs.candidateJobPrefStatus.put(ServerConstants.JOBPREFERENCE_YES);
                    if(Prefs.candidateHomeLocalityStatus.get() == 0){
                        intent = new Intent(JobPreference.this, HomeLocality.class);
                    } else{
                        intent = new Intent(JobPreference.this, JobActivity.class);
                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                    finish();
                } else{
                    Toast.makeText(JobPreference.this, "Something went wrong. Please try again later",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void addJobPref(JobRoleResponse jobRoleResponse, int pos){
        saveJobPrefBtn = (Button) findViewById(R.id.add_job_role_pref_btn);
        if(jobPrefStack.search(jobRoleResponse.getJobRole(pos).getJobRoleId()) < 0){
            mJobPrefOne = (FrameLayout) findViewById(R.id.job_pref_one);
            mJobPrefTwo = (FrameLayout) findViewById(R.id.job_pref_two);
            mJobPrefThree = (FrameLayout) findViewById(R.id.job_pref_three);

            if(!jobPrefOptionOne){
                Picasso.with(getApplicationContext()).load(jobRoleResponse.getJobRole(pos).getJobRoleIcon()).into(jobPrefOneImage);
                mJobPrefOneText.setText(jobRoleResponse.getJobRole(pos).getJobRoleName());
                jobPrefRemoveOne.setVisibility(View.VISIBLE);
                jobPrefOptionOne = true;
                jobPrefOne = jobRoleResponse.getJobRole(pos).getJobRoleId();
                saveJobPrefBtn.setEnabled(true);
                saveJobPrefBtn.setBackgroundResource(R.color.colorPrimary);
                jobPrefStack.push(jobRoleResponse.getJobRole(pos).getJobRoleId());
            } else if(!jobPrefOptionTwo){
                Picasso.with(getApplicationContext()).load(jobRoleResponse.getJobRole(pos).getJobRoleIcon()).into(jobPrefTwoImage);
                mJobPrefTwoText.setText(jobRoleResponse.getJobRole(pos).getJobRoleName());
                jobPrefRemoveTwo.setVisibility(View.VISIBLE);
                jobPrefOptionTwo = true;
                jobPrefTwo = jobRoleResponse.getJobRole(pos).getJobRoleId();
                jobPrefStack.push(jobRoleResponse.getJobRole(pos).getJobRoleId());
            } else if(!jobPrefOptionThree){
                Picasso.with(getApplicationContext()).load(jobRoleResponse.getJobRole(pos).getJobRoleIcon()).into(jobPrefThreeImage);
                mJobPrefThreeText.setText(jobRoleResponse.getJobRole(pos).getJobRoleName());
                jobPrefRemoveThree.setVisibility(View.VISIBLE);
                jobPrefOptionThree = true;
                jobPrefThree = jobRoleResponse.getJobRole(pos).getJobRoleId();
                jobPrefStack.push(jobRoleResponse.getJobRole(pos).getJobRoleId());
            } else{
                Toast.makeText(JobPreference.this, "Maximum 3 job preferences allowed!", Toast.LENGTH_SHORT).show();
            }
        } else{
            Toast.makeText(JobPreference.this, "Already Selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeJobPref(int pos){
        saveJobPrefBtn = (Button) findViewById(R.id.add_job_role_pref_btn);
        switch (pos){
            case 1: Picasso.with(getApplicationContext()).load(R.drawable.edit).into(jobPrefOneImage);
                mJobPrefOneText.setText("1st Preference");
                jobPrefOptionOne = false;
                jobPrefRemoveOne.setVisibility(View.GONE);
                saveJobPrefBtn.setEnabled(false);
                saveJobPrefBtn.setBackgroundResource(R.color.back_grey_dark);
                removeJobPrefValueFromStack(jobPrefOne);
                jobPrefOne = 0L; break;

            case 2: Picasso.with(getApplicationContext()).load(R.drawable.edit).into(jobPrefTwoImage);
                mJobPrefTwoText.setText("2nd Preference");
                jobPrefOptionTwo = false;
                jobPrefRemoveTwo.setVisibility(View.GONE);
                removeJobPrefValueFromStack(jobPrefTwo);
                jobPrefTwo = 0L; break;

            case 3: Picasso.with(getApplicationContext()).load(R.drawable.edit).into(jobPrefThreeImage);
                mJobPrefThreeText.setText("3rd Preference");
                jobPrefOptionThree = false;
                jobPrefRemoveThree.setVisibility(View.GONE);
                removeJobPrefValueFromStack(jobPrefThree);
                jobPrefThree = 0L; break;
        }
    }

    public void removeJobPrefValueFromStack(Long jobRoleId){
        for(int i=0 ; i<jobPrefStack.size(); i++){
            if(jobPrefStack.get(i) == jobRoleId){
                jobPrefStack.remove(i);
                break;
            }
        }
    }
}