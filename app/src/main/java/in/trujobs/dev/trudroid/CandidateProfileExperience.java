package in.trujobs.dev.trudroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.CandidateSkillObject;
import in.trujobs.proto.GetCandidateExperienceProfileStaticResponse;
import in.trujobs.proto.JobRoleObject;
import in.trujobs.proto.LanguageKnownObject;
import in.trujobs.proto.LanguageObject;
import in.trujobs.proto.SkillObject;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;
import in.trujobs.proto.UpdateCandidateExperienceProfileRequest;

/**
 * Created by batcoder1 on 9/8/16.
 */
public class CandidateProfileExperience extends Fragment {

    private AsyncTask<UpdateCandidateExperienceProfileRequest, Void, UpdateCandidateBasicProfileResponse> mUpdateExperienceAsyncTask;
    private AsyncTask<Void, Void, GetCandidateExperienceProfileStaticResponse> mAsyncTask;
    ProgressDialog pd;
    String selectedExpValue = "";
    CandidateSkillObject.Builder existingSkill = CandidateSkillObject.newBuilder();
    LanguageKnownObject.Builder existingLanguageKnown = LanguageKnownObject.newBuilder();
    EditText currentJobRole, lastWithdrawnSalary, currentCompany;

    public CandidateProfileActivity candidateProfileActivity;

    LinearLayout qualificationLayout, experiencedSection, languageListView;
    Integer isCandidateExperienced = -1;

    // values
    Integer expInYears = 0;
    JobRoleObject currentJobRoleValue;
    final List<LanguageKnownObject> candidateLanguageKnown = new ArrayList<LanguageKnownObject>();
    final List<CandidateSkillObject> candidateSkill = new ArrayList<CandidateSkillObject>();
    Integer isEmployed = -1;

    CharSequence[] allLanguageList = new CharSequence[0];
    List<Integer> languageIdList = new ArrayList<Integer>();

    Button saveExperienceBtn, isExperienced, isFresher, isEmployedYes, isEmployedNo;
    int pos = -1;
    TextView selectExp;

    ImageView experiencePicker, currentJobRolePicker;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.candidate_experience_profile, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateProfileActivity)getActivity()).setSupportActionBar(toolbar);

        ((CandidateProfileActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = CustomProgressDialog.get(getActivity());

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Experience Profile");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        candidateProfileActivity = (CandidateProfileActivity) getActivity();
        mAsyncTask = new GetExperienceStaticAsyncTask();
        mAsyncTask.execute();
        return view;
    }

    public void showExperiencePicker(){
        final Dialog expDialog = new Dialog(getActivity());
        expDialog.setTitle("Select Experience");
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

    private class UpdateExperienceProfileAsyncTask extends AsyncTask<UpdateCandidateExperienceProfileRequest,
            Void, UpdateCandidateBasicProfileResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected UpdateCandidateBasicProfileResponse doInBackground(UpdateCandidateExperienceProfileRequest ... params) {
            return HttpRequest.updateCandidateExperienceProfile(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateCandidateBasicProfileResponse updateCandidateBasicProfileResponse) {
            super.onPostExecute(updateCandidateBasicProfileResponse);
            pd.cancel();

            if (updateCandidateBasicProfileResponse == null) {
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
            } else{
                if(updateCandidateBasicProfileResponse.getStatusValue() == ServerConstants.SUCCESS){
                    CandidateProfileEducation candidateProfileEducation = new CandidateProfileEducation();
                    candidateProfileEducation.setArguments(getActivity().getIntent().getExtras());
                    getFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .add(R.id.main_profile, candidateProfileEducation).commit();
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong while saving experience profile. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class GetExperienceStaticAsyncTask extends AsyncTask<Void,
            Void, GetCandidateExperienceProfileStaticResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected GetCandidateExperienceProfileStaticResponse doInBackground(Void... params) {
            return HttpRequest.getCandidateExperienceProfileStatic();
        }

        @Override
        protected void onPostExecute(final GetCandidateExperienceProfileStaticResponse getCandidateExperienceProfileStaticResponse) {
            super.onPostExecute(getCandidateExperienceProfileStaticResponse);
            pd.cancel();

            if (getCandidateExperienceProfileStaticResponse == null) {
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null Response");
                return;
            } else {
                if(getCandidateExperienceProfileStaticResponse.getStatusValue() == ServerConstants.SUCCESS){
                    Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
                    ((CandidateProfileActivity)getActivity()).setSupportActionBar(toolbar);

                    languageListView = (LinearLayout) view.findViewById(R.id.language_list_view);

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

                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateLastWithdrawnSalary() > 0){
                        lastWithdrawnSalary.setText(candidateProfileActivity.candidateInfo.getCandidate().getCandidateLastWithdrawnSalary() + "");
                    }
                    currentCompany.setText(candidateProfileActivity.candidateInfo.getCandidate().getCandidateCurrentCompany() + "");
                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateCurrentJobRole() != null){
                        currentJobRole.setText(candidateProfileActivity.candidateInfo.getCandidate().getCandidateCurrentJobRole().getJobRoleName());
                        currentJobRoleValue = candidateProfileActivity.candidateInfo.getCandidate().getCandidateCurrentJobRole();
                    }

                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateTotalExperience() > 0){
                        isCandidateExperienced = 1;
                        experiencedSection.setVisibility(View.VISIBLE);
                        isExperienced.setBackgroundResource(R.drawable.rounded_corner_button);
                        isExperienced.setTextColor(getResources().getColor(R.color.white));
                        isFresher.setBackgroundResource(R.drawable.round_white_button);
                        isFresher.setTextColor(getResources().getColor(R.color.colorPrimary));
                        expInYears = candidateProfileActivity.candidateInfo.getCandidate().getCandidateTotalExperience();
                        Integer year = expInYears / 12;
                        Integer month = expInYears % 12;
                        if(year == 0){
                            selectExp.setText(month + " months");
                        } else if(month == 0){
                            selectExp.setText(year + " years");
                        } else if(year != 0 && month != 0){
                            selectExp.setText(year + " years " + month + " months");
                        }
                    } else if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateTotalExperience() == 0){
                        isCandidateExperienced = 0;
                        experiencedSection.setVisibility(View.GONE);
                        isFresher.setBackgroundResource(R.drawable.rounded_corner_button);
                        isFresher.setTextColor(getResources().getColor(R.color.white));
                        isExperienced.setBackgroundResource(R.drawable.round_white_button);
                        isExperienced.setTextColor(getResources().getColor(R.color.colorPrimary));
                        expInYears = candidateProfileActivity.candidateInfo.getCandidate().getCandidateTotalExperience();
                        qualificationLayout.setVisibility(View.GONE);
                    }

                    selectExp = (TextView) view.findViewById(R.id.select_experience);
                    currentJobRolePicker = (ImageView) view.findViewById(R.id.current_job_role_picker);

                    experiencePicker = (ImageView) view.findViewById(R.id.experience_picker);
                    experiencePicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showExperiencePicker();
                        }
                    });

                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateIsEmployed() == 1){
                        isEmployed = 1;
                        qualificationLayout.setVisibility(View.VISIBLE);
                        isEmployedNo.setBackgroundResource(R.drawable.round_white_button);
                        isEmployedNo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isEmployedYes.setBackgroundResource(R.drawable.rounded_corner_button);
                        isEmployedYes.setTextColor(getResources().getColor(R.color.white));
                    } else if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateIsEmployed() == 0){
                        isEmployed = 0;
                        qualificationLayout.setVisibility(View.GONE);
                        isEmployedYes.setBackgroundResource(R.drawable.round_white_button);
                        isEmployedYes.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isEmployedNo.setBackgroundResource(R.drawable.rounded_corner_button);
                        isEmployedNo.setTextColor(getResources().getColor(R.color.white));
                    }
                    isEmployedYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isEmployed = 1;
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
                            qualificationLayout.setVisibility(View.GONE);
                            isEmployedYes.setBackgroundColor(getResources().getColor(R.color.white));
                            isEmployedYes.setTextColor(getResources().getColor(R.color.colorPrimary));
                            isEmployedNo.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            isEmployedNo.setTextColor(getResources().getColor(R.color.white));
                        }
                    });

                    isFresher.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isCandidateExperienced = 0;
                            isEmployed = 0;
                            isEmployedYes.setBackgroundResource(R.drawable.round_white_button);
                            isEmployedYes.setTextColor(getResources().getColor(R.color.colorPrimary));
                            isEmployedNo.setBackgroundResource(R.drawable.round_white_button);
                            isEmployedNo.setTextColor(getResources().getColor(R.color.colorPrimary));
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
                            experiencedSection.setVisibility(View.VISIBLE);
                            isExperienced.setBackgroundResource(R.drawable.rounded_corner_button);
                            isExperienced.setTextColor(getResources().getColor(R.color.white));
                            isFresher.setBackgroundResource(R.drawable.round_white_button);
                            isFresher.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                    });

                    getAllLanguages(getCandidateExperienceProfileStaticResponse.getLanguageObjectList());

                    allLanguageList = new CharSequence[getCandidateExperienceProfileStaticResponse.getLanguageObjectCount()];
                    languageIdList = new ArrayList<Integer>();

                    for(int i=0 ; i<getCandidateExperienceProfileStaticResponse.getLanguageObjectCount() ; i++){
                        allLanguageList[i] = getCandidateExperienceProfileStaticResponse.getLanguageObjectList().get(i).getLanguageName();
                        languageIdList.add(getCandidateExperienceProfileStaticResponse.getLanguageObjectList().get(i).getLanguageId());
                    }

                    final boolean[] checkedItems = new boolean[getCandidateExperienceProfileStaticResponse.getLanguageObjectCount()];
                    for(int i=0 ; i<3 ; i++){
                        checkedItems[i] = true;
                    }

                    final CharSequence[] jobRoleList = new CharSequence[candidateProfileActivity.candidateInfo.getJobRolesCount()];
                    final List<Long> jobRoleIdList = new ArrayList<Long>();
                    for (int i = 0; i < candidateProfileActivity.candidateInfo.getJobRolesCount(); i++) {
                        jobRoleList[i] = candidateProfileActivity.candidateInfo.getJobRoles(i).getJobRoleName();
                        jobRoleIdList.add(candidateProfileActivity.candidateInfo.getJobRoles(i).getJobRoleId());
                    }

                    currentJobRole.setEnabled(false);

                    currentJobRolePicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final AlertDialog alertDialog = new AlertDialog.Builder(
                                    getContext())
                                    .setCancelable(true)
                                    .setPositiveButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .setTitle("Select current Job Role")
                                    .setSingleChoiceItems(jobRoleList, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            JobRoleObject.Builder currentJobRoleBuilder = JobRoleObject.newBuilder();
                                            currentJobRoleBuilder.setJobRoleName(String.valueOf(jobRoleList[which]));
                                            currentJobRoleBuilder.setJobRoleId(jobRoleIdList.get(which));
                                            currentJobRoleValue = currentJobRoleBuilder.build();
                                            currentJobRole.setText(jobRoleList[which]);
                                            dialog.dismiss();
                                        }
                                    }).create();
                            alertDialog.show();
                            WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
                            params.gravity = Gravity.CENTER|Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
                            params.height = 900;
                            alertDialog.getWindow().setAttributes(params);
                        }
                    });

                    saveExperienceBtn = (Button) view.findViewById(R.id.save_experience_btn);
                    saveExperienceBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UpdateCandidateExperienceProfileRequest.Builder experienceBuilder = UpdateCandidateExperienceProfileRequest.newBuilder();
                            boolean check = true;

                            if(isCandidateExperienced < 0){
                                check = false;
                                showDialog("Please select Fresher or Experience");
                            }
                            if(expInYears > 1 && isEmployed < 0){
                                check = false;
                                showDialog("Please select are you employed?");
                            } else if(isEmployed == 1 && (lastWithdrawnSalary.getText().toString().isEmpty())){
                                check = false;
                                showDialog("Please provide your current Salary");
                            } else if(isCandidateExperienced == 1 && (expInYears < 1)){
                                check = false;
                                showDialog("Please select total years of experience?");
                            }

                            if(check){
                                experienceBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                                experienceBuilder.addAllCandidateLanguage(candidateLanguageKnown);
                                experienceBuilder.addAllCandidateSkill(candidateSkill);
                                experienceBuilder.setCandidateIsEmployed(isEmployed);
                                if(isCandidateExperienced == 1){
                                    experienceBuilder.setCandidateTotalExperience(expInYears);
                                } else{
                                    experienceBuilder.setCandidateTotalExperience(0);
                                    isEmployed = 0;
                                }
                                if(isEmployed == 1){
                                    experienceBuilder.setCurrentJobRole(currentJobRoleValue);
                                    experienceBuilder.setCandidateCurrentCompany(currentCompany.getText().toString());
                                    experienceBuilder.setCandidateCurrentSalary(Long.parseLong(lastWithdrawnSalary.getText().toString()));
                                }

                                mUpdateExperienceAsyncTask = new UpdateExperienceProfileAsyncTask();
                                mUpdateExperienceAsyncTask.execute(experienceBuilder.build());
                            }
                        }
                    });

                    LinearLayout skillListView = (LinearLayout) view.findViewById(R.id.skill_list_view);

                    for(final SkillObject skillObject : getCandidateExperienceProfileStaticResponse.getSkillObjectList()){
                        LayoutInflater inflater = null;
                        inflater = (LayoutInflater) getActivity().getApplicationContext()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View mLinearView = inflater.inflate(R.layout.skill_list_view, null);
                        TextView skillName = (TextView) mLinearView
                                .findViewById(R.id.skill_name);
                        skillName.setText(skillObject.getSkillName());
                        skillListView.addView(mLinearView);

                        final CheckBox skillCheckbox = (CheckBox) mLinearView.findViewById(R.id.skill_checkbox);

                        for(CandidateSkillObject skill : candidateProfileActivity.candidateInfo.getCandidate().getCandidateSkillObjectList()){
                            if(skill.getSkillId() == skillObject.getSkillId()){
                                if(skill.getAnswer() == true){
                                    skillCheckbox.setChecked(true);
                                    CandidateSkillObject.Builder skillBuilder = CandidateSkillObject.newBuilder();
                                    skillBuilder.setSkillId(skillObject.getSkillId());
                                    skillBuilder.setAnswer(true);
                                    candidateSkill.add(skillBuilder.build());
                                }
                                break;
                            }
                        }

                        skillCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                boolean flag = false;
                                flag = findExistingSkillObject(skillObject);
                                CandidateSkillObject.Builder skill = CandidateSkillObject.newBuilder();
                                if(skillCheckbox.isChecked()){
                                    if(flag){
                                        skill.setSkillId(existingSkill.getSkillId());
                                        skill.setAnswer(true);
                                        candidateSkill.remove(pos);
                                        candidateSkill.add(skill.build());
                                    } else{
                                        skill.setSkillId(skillObject.getSkillId());
                                        skill.setAnswer(true);
                                        candidateSkill.add(skill.build());
                                    }
                                } else{
                                    skill.setSkillId(existingSkill.getSkillId());
                                    skill.setAnswer(false);
                                    candidateSkill.remove(pos);
                                    candidateSkill.add(skill.build());
                                }
                            }
                        });
                    }
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                            Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        }
    }

    public boolean findExistingLanguageKnownObject(LanguageObject languageObject){
        boolean flag = false;
        for(int i=0; i<candidateLanguageKnown.size(); i++){
            if(candidateLanguageKnown.get(i).getLanguageKnownId() == languageObject.getLanguageId()){
                flag = true;
                pos = i;
                existingLanguageKnown = candidateLanguageKnown.get(i).toBuilder();
                break;
            }
        }
        return flag;
    }

    public void getAllLanguages(List<LanguageObject> languageObjectList){
        for(final LanguageObject languageObject : languageObjectList){
            LayoutInflater inflater = null;
            inflater = (LayoutInflater) getActivity().getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View mLinearView = inflater.inflate(R.layout.language_list_item, null);
            TextView languageName = (TextView) mLinearView
                    .findViewById(R.id.language_name);

            languageName.setText(languageObject.getLanguageName());
            languageListView.addView(mLinearView);

            final CheckBox languageReadWrite = (CheckBox) mLinearView.findViewById(R.id.lang_read_write);
            final CheckBox languageUnderstand = (CheckBox) mLinearView.findViewById(R.id.lang_understand);
            final CheckBox languageSpeak = (CheckBox) mLinearView.findViewById(R.id.lang_speak);

            for(LanguageKnownObject language : candidateProfileActivity.candidateInfo.getCandidate().getLanguageKnownObjectList()){
                if(language.getLanguageKnownId() == languageObject.getLanguageId()){
                    LanguageKnownObject.Builder languageBuilder = LanguageKnownObject.newBuilder();
                    languageBuilder.setLanguageKnownId(languageObject.getLanguageId());
                    if(language.getLanguageReadWrite() == 1){
                        languageReadWrite.setChecked(true);
                        languageBuilder.setLanguageReadWrite(1);
                    } else {
                        languageBuilder.setLanguageReadWrite(0);
                    }
                    if(language.getLanguageUnderstand() == 1){
                        languageUnderstand.setChecked(true);
                        languageBuilder.setLanguageUnderstand(1);
                    } else{
                        languageBuilder.setLanguageUnderstand(0);
                    }
                    if(language.getLanguageSpeak() == 1){
                        languageSpeak.setChecked(true);
                        languageBuilder.setLanguageSpeak(1);
                    } else {
                        languageBuilder.setLanguageSpeak(0);
                    }
                    candidateLanguageKnown.add(languageBuilder.build());
                    break;
                }
            }

            languageReadWrite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    boolean flag = false;
                    //check if this is there in list or not
                    flag = findExistingLanguageKnownObject(languageObject);
                    LanguageKnownObject.Builder language = LanguageKnownObject.newBuilder();
                    if(languageReadWrite.isChecked()){
                        if(flag){
                            language.setLanguageKnownId(languageObject.getLanguageId());
                            language.setLanguageReadWrite(1);
                            language.setLanguageUnderstand(existingLanguageKnown.getLanguageUnderstand());
                            language.setLanguageSpeak(existingLanguageKnown.getLanguageSpeak());
                            candidateLanguageKnown.remove(pos);
                            candidateLanguageKnown.add(language.build());
                            pos = -1;
                        } else{
                            language.setLanguageKnownId(languageObject.getLanguageId());
                            language.setLanguageReadWrite(1);
                            language.setLanguageUnderstand(0);
                            language.setLanguageSpeak(0);
                            candidateLanguageKnown.add(language.build());
                        }
                    } else{
                        language.setLanguageKnownId(languageObject.getLanguageId());
                        language.setLanguageReadWrite(0);
                        language.setLanguageUnderstand(existingLanguageKnown.getLanguageUnderstand());
                        language.setLanguageSpeak(existingLanguageKnown.getLanguageSpeak());
                        candidateLanguageKnown.add(pos, language.build());
                    }
                }
            });

            languageUnderstand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    boolean flag = false;
                    //check if this is there in list or not
                    flag = findExistingLanguageKnownObject(languageObject);
                    LanguageKnownObject.Builder language = LanguageKnownObject.newBuilder();
                    if(languageUnderstand.isChecked()){
                        if(flag){
                            language.setLanguageKnownId(languageObject.getLanguageId());
                            language.setLanguageReadWrite(existingLanguageKnown.getLanguageReadWrite());
                            language.setLanguageUnderstand(1);
                            language.setLanguageSpeak(existingLanguageKnown.getLanguageSpeak());
                            candidateLanguageKnown.remove(pos);
                            candidateLanguageKnown.add(language.build());
                        } else{
                            language.setLanguageKnownId(languageObject.getLanguageId());
                            language.setLanguageReadWrite(0);
                            language.setLanguageUnderstand(1);
                            language.setLanguageSpeak(0);
                            candidateLanguageKnown.add(language.build());
                        }
                    } else{
                        language.setLanguageKnownId(languageObject.getLanguageId());
                        language.setLanguageReadWrite(existingLanguageKnown.getLanguageReadWrite());
                        language.setLanguageUnderstand(0);
                        language.setLanguageSpeak(existingLanguageKnown.getLanguageSpeak());
                        candidateLanguageKnown.add(pos, language.build());
                    }
                }
            });

            languageSpeak.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    boolean flag = false;
                    //check if this is there in list or not
                    flag = findExistingLanguageKnownObject(languageObject);
                    LanguageKnownObject.Builder language = LanguageKnownObject.newBuilder();

                    if(languageSpeak.isChecked()){
                        if(flag){
                            language.setLanguageKnownId(languageObject.getLanguageId());
                            language.setLanguageReadWrite(existingLanguageKnown.getLanguageReadWrite());
                            language.setLanguageUnderstand(existingLanguageKnown.getLanguageUnderstand());
                            language.setLanguageSpeak(1);
                            candidateLanguageKnown.remove(pos);
                            candidateLanguageKnown.add(language.build());
                        } else{
                            language.setLanguageKnownId(languageObject.getLanguageId());
                            language.setLanguageReadWrite(0);
                            language.setLanguageUnderstand(0);
                            language.setLanguageSpeak(1);
                            candidateLanguageKnown.add(language.build());
                        }
                    } else{
                        language.setLanguageKnownId(languageObject.getLanguageId());
                        language.setLanguageReadWrite(existingLanguageKnown.getLanguageReadWrite());
                        language.setLanguageUnderstand(existingLanguageKnown.getLanguageUnderstand());
                        language.setLanguageSpeak(0);
                        candidateLanguageKnown.add(pos, language.build());
                    }
                }
            });
        }
    }

    public boolean findExistingSkillObject(SkillObject skillObject){
        boolean flag = false;
        for(int i=0; i<candidateSkill.size(); i++){
            if(candidateSkill.get(i).getSkillId() == skillObject.getSkillId()){
                flag = true;
                pos = i;
                existingSkill = candidateSkill.get(i).toBuilder();
                break;
            }
        }
        return flag;
    }

    public void showDialog(String msg){
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
        alertDialog.setMessage(msg);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
