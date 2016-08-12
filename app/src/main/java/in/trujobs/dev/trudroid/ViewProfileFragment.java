package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.CandidateInformationRequest;
import in.trujobs.proto.GetCandidateInformationResponse;
import in.trujobs.proto.JobRoleObject;
import in.trujobs.proto.LocalityObject;

/**
 * Created by batcoder1 on 9/8/16.
 */
public class ViewProfileFragment extends Fragment {

    View view;
    private AsyncTask<CandidateInformationRequest, Void, GetCandidateInformationResponse> mAsyncTask;
    LinearLayout headerPersonal, headerEducation, headerPreference, headerExperience, bodyPersonal, bodyEducation, bodyPreference, bodyExperience;
    ProgressDialog pd;
    Boolean bodyOpen, preferenceOpen, experienceOpen, educationOpen;
    ImageView assessment;

    public CandidateInfoActivity candidateInfoActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_profile_fragment, container, false);

/*        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.userIcon);
        fab.setImageResource(R.drawable.male_icon);*/

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateInfoActivity)getActivity()).setSupportActionBar(toolbar);

        ((CandidateInfoActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bodyOpen = true;
        preferenceOpen = false;
        educationOpen = false;
        experienceOpen = false;

        assessment = (ImageView) view.findViewById(R.id.assessment_status);
        assessment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CandidateProfileBasic candidateProfileBasic = new CandidateProfileBasic();
                candidateProfileBasic.setArguments(getActivity().getIntent().getExtras());
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_profile, candidateProfileBasic).commit();
            }
        });

        headerPersonal = (LinearLayout) view.findViewById(R.id.personal_header);
        headerPreference = (LinearLayout) view.findViewById(R.id.preference_header);
        headerEducation = (LinearLayout) view.findViewById(R.id.education_header);
        headerExperience = (LinearLayout) view.findViewById(R.id.experience_header);

        bodyPersonal = (LinearLayout) view.findViewById(R.id.personal_body);
        bodyPreference = (LinearLayout) view.findViewById(R.id.preference_body);
        bodyEducation = (LinearLayout) view.findViewById(R.id.education_body);
        bodyExperience = (LinearLayout) view.findViewById(R.id.experience_body);

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

        return view;
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
            pd = new ProgressDialog(getContext(),R.style.SpinnerTheme);
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
                Toast.makeText(getContext(), "Null Candidate returned",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null candidate Response");
                return;
            } else {
                candidateInfoActivity = (CandidateInfoActivity) getActivity();
                candidateInfoActivity.candidateInfo = getCandidateInformationResponse;
                TextView candidateName = (TextView) view.findViewById(R.id.user_name);
                TextView candidateGender = (TextView) view.findViewById(R.id.candidate_gender);
                TextView candidateMobile = (TextView) view.findViewById(R.id.candidate_phone_number);
                TextView candidateAge = (TextView) view.findViewById(R.id.candidate_age);
                TextView candidateLocation = (TextView) view.findViewById(R.id.candidate_location);

                TextView candidateJobPref = (TextView) view.findViewById(R.id.candidate_job_pref);
                TextView candidateLocalityPref = (TextView) view.findViewById(R.id.candidate_locality_pref);
                TextView candidateShiftPref = (TextView) view.findViewById(R.id.candidate_job_time_shift);

                TextView candidateDegree = (TextView) view.findViewById(R.id.candidate_degree);
                TextView candidateCollege = (TextView) view.findViewById(R.id.candidate_college);
                TextView candidateCourse = (TextView) view.findViewById(R.id.candidate_course);

                TextView candidateTotalExp = (TextView) view.findViewById(R.id.candidate_experience);
                TextView candidateCurrentCompany = (TextView) view.findViewById(R.id.candidate_current_company);
                TextView candidateLastWithdrawnSalary = (TextView) view.findViewById(R.id.candidate_current_salary);

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
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
