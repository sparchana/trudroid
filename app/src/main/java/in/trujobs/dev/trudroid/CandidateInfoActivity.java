package in.trujobs.dev.trudroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.CandidateInformationRequest;
import in.trujobs.proto.GetCandidateInformationResponse;
import in.trujobs.proto.JobRoleObject;
import in.trujobs.proto.LocalityObject;

public class CandidateInfoActivity extends AppCompatActivity {

    private AsyncTask<CandidateInformationRequest, Void, GetCandidateInformationResponse> mAsyncTask;
    LinearLayout headerPersonal, headerEducation, headerPreference, headerExperience, bodyPersonal, bodyEducation, bodyPreference, bodyExperience;
    ProgressDialog pd;
    Boolean bodyOpen, preferenceOpen, experienceOpen, educationOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.userIcon);
        fab.setImageResource(R.drawable.male_icon);

        bodyOpen = true;
        preferenceOpen = false;
        educationOpen = false;
        experienceOpen = false;

        headerPersonal = (LinearLayout) findViewById(R.id.personal_header);
        headerPreference = (LinearLayout) findViewById(R.id.preference_header);
        headerEducation = (LinearLayout) findViewById(R.id.education_header);
        headerExperience = (LinearLayout) findViewById(R.id.experience_header);

        bodyPersonal = (LinearLayout) findViewById(R.id.personal_body);
        bodyPreference = (LinearLayout) findViewById(R.id.preference_body);
        bodyEducation = (LinearLayout) findViewById(R.id.education_body);
        bodyExperience = (LinearLayout) findViewById(R.id.experience_body);

        headerPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bodyOpen){
                    bodyPersonal.setVisibility(View.GONE);
                    bodyOpen = false;
                } else{
                    bodyOpen = true;
                    bodyPersonal.setVisibility(View.VISIBLE);
                }
            }
        });

        headerPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(preferenceOpen){
                    preferenceOpen = false;
                    bodyPreference.setVisibility(View.GONE);
                } else{
                    preferenceOpen = true;
                    bodyPreference.setVisibility(View.VISIBLE);
                }
            }
        });

        headerExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(experienceOpen){
                    experienceOpen = false;
                    bodyExperience.setVisibility(View.GONE);
                } else{
                    experienceOpen = true;
                    bodyExperience.setVisibility(View.VISIBLE);
                }
            }
        });

        headerEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(educationOpen){
                    educationOpen = false;
                    bodyEducation.setVisibility(View.GONE);
                } else{
                    educationOpen = true;
                    bodyEducation.setVisibility(View.VISIBLE);
                }
            }
        });
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
                finish();
                Log.w("","Null candidate Response");
                return;
            } else {
                TextView candidateName = (TextView) findViewById(R.id.user_name);
                TextView candidateGender = (TextView) findViewById(R.id.candidate_gender);
                TextView candidateMobile = (TextView) findViewById(R.id.candidate_phone_number);
                TextView candidateAge = (TextView) findViewById(R.id.candidate_age);
                TextView candidateLocation = (TextView) findViewById(R.id.candidate_location);

                TextView candidateJobPref = (TextView) findViewById(R.id.candidate_job_pref);
                TextView candidateLocalityPref = (TextView) findViewById(R.id.candidate_locality_pref);
                TextView candidateShiftPref = (TextView) findViewById(R.id.candidate_job_time_shift);

                TextView candidateDegree = (TextView) findViewById(R.id.candidate_degree);
                TextView candidateCollege = (TextView) findViewById(R.id.candidate_college);
                TextView candidateCourse = (TextView) findViewById(R.id.candidate_course);

                TextView candidateTotalExp = (TextView) findViewById(R.id.candidate_experience);
                TextView candidateCurrentCompany = (TextView) findViewById(R.id.candidate_current_company);
                TextView candidateLastWithdrawnSalary = (TextView) findViewById(R.id.candidate_current_salary);

                candidateName.setText("Hi " + getCandidateInformationResponse.getCandidate().getCandidateFirstName());
                candidateAge.setText("22 years");

                candidateMobile.setText(getCandidateInformationResponse.getCandidate().getCandidateMobile());
                if(getCandidateInformationResponse.getCandidate().getCandidateEducation().getCandidateInstitute() != null){
                    candidateCollege.setText(getCandidateInformationResponse.getCandidate().getCandidateEducation().getCandidateInstitute());
                } else{
                    candidateCollege.setText("Not Specified");
                }
                if(getCandidateInformationResponse.getCandidate().getCandidateEducation().getDegree() != null){
                    candidateDegree.setText(getCandidateInformationResponse.getCandidate().getCandidateEducation().getDegree().getDegreeName());
                } else{
                    candidateDegree.setText("Not Specified");
                }
                if(getCandidateInformationResponse.getCandidate().getCandidateCurrentCompany() != null){
                    candidateCurrentCompany.setText(getCandidateInformationResponse.getCandidate().getCandidateCurrentCompany());
                } else{
                    candidateCurrentCompany.setText("Not Specified");
                }
                if(getCandidateInformationResponse.getCandidate().getCandidateEducation().getEducation() != null){
                    candidateDegree.setText(getCandidateInformationResponse.getCandidate().getCandidateEducation().getEducation().getEducationName());
                } else{
                    candidateDegree.setText("Not Specified");
                }
                candidateLastWithdrawnSalary.setText(getCandidateInformationResponse.getCandidate().getCandidateLastWithdrawnSalary() + "");
                if(getCandidateInformationResponse.getCandidate().getCandidateGender() == 0){
                    candidateGender.setText("Male");
                } else if(getCandidateInformationResponse.getCandidate().getCandidateGender() == 1){
                    candidateGender.setText("Female");
                } else{
                    candidateGender.setText("Not specified");
                }
                if(getCandidateInformationResponse.getCandidate().getCandidateHomelocality() != null){
                    candidateLocation.setText(getCandidateInformationResponse.getCandidate().getCandidateHomelocality().getLocalityName());
                } else{
                    candidateLocation.setText("Home Location not Specified");
                }

                String jobRolePref = "";
                List<JobRoleObject> jobRoleObjectList = getCandidateInformationResponse.getCandidate().getCandidateJobRolePrefList();
                if(jobRoleObjectList.size() > 0){
                    for(int i=0; i<jobRoleObjectList.size(); i++){
                        jobRolePref += jobRoleObjectList.get(i).getJobRoleName();

                        if(i != (jobRoleObjectList.size() - 1)){
                            jobRolePref += ", ";
                        }
                    }
                    candidateJobPref.setText(jobRolePref);
                } else{
                    candidateJobPref.setText("Not Specified");
                }

                String localityPref = "";
                List<LocalityObject> localityObjectsList = getCandidateInformationResponse.getCandidate().getCandidateLocationPrefList();
                if(localityObjectsList.size() > 0){
                    for(int i=0; i<localityObjectsList.size(); i++){
                        localityPref += localityObjectsList.get(i).getLocalityName();

                        if(i != (localityObjectsList.size() - 1)){
                            localityPref += ", ";
                        }
                    }
                    candidateLocalityPref.setText(localityPref);
                } else{
                    candidateLocalityPref.setText("Not Specified");
                }

                if(getCandidateInformationResponse.getCandidate().getCandidateTimeShiftPref() != null){
                    candidateShiftPref.setText(getCandidateInformationResponse.getCandidate().getCandidateTimeShiftPref().getTimeShiftName());
                } else{
                    candidateShiftPref.setText("Not Specified");
                }

                Integer expYears = getCandidateInformationResponse.getCandidate().getCandidateTotalExperience() / 12;
                Integer expMonth = getCandidateInformationResponse.getCandidate().getCandidateTotalExperience() % 12;
                if(expYears != 0){
                    candidateTotalExp.setText(expYears + " years " + expMonth + " months");
                } else if(expYears == 0 && expMonth == 0) {
                    candidateTotalExp.setText("Fresher");
                } else{
                    candidateTotalExp.setText(expMonth + " months");
                }

                Log.e("CandidateInfo", "Name: "+ getCandidateInformationResponse.getCandidate().getCandidateFirstName() + " =======> ");
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
