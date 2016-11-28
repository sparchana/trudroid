package in.trujobs.dev.trudroid.prescreen;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.GenericResponse;
import in.trujobs.proto.GetCandidateExperienceProfileStaticResponse;
import in.trujobs.proto.JobRoleObject;
import in.trujobs.proto.JobRoleResponse;
import in.trujobs.proto.UpdateCandidateExperienceRequest;

public class PreScreenExperience extends Fragment{
    LinearLayout qualificationLayout, experiencedSection, fresherExperienceLayout, isEmployedLayout;
    Integer isCandidateExperienced = -1;
    ProgressDialog pd;
    public PreScreenActivity preScreenActivity;
    TextView selectExp;
    private AsyncTask<Void, Void, GetCandidateExperienceProfileStaticResponse> mAsyncTask;
    View view;
    EditText currentJobRole, lastWithdrawnSalary, currentCompany;
    Button saveExperienceBtn, isExperienced, isFresher, isEmployedYes, isEmployedNo;
    ImageView experiencePicker, currentJobRolePicker;
    Integer expInYears = 0;
    Integer isEmployed = -1;
    JobRoleObject currentJobRoleValue;
    String selectedExpValue = "";
    final List<String> jobRoleList = new ArrayList<>();
    final List<Long> jobRoleIdList = new ArrayList<Long>();
    private AsyncTask<UpdateCandidateExperienceRequest, Void, GenericResponse> mUpdateExperienceAsyncTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        view = inflater.inflate(R.layout.pre_screen_experience, container, false);


        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

        ((PreScreenActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // track screen view
        ((PreScreenActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_EDIT_EXPERIENCE_PRESCREEN);

        fresherExperienceLayout = (LinearLayout) view.findViewById(R.id.fresher_experienced_layout);
        isEmployedLayout = (LinearLayout) view.findViewById(R.id.is_employed_layout);

        pd = CustomProgressDialog.get(getActivity());

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Experience Details");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        selectExp = (TextView) view.findViewById(R.id.select_experience);

        preScreenActivity = (PreScreenActivity) getActivity();

        JobRoleAsyncTask fetchAllJobs = new JobRoleAsyncTask();
        fetchAllJobs.execute();

//        try {
//            preScreenExperienceObject = PreScreenExperienceObject.parseFrom(bundle.getByteArray("experience"));
//
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
        return view;
    }


//
//    private class GetExperienceStaticAsyncTask extends AsyncTask<Void,
//            Void, GetCandidateExperienceProfileStaticResponse> {
//        protected void onPreExecute() {
//            super.onPreExecute();
//            pd.show();
//        }
//
//        @Override
//        protected GetCandidateExperienceProfileStaticResponse doInBackground(Void... params) {
//            return HttpRequest.getCandidateExperienceProfileStatic();
//        }
//
//        @Override
//        protected void onPostExecute(final GetCandidateExperienceProfileStaticResponse getCandidateExperienceProfileStaticResponse) {
//            super.onPostExecute(getCandidateExperienceProfileStaticResponse);
//            pd.cancel();
//
//            if(!Util.isConnectedToInternet(getContext())) {
//                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
//            } else if (getCandidateExperienceProfileStaticResponse == null) {
//                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
//                        Toast.LENGTH_LONG).show();
//                Log.w("", "Null Response");
//                return;
//            } else {
//                if(getCandidateExperienceProfileStaticResponse.getStatusValue() == ServerConstants.SUCCESS){
//
//
//                } else{
//                    Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
//                            Toast.LENGTH_LONG).show();
//                    getActivity().getSupportFragmentManager().popBackStack();
//                }
//            }
//        }
//    }

    public void showExperiencePicker(){
        final Dialog expDialog = new Dialog(getActivity());
        expDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        expDialog.setContentView(R.layout.experience_picker);
        Button setBtn = (Button) expDialog.findViewById(R.id.setBtn);

        final NumberPicker numberPickerYear = (NumberPicker) expDialog.findViewById(R.id.numberPickerYears);
        final NumberPicker numberPickerMonth = (NumberPicker) expDialog.findViewById(R.id.numberPickerMonths);

        numberPickerYear.setMaxValue(35);
        numberPickerYear.setMinValue(0);
        numberPickerYear.setWrapSelectorWheel(true);

        numberPickerMonth.setMaxValue(11);
        numberPickerMonth.setMinValue(0);
        numberPickerMonth.setWrapSelectorWheel(true);

        if(expInYears != null){
            numberPickerYear.setValue(expInYears / 12);
            numberPickerMonth.setValue(expInYears % 12);
        }

        setBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                if(numberPickerYear.getValue() == 0){
                    selectedExpValue = numberPickerMonth.getValue() + " months";
                } else if(numberPickerMonth.getValue() == 0){
                    selectedExpValue = numberPickerYear.getValue() + " years";
                } else if(numberPickerYear.getValue() != 0 && numberPickerMonth.getValue() != 0){
                    selectedExpValue = numberPickerYear.getValue() + " years - " + numberPickerMonth.getValue() + " months";
                }
                selectExp.setText(selectedExpValue);
                expInYears = (numberPickerYear.getValue() * 12) + (numberPickerMonth.getValue());
                expDialog.dismiss();
            }
        });

        expDialog.show();
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
    private class GenericTextWatcher implements TextWatcher {
        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            switch(view.getId()){
                case R.id.last_withdrawn_salary:
                    lastWithdrawnSalary.setError(null);
                    break;
                case R.id.select_experience:
                    selectExp.setError(null);
                    break;
            }
        }
    }
    private class JobRoleAsyncTask extends AsyncTask<Void,
            Void, JobRoleResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected JobRoleResponse doInBackground(Void... params) {
            return HttpRequest.getJobRoles();
        }

        @Override
        protected void onPostExecute(final JobRoleResponse jobRoleResponse) {
            super.onPostExecute(jobRoleResponse);
            pd.cancel();
            if(jobRoleResponse!= null){
                Tlog.i("fetched all jobRolesFilter successfully + " + jobRoleResponse.getJobRoleList().size());

                for(JobRoleObject jobRoleObject : jobRoleResponse.getJobRoleList()) {
                    jobRoleList.add(jobRoleObject.getJobRoleName());
                    jobRoleIdList.add(jobRoleObject.getJobRoleId());
                }

                Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
                ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

                qualificationLayout = (LinearLayout) view.findViewById(R.id.current_company_details_layout);
                experiencedSection = (LinearLayout) view.findViewById(R.id.experienced_section);

                lastWithdrawnSalary = (EditText) view.findViewById(R.id.last_withdrawn_salary);
                currentCompany = (EditText) view.findViewById(R.id.current_company);
                currentJobRole = (EditText) view.findViewById(R.id.currentJobRole);

                isExperienced = (Button) view.findViewById(R.id.is_experienced);
                isFresher = (Button) view.findViewById(R.id.is_fresher);
                isEmployedYes = (Button) view.findViewById(R.id.is_employed_yes);
                isEmployedNo = (Button) view.findViewById(R.id.is_employed_no);

                selectExp = (TextView) view.findViewById(R.id.select_experience);
                currentJobRolePicker = (ImageView) view.findViewById(R.id.current_job_role_picker);

                experiencePicker = (ImageView) view.findViewById(R.id.experience_picker);
                experiencePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showExperiencePicker();
                    }
                });

                isEmployedYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isEmployed = 1;
                        isEmployedLayout.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
                        qualificationLayout.setVisibility(View.VISIBLE);
                        isEmployedNo.setBackgroundResource(R.drawable.round_white_button);
                        isEmployedNo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isEmployedYes.setBackgroundResource(R.drawable.rounded_corner_button);
                        isEmployedYes.setTextColor(getResources().getColor(R.color.white));
                    }
                });

                isEmployedNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isEmployed = 0;
                        isEmployedLayout.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
                        qualificationLayout.setVisibility(View.GONE);
                        isEmployedYes.setBackgroundResource(R.drawable.round_white_button);
                        isEmployedYes.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isEmployedNo.setBackgroundResource(R.drawable.rounded_corner_button);
                        isEmployedNo.setTextColor(getResources().getColor(R.color.white));
                    }
                });

                isFresher.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isCandidateExperienced = 0;
                        isEmployed = 0;
                        expInYears = 0;
                        selectExp.setText("Select work experience");
                        isEmployedLayout.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
                        fresherExperienceLayout.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
                        isEmployedYes.setBackgroundResource(R.drawable.round_white_button);
                        isEmployedYes.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isEmployedNo.setBackgroundResource(R.drawable.rounded_corner_button);
                        isEmployedNo.setTextColor(getResources().getColor(R.color.white));
                        experiencedSection.setVisibility(View.GONE);
                        qualificationLayout.setVisibility(View.GONE);
                        isExperienced.setBackgroundResource(R.drawable.round_white_button);
                        isExperienced.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isFresher.setBackgroundResource(R.drawable.rounded_corner_button);
                        isFresher.setTextColor(getResources().getColor(R.color.white));
                    }
                });

                isExperienced.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isCandidateExperienced = 1;
                        isEmployed = -1;
                        isEmployedYes.setBackgroundResource(R.drawable.round_white_button);
                        isEmployedYes.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isEmployedNo.setBackgroundResource(R.drawable.round_white_button);
                        isEmployedNo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        fresherExperienceLayout.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
                        experiencedSection.setVisibility(View.VISIBLE);
                        isExperienced.setBackgroundResource(R.drawable.rounded_corner_button);
                        isExperienced.setTextColor(getResources().getColor(R.color.white));
                        isFresher.setBackgroundResource(R.drawable.round_white_button);
                        isFresher.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                });

                currentJobRole.setEnabled(false);

                if(jobRoleList == null){
                    Tlog.e("jobrole list null");
                }
                final CharSequence[] jRoleList = jobRoleList.toArray(new CharSequence[jobRoleList.size()]);


                currentJobRolePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final android.support.v7.app.AlertDialog.Builder applyDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
                        applyDialogBuilder.setCancelable(true);
                        applyDialogBuilder.setTitle("Select current Job Role");
                        applyDialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        applyDialogBuilder.setSingleChoiceItems(jRoleList, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JobRoleObject.Builder currentJobRoleBuilder = JobRoleObject.newBuilder();
                                currentJobRoleBuilder.setJobRoleName(String.valueOf(jRoleList[which]));
                                currentJobRoleBuilder.setJobRoleId(jobRoleIdList.get(which));
                                currentJobRoleValue = currentJobRoleBuilder.build();
                                currentJobRole.setText(jRoleList[which]);
                            }
                        });
                        final android.support.v7.app.AlertDialog applyDialog = applyDialogBuilder.create();
                        applyDialog.show();
                    }
                });

                saveExperienceBtn = (Button) view.findViewById(R.id.save_ps_experience_btn);
                saveExperienceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UpdateCandidateExperienceRequest.Builder experienceBuilder = UpdateCandidateExperienceRequest.newBuilder();
                        boolean check = true;

                        if(isCandidateExperienced < 0){
                            check = false;
                            showDialog("Please answer the question: Are you a fresher or an experienced candidate?");
                            fresherExperienceLayout.setBackgroundResource(R.drawable.border);
                        } else if(isCandidateExperienced == 1 && (expInYears < 1)){
                            check = false;
                            selectExp.setError("Please answer the question: Total Work Experience");
                            selectExp.addTextChangedListener(new GenericTextWatcher(lastWithdrawnSalary));
                            showDialog("Please answer the question: Total Work Experience");
                        } else if(expInYears > 1 && isEmployed < 0){
                            check = false;
                            showDialog("Please answer the question: Are you currently working?");
                            isEmployedLayout.setBackgroundResource(R.drawable.border);
                        } else if(expInYears > 1 && (lastWithdrawnSalary.getText().toString().isEmpty())){
                            check = false;
                            lastWithdrawnSalary.setError("Please provide your last drawn Salary");
                            lastWithdrawnSalary.addTextChangedListener(new GenericTextWatcher(lastWithdrawnSalary));
                            showDialog("Please provide your current Salary");
                        }

                        if(check){

                            //Track this action
                            ((PreScreenActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_EDIT_EXPERIENCE_PRESCREEN, Constants.GA_ACTION_SAVE_EXPERIENCE_PRESCREEN);

                            experienceBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                            experienceBuilder.setCandidateIsEmployed(isEmployed);
                            if(isCandidateExperienced == 1){
                                experienceBuilder.setCandidateTotalExperience(expInYears);
                                experienceBuilder.setCandidateCurrentSalary(Long.parseLong(lastWithdrawnSalary.getText().toString()));
                            } else{
                                experienceBuilder.setCandidateTotalExperience(0);
                                isEmployed = 0;
                            }
                            if(isEmployed == 1){
                                if(currentJobRoleValue != null) {
                                    experienceBuilder.setCurrentJobRole(currentJobRoleValue);
                                }
                                experienceBuilder.setCandidateCurrentCompany(currentCompany.getText().toString());
                            }

                            mUpdateExperienceAsyncTask = new UpdateExperienceAsyncTask();
                            mUpdateExperienceAsyncTask.execute(experienceBuilder.build());
                        }

                    }
                });
            }
        }
    }

    private class UpdateExperienceAsyncTask extends AsyncTask<UpdateCandidateExperienceRequest,
            Void, GenericResponse> {
        @Override
        protected GenericResponse doInBackground(UpdateCandidateExperienceRequest... params) {
            return HttpRequest.updateCandidateExperience(params[0]);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected void onPostExecute(GenericResponse genericResponse) {
            super.onPostExecute(genericResponse);
            pd.cancel();
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if(genericResponse == null){
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
            } else{
                if(genericResponse.getStatus() == GenericResponse.Status.SUCCESS){
                    PreScreenActivity.showRequiredFragment(getActivity());
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong while saving education profile. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
