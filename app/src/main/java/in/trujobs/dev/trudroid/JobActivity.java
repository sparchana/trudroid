package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Adapters.DashboardOptionsAdapter;
import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.Adapters.JobRoleAdapter;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobRoleResponse;

public class JobActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private AsyncTask<Void, Void, JobPostResponse> mAsyncTask;
    ProgressDialog pd;
    String[] jobPostTitle, jobPostCompany, jobPostSalary;
    Long[] jobPostId;
    ListView jobPostListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.job, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            mAsyncTask = null;
            pd.cancel();
            if (jobPostResponse == null) {
                Toast.makeText(JobActivity.this, "No Jobs available right now. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                return;
            }

            else {
                jobPostTitle = new String[jobPostResponse.getJobPostCount()];
                jobPostCompany = new String[jobPostResponse.getJobPostCount()];
                jobPostSalary = new String[jobPostResponse.getJobPostCount()];
                int i=0;
                long minSalary, maxSalary;
                for(i=0; i<jobPostResponse.getJobPostCount(); i++){
                    jobPostTitle[i] = jobPostResponse.getJobPost(i).getJobPostTitle();
                    jobPostCompany[i] = jobPostResponse.getJobPost(i).getJobPostCompanyName();
                    minSalary = jobPostResponse.getJobPost(i).getJobPostMinSalary();
                    maxSalary = jobPostResponse.getJobPost(i).getJobPostMaxSalary();
                    if(maxSalary == 0){
                        jobPostSalary[i] = String.valueOf(minSalary);
                    }
                    jobPostSalary[i] = minSalary + " - " + maxSalary;
                }

                jobPostListView = (ListView) findViewById(R.id.jobs_list_view);
                jobPostListView.setAdapter(new JobPostAdapter(JobActivity.this, jobPostTitle, jobPostCompany, jobPostSalary));
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
