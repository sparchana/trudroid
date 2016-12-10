package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
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
    ImageView profileCompletePercent, personalArrow, preferenceArrow, educationArrow, experienceArrow;
    TextView profileStatusText, profileMsg, jobsApplied;
    TextView myJobsHeading,  myJobsSubHeading;

    public CandidateProfileActivity candidateProfileActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_profile_fragment, container, false);

        FloatingActionButton editBtn = (FloatingActionButton) view.findViewById(R.id.edit_btn);

        // track screen view
        ((CandidateProfileActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_VIEW_PROFILE);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateProfileActivity)getActivity()).setSupportActionBar(toolbar);

        ((CandidateProfileActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle(Prefs.firstName.get() + " " + Prefs.lastName.get());
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        personalArrow = (ImageView) view.findViewById(R.id.personal_drop_icon);
        preferenceArrow = (ImageView) view.findViewById(R.id.job_preference_drop_icon);
        experienceArrow = (ImageView) view.findViewById(R.id.experience_drop_icon);
        educationArrow = (ImageView) view.findViewById(R.id.educ_drop_icon);

        pd = CustomProgressDialog.get(getActivity());

        profileStatusText = (TextView) view.findViewById(R.id.profile_status_text);

        profileMsg = (TextView) view.findViewById(R.id.profile_msg);

        jobsApplied = (TextView) view.findViewById(R.id.candidate_applied_job_no);

        bodyOpen = true;
        preferenceOpen = false;
        educationOpen = false;
        experienceOpen = false;

        myJobsHeading = (TextView) view.findViewById(R.id.my_jobs_header);
        myJobsSubHeading = (TextView) view.findViewById(R.id.assessment_msg);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CandidateProfileBasic candidateProfileBasic = new CandidateProfileBasic();
                candidateProfileBasic.setArguments(getActivity().getIntent().getExtras());
                getFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .add(R.id.main_profile, candidateProfileBasic).commit();

                //Track this action
                ((CandidateProfileActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_PROFILE, Constants.GA_ACTION_EDIT_PROFILE);
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

        bodyPreference.setVisibility(View.GONE);
        bodyEducation.setVisibility(View.GONE);
        bodyExperience.setVisibility(View.GONE);

        personalArrow.setImageResource(R.drawable.down_button);
        preferenceArrow.setImageResource(R.drawable.arrow_right);
        experienceArrow.setImageResource(R.drawable.arrow_right);
        educationArrow.setImageResource(R.drawable.arrow_right);

        profileCompletePercent = (ImageView) view.findViewById(R.id.profile_status);

        headerPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bodyOpen){
                    bodyPersonal.setVisibility(View.GONE);
                    bodyOpen = false;
                    personalArrow.setImageResource(R.drawable.arrow_right);
                } else{
                    bodyOpen = true;
                    bodyPersonal.setVisibility(View.VISIBLE);
                    personalArrow.setImageResource(R.drawable.down_button);
                }
            }
        });

        headerPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(preferenceOpen){
                    preferenceOpen = false;
                    bodyPreference.setVisibility(View.GONE);
                    preferenceArrow.setImageResource(R.drawable.arrow_right);
                } else{
                    preferenceOpen = true;
                    bodyPreference.setVisibility(View.VISIBLE);
                    preferenceArrow.setImageResource(R.drawable.down_button);
                }
            }
        });

        headerExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(experienceOpen){
                    experienceOpen = false;
                    bodyExperience.setVisibility(View.GONE);
                    experienceArrow.setImageResource(R.drawable.arrow_right);
                } else{
                    experienceOpen = true;
                    bodyExperience.setVisibility(View.VISIBLE);
                    experienceArrow.setImageResource(R.drawable.down_button);
                }
            }
        });

        headerEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(educationOpen){
                    educationOpen = false;
                    bodyEducation.setVisibility(View.GONE);
                    educationArrow.setImageResource(R.drawable.arrow_right);
                } else{
                    educationOpen = true;
                    bodyEducation.setVisibility(View.VISIBLE);
                    educationArrow.setImageResource(R.drawable.down_button);
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
            pd.show();
        }

        @Override
        protected GetCandidateInformationResponse doInBackground(CandidateInformationRequest... params) {
            return HttpRequest.getCandidateInfo(params[0]);
        }

        @Override
        protected void onPostExecute(final GetCandidateInformationResponse getCandidateInformationResponse) {
            super.onPostExecute(getCandidateInformationResponse);
            pd.cancel();

            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                getActivity().finish();
            } else if (getCandidateInformationResponse == null) {
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null candidate Response");
                getActivity().finish();
                return;
            } else {
                if(getCandidateInformationResponse.getStatusValue() == ServerConstants.SUCCESS){
                    candidateProfileActivity = (CandidateProfileActivity) getActivity();
                    candidateProfileActivity.candidateInfo = getCandidateInformationResponse;

                    TextView candidateName = (TextView) view.findViewById(R.id.user_name);

                    TextView candidateGender = (TextView) view.findViewById(R.id.candidate_gender);

                    TextView candidateMobile = (TextView) view.findViewById(R.id.candidate_phone_number);

                    TextView candidateAge = (TextView) view.findViewById(R.id.candidate_age);

                    TextView candidateLocation = (TextView) view.findViewById(R.id.candidate_location);

                    //pref
                    TextView candidateJobPref = (TextView) view.findViewById(R.id.candidate_job_pref);

                    TextView candidateShiftPref = (TextView) view.findViewById(R.id.candidate_job_time_shift);

                    //education
                    TextView candidateDegree = (TextView) view.findViewById(R.id.candidate_degree);

                    TextView candidateCollege = (TextView) view.findViewById(R.id.candidate_college);

                    TextView candidateCourse = (TextView) view.findViewById(R.id.candidate_course);

                    //experience
                    TextView candidateTotalExp = (TextView) view.findViewById(R.id.candidate_experience);

                    TextView candidateCurrentCompany = (TextView) view.findViewById(R.id.candidate_current_company);

                    TextView candidateLastWithdrawnSalary = (TextView) view.findViewById(R.id.candidate_current_salary);

                    candidateName.setText("Hi " + getCandidateInformationResponse.getCandidate().getCandidateFirstName());

                    FloatingActionButton userIcon = (FloatingActionButton) view.findViewById(R.id.user_icon);
                    userIcon.setVisibility(View.VISIBLE);
                    userIcon.setImageResource(R.drawable.icon_male);

                    if(getCandidateInformationResponse.getCandidate().getCandidateProfileCompletePercent() > 80){
                        profileCompletePercent.setImageResource(R.drawable.thumbs_up);
                        profileStatusText.setText("Profile Complete");
                        profileMsg.setText("");

                    } else {
                        profileCompletePercent.setImageResource(R.drawable.thumbs_down);
                        profileStatusText.setText("Profile Incomplete");
                        profileMsg.setText("Complete Profile now!");

                        LinearLayout profileStatusLayout = (LinearLayout) view.findViewById(R.id.profile_status_layout);
                        profileStatusLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CandidateProfileBasic candidateProfileBasic = new CandidateProfileBasic();
                                candidateProfileBasic.setArguments(getActivity().getIntent().getExtras());
                                getFragmentManager().beginTransaction()
                                        .addToBackStack(null)
                                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                                        .add(R.id.main_profile, candidateProfileBasic).commit();
                            }
                        });
                    }

                    candidateAge.setText("Not Specified");
                    if(getCandidateInformationResponse.getCandidate().getCandidateDobMillis() != 0){
                        Date current = new Date();
                        Date bday = new Date(getCandidateInformationResponse.getCandidate().getCandidateDobMillis());

                        final Calendar calender = new GregorianCalendar();
                        calender.set(Calendar.HOUR_OF_DAY, 0);
                        calender.set(Calendar.MINUTE, 0);
                        calender.set(Calendar.SECOND, 0);
                        calender.set(Calendar.MILLISECOND, 0);
                        calender.setTimeInMillis(current.getTime() - bday.getTime());

                        int age = 0;
                        age = calender.get(Calendar.YEAR) - 1970;
                        age += (float) calender.get(Calendar.MONTH) / (float) 12;
                        candidateAge.setText(age + " years ");
                    }

                    jobsApplied.setText(getCandidateInformationResponse.getCandidate().getAppliedJobs() + "");

                    //setting my applications header message if candidate has applied in any one of the jobs
                    if(getCandidateInformationResponse.getCandidate().getAppliedJobs() > 0){
                        myJobsHeading.setText("Jobs you have applied");
                        myJobsSubHeading.setText("Go to my Applications");
                    } else{
                        myJobsHeading.setText("No Jobs Applied");
                        myJobsSubHeading.setText("Apply Now");
                    }
                    LinearLayout myJobStatusLayout = (LinearLayout) view.findViewById(R.id.my_jobs_status_layout);
                    myJobStatusLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent;
                            if(getCandidateInformationResponse.getCandidate().getAppliedJobs() > 0){
                                intent = new Intent(getContext(), JobApplicationActivity.class);
                            } else{
                                intent = new Intent(getContext(), SearchJobsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                            }
                            startActivity(intent);
                        }
                    });

                    candidateMobile.setText(getCandidateInformationResponse.getCandidate().getCandidateMobile());
                    if(getCandidateInformationResponse.getCandidate().getCandidateEducation().getCandidateInstitute() != ""){
                        candidateCollege.setText(getCandidateInformationResponse.getCandidate().getCandidateEducation().getCandidateInstitute());
                    } else{
                        candidateCollege.setText("Not Specified");
                    }
                    if(getCandidateInformationResponse.getCandidate().getCandidateEducation().getDegree().getDegreeName() != ""){
                        candidateCourse.setText(getCandidateInformationResponse.getCandidate().getCandidateEducation().getDegree().getDegreeName());
                    } else{
                        candidateCourse.setText("Not Specified");
                    }
                    if(getCandidateInformationResponse.getCandidate().getCandidateEducation().getEducation().getEducationName() != ""){
                        candidateDegree.setText(getCandidateInformationResponse.getCandidate().getCandidateEducation().getEducation().getEducationName());
                    } else{
                        candidateDegree.setText("Not Specified");
                    }
                    if(getCandidateInformationResponse.getCandidate().getCandidateGender() == 0){
                        candidateGender.setText("Male");
                        userIcon.setImageResource(R.drawable.icon_male);
                    } else if(getCandidateInformationResponse.getCandidate().getCandidateGender() == 1){
                        candidateGender.setText("Female");
                        userIcon.setImageResource(R.drawable.icon_female);
                    } else{
                        candidateGender.setText("Not specified");
                    }
                    if(getCandidateInformationResponse.getCandidate().getCandidateHomelocality().getLocalityName() != ""){
                        candidateLocation.setText(getCandidateInformationResponse.getCandidate().getCandidateHomelocality().getLocalityName());
                    } else{
                        candidateLocation.setText("Not Specified");
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

                    if(getCandidateInformationResponse.getCandidate().getCandidateTimeShiftPref().getTimeShiftName() != ""){
                        candidateShiftPref.setText(getCandidateInformationResponse.getCandidate().getCandidateTimeShiftPref().getTimeShiftName());
                    } else{
                        candidateShiftPref.setText("Not Specified");
                    }



                    if(getCandidateInformationResponse.getCandidate().getCandidateTotalExperience() != -1){
                        Integer expYears = getCandidateInformationResponse.getCandidate().getCandidateTotalExperience() / 12;
                        Integer expMonth = getCandidateInformationResponse.getCandidate().getCandidateTotalExperience() % 12;
                        if(expYears == 0 && expMonth == 0){
                            //is a fresher
                            candidateTotalExp.setText("Fresher");
                            candidateLastWithdrawnSalary.setText("Not Applicable");
                            candidateCurrentCompany.setText("Not Applicable");
                        } else{
                            //is not a fresher
                            if(expYears != 0){
                                candidateTotalExp.setText(expYears + " years " + expMonth + " months");
                            } else{
                                candidateTotalExp.setText(expMonth + " months");
                            }

                            //last withdrawn salary
                            if(getCandidateInformationResponse.getCandidate().getCandidateLastWithdrawnSalary() != 0){
                                candidateLastWithdrawnSalary.setText(getCandidateInformationResponse.getCandidate().getCandidateLastWithdrawnSalary() + "");
                            } else{
                                candidateLastWithdrawnSalary.setText("Not Specified");
                            }

                            //current company
                            candidateCurrentCompany.setText("Not Specified");
                            if(getCandidateInformationResponse.getCandidate().getCandidateIsEmployed() == 1){
                                if(getCandidateInformationResponse.getCandidate().getCandidateCurrentCompany() != ""){
                                    candidateCurrentCompany.setText(getCandidateInformationResponse.getCandidate().getCandidateCurrentCompany());
                                } else{
                                    candidateCurrentCompany.setText("Not Specified");
                                }
                            }
                        }
                    } else{
                        candidateTotalExp.setText("Not Specified");
                    }
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                            Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
