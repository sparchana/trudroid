package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.GetCandidateEducationProfileStaticResponse;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;
import in.trujobs.proto.UpdateCandidateEducationProfileRequest;

/**
 * Created by batcoder1 on 11/8/16.
 */
public class CandidateProfileEducation extends Fragment {

    private AsyncTask<UpdateCandidateEducationProfileRequest, Void, UpdateCandidateBasicProfileResponse> UpdateEducationAsyncTask;
    private AsyncTask<Void, Void, GetCandidateEducationProfileStaticResponse> mAsyncTask;
    ProgressDialog pd;

    LinearLayout degreeSection;
    LinearLayout qualificationBackground;
    LinearLayout qualificationLayout;
    LinearLayout degreeLayout;
    EditText candidateCollege;
    Button updateEducationProfile;
    TextView qualificationStatusText;
    int qualificationPos = 0, degreePos = 0, firstTimeSetting = 0;
    SpinnerAdapter adapter;
    public CandidateProfileActivity candidateProfileActivity;
    View view;

    String[] qualificationLevel = new String[0];
    Long[] qualificationId = new Long[0];

    String[] degreeName = new String[0];
    Long[] degreeId = new Long[0];

    //values
    private Long qualificationSelected = Long.valueOf(0), degreeSelected = Long.valueOf(0);

    Integer qualificationStatus = -1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.candidate_education_profile, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateProfileActivity)getActivity()).setSupportActionBar(toolbar);

        ((CandidateProfileActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        qualificationLayout = (LinearLayout) view.findViewById(R.id.qualification_layout);
        degreeLayout = (LinearLayout) view.findViewById(R.id.degree_layout);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Education Profile");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        pd = CustomProgressDialog.get(getActivity());

        mAsyncTask = new GetEducationStaticAsyncTask();
        mAsyncTask.execute();

        return view;
    }

    private class UpdateEducationProfileAsyncTask extends AsyncTask<UpdateCandidateEducationProfileRequest,
            Void, UpdateCandidateBasicProfileResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected UpdateCandidateBasicProfileResponse doInBackground(UpdateCandidateEducationProfileRequest ... params) {
            return HttpRequest.updateCandidateEducationProfile(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateCandidateBasicProfileResponse updateCandidateBasicProfileResponse) {
            super.onPostExecute(updateCandidateBasicProfileResponse);
            pd.cancel();
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if(updateCandidateBasicProfileResponse == null){
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
            } else{
                if(updateCandidateBasicProfileResponse.getStatusValue() == ServerConstants.SUCCESS){
                    Toast.makeText(getContext(), MessageConstants.PROFILE_UPDATED,
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), CandidateProfileActivity.class);
                    getActivity().finish();
                    getContext().startActivity(intent);
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong while saving education profile. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class GetEducationStaticAsyncTask extends AsyncTask<Void,
            Void, GetCandidateEducationProfileStaticResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(getActivity());
            pd.show();
        }

        @Override
        protected GetCandidateEducationProfileStaticResponse doInBackground(Void... params) {
            return HttpRequest.getCandidateEducationProfileStatic();
        }

        @Override
        protected void onPostExecute(GetCandidateEducationProfileStaticResponse getCandidateEducationProfileStaticResponse) {
            super.onPostExecute(getCandidateEducationProfileStaticResponse);
            pd.cancel();
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if (getCandidateEducationProfileStaticResponse == null) {
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null Response");
                return;
            } else {
                if(getCandidateEducationProfileStaticResponse.getStatusValue() == ServerConstants.SUCCESS){
                    candidateProfileActivity = (CandidateProfileActivity) getActivity();

                    updateEducationProfile = (Button) view.findViewById(R.id.save_education);
                    final Spinner candidateQualification = (Spinner) view.findViewById(R.id.candidate_qualification);
                    final Spinner candidateDegree = (Spinner) view.findViewById(R.id.candidate_degree);

                    qualificationLevel = new String[getCandidateEducationProfileStaticResponse.getEducationObjectCount() + 1];
                    qualificationId = new Long[getCandidateEducationProfileStaticResponse.getEducationObjectCount() + 1];

                    degreeName = new String[getCandidateEducationProfileStaticResponse.getDegreeObjectCount() + 1];
                    degreeId = new Long[getCandidateEducationProfileStaticResponse.getDegreeObjectCount() + 1];

                    qualificationLevel[0] = "Select Education Level";
                    qualificationId[0] = Long.valueOf(-1);
                    for(int i = 1; i<= getCandidateEducationProfileStaticResponse.getEducationObjectCount() ; i++){
                        qualificationLevel[i] = getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationName();
                        qualificationId[i] = getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationId();
                        if(getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationId() == candidateProfileActivity.candidateInfo.getCandidate().getCandidateEducation().getEducation().getEducationId()){
                            qualificationSelected = getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationId();
                            qualificationPos = i;
                            firstTimeSetting = 1;
                        }
                    }

                    degreeName[0] = "Select Highest Degree";
                    degreeId[0] = Long.valueOf(-1);
                    for(int i = 1; i<= getCandidateEducationProfileStaticResponse.getDegreeObjectCount() ; i++){
                        degreeName[i] = getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeName();
                        degreeId[i] = getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeId();
                        if(getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeId() == candidateProfileActivity.candidateInfo.getCandidate().getCandidateEducation().getDegree().getDegreeId()){
                            degreeSelected = getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeId();
                            degreePos = i;
                        }
                    }

                    adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, qualificationLevel);
                    candidateQualification.setAdapter(adapter);
                    candidateQualification.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            qualificationLayout.setBackgroundResource(0);
                            return false;
                        }
                    });

                    candidateCollege = (EditText) view.findViewById(R.id.candidate_college);

                    degreeSection = (LinearLayout) view.findViewById(R.id.degree_section);
                    degreeSection.setVisibility(View.GONE);

                    adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, degreeName);
                    candidateDegree.setAdapter(adapter);

                    candidateDegree.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            degreeLayout.setBackgroundResource(0);
                            return false;
                        }
                    });

                    //pre-filling data
                    candidateQualification.setSelection(qualificationPos);
                    candidateDegree.setSelection(degreePos);

                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateEducation().getCandidateInstitute() != null){
                        candidateCollege.setText(candidateProfileActivity.candidateInfo.getCandidate().getCandidateEducation().getCandidateInstitute());
                    }

                    qualificationStatus = candidateProfileActivity.candidateInfo.getCandidate().getCandidateEducation().getCandidateEducationCompletionStatus();
                    candidateQualification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
                        {
                            if(position != 0){
                                if(firstTimeSetting == 0){
                                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                    alertDialog.setMessage("Have you successfully completed this course?");
                                    alertDialog.setCanceledOnTouchOutside(false);
                                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    qualificationStatus = 1;
                                                    qualificationBackground = (LinearLayout) view.findViewById(R.id.educationQualificationBackground);
                                                    qualificationStatusText = (TextView) view.findViewById(R.id.spinnerQualificationStatus);

                                                    qualificationBackground.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                                                    qualificationStatusText.setText("Pass");
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    qualificationStatus = 0;
                                                    qualificationBackground = (LinearLayout) view.findViewById(R.id.educationQualificationBackground);
                                                    qualificationStatusText = (TextView) view.findViewById(R.id.spinnerQualificationStatus);

                                                    qualificationBackground.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                                                    qualificationStatusText.setText("Fail");
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();
                                } else {
                                    if(qualificationStatus == 1){
                                        qualificationBackground = (LinearLayout) view.findViewById(R.id.educationQualificationBackground);
                                        qualificationStatusText = (TextView) view.findViewById(R.id.spinnerQualificationStatus);

                                        qualificationBackground.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                                        qualificationStatusText.setText("Pass");
                                    } else if(qualificationStatus == 0){
                                        qualificationBackground = (LinearLayout) view.findViewById(R.id.educationQualificationBackground);
                                        qualificationStatusText = (TextView) view.findViewById(R.id.spinnerQualificationStatus);

                                        qualificationBackground.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                                        qualificationStatusText.setText("Fail");

                                    }
                                    firstTimeSetting = 0;
                                }
                            } else{
                                qualificationStatus = 0;
                                qualificationSelected = Long.valueOf(0);
                            }

                            if(position > 3){
                                degreeSection.setVisibility(View.VISIBLE);
                            } else {
                                degreeSection.setVisibility(View.GONE);
                            }
                            qualificationSelected = qualificationId[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {}
                    });

                    candidateDegree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
                        {
                            degreeSelected = degreeId[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {}
                    });

                    updateEducationProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean check = true;
                            if(qualificationSelected < 1){
                                showDialog("Please select your education level");
                                qualificationLayout.setBackgroundResource(R.drawable.border);
                                check = false;
                            } else if(qualificationSelected > 3 && degreeSelected < 1){
                                check = false;
                                showDialog("Please select your Degree");
                                degreeLayout.setBackgroundResource(R.drawable.border);
                            } else if(qualificationStatus == -1){
                                check = false;
                                showDialog("Please select \"have you completed this course?\"");
                            }

                            if(check){
                                UpdateCandidateEducationProfileRequest.Builder educationBuilder = UpdateCandidateEducationProfileRequest.newBuilder();
                                educationBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                                educationBuilder.setCandidateEducationLevel(qualificationSelected);
                                if(qualificationSelected > 3){
                                    educationBuilder.setCandidateDegree(degreeSelected);
                                }
                                educationBuilder.setCandidateEducationCompletionStatus(qualificationStatus);
                                educationBuilder.setCandidateEducationInstitute(candidateCollege.getText().toString());

                                UpdateEducationAsyncTask = new UpdateEducationProfileAsyncTask();
                                UpdateEducationAsyncTask.execute(educationBuilder.build());
                            }
                        }
                    });
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                            Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        }
    }

    public void showDialog(String msg){
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
        alertDialog.setMessage(msg);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}

