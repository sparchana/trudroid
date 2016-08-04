package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobPostResponse;

public class JobActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private AsyncTask<Void, Void, JobPostResponse> mAsyncTask;
    ProgressDialog pd;
    ListView jobPostListView;
    private Bundle jobPostExtraDetails;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewDialog alert = new ViewDialog();
                alert.showDialog(JobActivity.this, "Complete Assessment", "Increase your chance of getting a job by 50%", "Call us to know more!", R.drawable.assesment, 1);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showJobPosts();
    }

    private void showJobPosts(){
        mAsyncTask = new JobPostAsyncTask();
        mAsyncTask.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private class JobPostAsyncTask extends AsyncTask<Void,
            Void, JobPostResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(JobActivity.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected JobPostResponse doInBackground(Void... params) {
            return HttpRequest.getJobPosts();
        }

        @Override
        protected void onPostExecute(JobPostResponse jobPostResponse) {
            super.onPostExecute(jobPostResponse);
            pd.cancel();
            jobPostListView = (ListView) findViewById(R.id.jobs_list_view);
            if (jobPostResponse == null) {
                ImageView errorImageView = (ImageView) findViewById(R.id.something_went_wrong_image);
                ImageView noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);
                errorImageView.setVisibility(View.VISIBLE);
                jobPostListView.setVisibility(View.GONE);
                Log.w("","Null JobPosts Response");
                return;
            } else {
                if(jobPostResponse.getJobPostList().size() > 0){
                    jobPostExtraDetails = new Bundle();
                    Log.e("jobActivity", "Data: "+ jobPostResponse.getJobPostList().get(0));
                    JobPostAdapter jobPostAdapter = new JobPostAdapter(JobActivity.this, jobPostResponse.getJobPostList());
                    jobPostListView.setAdapter(jobPostAdapter);
                } else {
                    ImageView noJobsImageView = (ImageView) findViewById(R.id.no_jobs_image);
                    noJobsImageView.setVisibility(View.VISIBLE);
                    showToast("No jobs found in your locality");
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_logout) {
            Prefs.onLogout();
            Toast.makeText(JobActivity.this, "Logout Successful",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(JobActivity.this, WelcomeScreen.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
        } else if (id == R.id.nav_my_jobs) {
            Intent intent = new Intent(JobActivity.this, JobPreference.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(JobActivity.this, CandidateInfoActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
        }

        return true;
    }

    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
