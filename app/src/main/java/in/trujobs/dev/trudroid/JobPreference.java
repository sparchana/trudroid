package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Adapters.JobRoleAdapter;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobRoleResponse;

public class JobPreference extends AppCompatActivity {

    private AsyncTask<Void, Void, JobRoleResponse> mAsyncTask;
    GridView grid;
    boolean jobPrefOptionOne, jobPrefOptionTwo, jobPrefOptionThree;
    String[] jobRoleName;
    ProgressDialog pd;
    int[] imageId = {
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.enter_mobile,
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.enter_mobile,
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.job_apply,
            R.drawable.job_apply
    };

    LinearLayout jobRoleGridViewLayout, mJobPrefOne, mJobPrefTwo, mJobPrefThree;
    ImageView jobPrefImageView, jobPrefOneImage, jobPrefTwoImage, jobPrefThreeImage;
    TextView mJobPrefOneText, mJobPrefTwoText, mJobPrefThreeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_preference);

        // Set job all job preference as none
        jobPrefOptionOne = false;
        jobPrefOptionTwo = false;
        jobPrefOptionThree = false;

        jobRoleGridViewLayout = (LinearLayout) findViewById(R.id.job_role_grid_view_layout);
        jobPrefImageView = (ImageView) findViewById(R.id.job_pref_image_view);
        mJobPrefOne = (LinearLayout) findViewById(R.id.job_pref_one);
        mJobPrefTwo = (LinearLayout) findViewById(R.id.job_pref_two);
        mJobPrefThree = (LinearLayout) findViewById(R.id.job_pref_three);

        jobRoleGridViewLayout.setVisibility(View.GONE);

        mJobPrefOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJobRoles();
            }
        });

        mJobPrefTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJobRoles();
            }
        });

        mJobPrefThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJobRoles();
            }
        });

    }

    private void showJobRoles(){
        jobPrefImageView.setVisibility(View.GONE);
        mAsyncTask = new JobRoleAsyncTask();
        mAsyncTask.execute();
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
        protected void onPostExecute(JobRoleResponse jobRoleResponse) {
            super.onPostExecute(jobRoleResponse);
            mAsyncTask = null;
            pd.hide();
            if (jobRoleResponse == null) {
                Toast.makeText(JobPreference.this, "No Job roles available. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                return;
            }

            else {
                jobRoleGridViewLayout.setVisibility(View.VISIBLE);
                jobPrefOneImage = (ImageView) findViewById(R.id.job_pref_one_image_view);
                jobPrefTwoImage = (ImageView) findViewById(R.id.job_pref_two_image_view);
                jobPrefThreeImage = (ImageView) findViewById(R.id.job_pref_three_image_view);

                mJobPrefOneText = (TextView) findViewById(R.id.job_pref_one_text_view);
                mJobPrefTwoText = (TextView) findViewById(R.id.job_pref_two_text_view);
                mJobPrefThreeText = (TextView) findViewById(R.id.job_pref_three_text_view);

                jobRoleName = new String[jobRoleResponse.getJobRoleCount()];
                int i=0;
                for(i=0; i<jobRoleResponse.getJobRoleCount(); i++){
                    jobRoleName[i] = String.valueOf(jobRoleResponse.getJobRole(i).getJobRoleName());
                }
                final JobRoleAdapter adapter = new JobRoleAdapter(JobPreference.this, jobRoleName, imageId);
                grid=(GridView)findViewById(R.id.grid);
                grid.setAdapter(adapter);
                grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        addJobPref(imageId[+position],jobRoleName[+ position]);
                    }
                });
            }
        }
    }

    public void addJobPref(int jobRoleIcon,String jobRoleText){
        if(jobPrefOptionOne == false){
            jobPrefOneImage.setBackgroundResource(jobRoleIcon);
            mJobPrefOneText.setText(jobRoleText);
            jobPrefOptionOne = true;
            return;
        } else if(jobPrefOptionTwo == false){
            jobPrefTwoImage.setBackgroundResource(jobRoleIcon);
            mJobPrefTwoText.setText(jobRoleText);
            jobPrefOptionTwo = true;
            return;
        } else if(jobPrefOptionThree == false){
            jobPrefThreeImage.setBackgroundResource(jobRoleIcon);
            mJobPrefThreeText.setText(jobRoleText);
            jobPrefOptionThree = true;
            return;
        } else{
            Toast.makeText(JobPreference.this, "Maximum 3 job preferences allowed!", Toast.LENGTH_SHORT).show();
        }
    }
}
