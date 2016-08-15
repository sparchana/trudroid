package in.trujobs.dev.trudroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
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
    EditText lastWithdrawnSalary, currentCompany;
    TextView currentJobRole;

    public CandidateInfoActivity candidateInfoActivity;

    LinearLayout qualificationLayout, experiencedSection;
    Integer isCandidateExperienced = -1;
    ImageView addLanguage;

    // values
    Integer expInYears;
    JobRoleObject currentJobRoleValue;
    final List<LanguageKnownObject> candidateLanguageKnown = new ArrayList<LanguageKnownObject>();
    final List<CandidateSkillObject> candidateSkill = new ArrayList<CandidateSkillObject>();
    final List<LanguageObject> checkedLanguage = new ArrayList<LanguageObject>();
    Integer isEmployed;

    CharSequence[] allLanguageList = new CharSequence[0];
    List<Integer> languageIdList = new ArrayList<Integer>();

    Button saveExperienceBtn, selectExp, isExperienced, isFresher, isEmployedYes, isEmployedNo;
    int pos = -1;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.candidate_experience_profile, container, false);
        candidateInfoActivity = (CandidateInfoActivity) getActivity();
        mAsyncTask = new GetExperienceStaticAsyncTask();
        mAsyncTask.execute();
        return view;
    }

    public void showExperiencePicker(){
        selectExp = (Button) view.findViewById(R.id.select_experience);
        final Dialog expDialog = new Dialog(getActivity());
        expDialog.setTitle("Select Experience");
        expDialog.setContentView(R.layout.experience_picker);
        Button setBtn = (Button) expDialog.findViewById(R.id.setBtn);
        Button cnlBtn = (Button) expDialog.findViewById(R.id.CancelButton_NumberPicker);

        final NumberPicker numberPickerYear = (NumberPicker) expDialog.findViewById(R.id.numberPickerYears);
        final NumberPicker numberPickerMonth = (NumberPicker) expDialog.findViewById(R.id.numberPickerMonths);
        numberPickerYear.setMaxValue(35);
        numberPickerYear.setMinValue(0);
        numberPickerYear.setWrapSelectorWheel(true);

        numberPickerMonth.setMaxValue(11);
        numberPickerMonth.setMinValue(0);
        numberPickerMonth.setWrapSelectorWheel(true);

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

        cnlBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                expDialog.dismiss();
            }
        });

        expDialog.show();
    }

    private class UpdateExperienceProfileAsyncTask extends AsyncTask<UpdateCandidateExperienceProfileRequest,
            Void, UpdateCandidateBasicProfileResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getContext(),R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
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

            CandidateProfileEducation candidateProfileEducation = new CandidateProfileEducation();
            candidateProfileEducation.setArguments(getActivity().getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_profile, candidateProfileEducation).commit();
        }
    }

    private class GetExperienceStaticAsyncTask extends AsyncTask<Void,
            Void, GetCandidateExperienceProfileStaticResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getContext(),R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
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
                Toast.makeText(getContext(), "Something went wrong in fetching data",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null Response");
                return;
            } else {
                Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
                ((CandidateInfoActivity)getActivity()).setSupportActionBar(toolbar);

                addLanguage = (ImageView) view.findViewById(R.id.add_more_language);
                qualificationLayout = (LinearLayout) view.findViewById(R.id.current_company_details_layout);
                experiencedSection = (LinearLayout) view.findViewById(R.id.experienced_section);

                lastWithdrawnSalary = (EditText) view.findViewById(R.id.last_withdrawn_salary);
                currentCompany = (EditText) view.findViewById(R.id.current_company);
                currentJobRole = (TextView) view.findViewById(R.id.currentJobRole);

                isExperienced = (Button) view.findViewById(R.id.is_experienced);
                isFresher = (Button) view.findViewById(R.id.is_fresher);
                isEmployedYes = (Button) view.findViewById(R.id.is_employed_yes);
                isEmployedNo = (Button) view.findViewById(R.id.is_employed_no);
                selectExp = (Button) view.findViewById(R.id.select_experience);

                lastWithdrawnSalary.setText(candidateInfoActivity.candidateInfo.getCandidate().getCandidateLastWithdrawnSalary() + "");
                currentCompany.setText(candidateInfoActivity.candidateInfo.getCandidate().getCandidateCurrentCompany() + "");
                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateCurrentJobRole() != null){
                    currentJobRole.setText(candidateInfoActivity.candidateInfo.getCandidate().getCandidateCurrentJobRole().getJobRoleName());
                    currentJobRoleValue = candidateInfoActivity.candidateInfo.getCandidate().getCandidateCurrentJobRole();
                }

                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateTotalExperience() > 0){
                    isCandidateExperienced = 1;
                    experiencedSection.setVisibility(View.VISIBLE);
                    isExperienced.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    isExperienced.setTextColor(getResources().getColor(R.color.white));
                    isFresher.setBackgroundColor(getResources().getColor(R.color.white));
                    isFresher.setTextColor(getResources().getColor(R.color.colorPrimary));
                    expInYears = candidateInfoActivity.candidateInfo.getCandidate().getCandidateTotalExperience();
                    Integer year = expInYears / 12;
                    Integer month = expInYears % 12;
                    if(year == 0){
                        selectExp.setText(month + " months");
                    } else if(month == 0){
                        selectExp.setText(year + " years");
                    } else if(year != 0 && month != 0){
                        selectExp.setText(year + " years " + month + " months");
                    }
                } else {
                    isCandidateExperienced = 0;
                    experiencedSection.setVisibility(View.GONE);
                }

                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateIsEmployed() == 1){
                    isEmployed = 1;
                    qualificationLayout.setVisibility(View.VISIBLE);
                    isEmployedNo.setBackgroundColor(getResources().getColor(R.color.white));
                    isEmployedNo.setTextColor(getResources().getColor(R.color.colorPrimary));
                    isEmployedYes.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    isEmployedYes.setTextColor(getResources().getColor(R.color.white));
                } else if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateIsEmployed() == 0){
                    isEmployed = 0;
                    qualificationLayout.setVisibility(View.GONE);
                    isEmployedYes.setBackgroundColor(getResources().getColor(R.color.white));
                    isEmployedYes.setTextColor(getResources().getColor(R.color.colorPrimary));
                    isEmployedNo.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    isEmployedNo.setTextColor(getResources().getColor(R.color.white));
                }
                isEmployedYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isEmployed = 1;
                        qualificationLayout.setVisibility(View.VISIBLE);
                        isEmployedNo.setBackgroundColor(getResources().getColor(R.color.white));
                        isEmployedNo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isEmployedYes.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
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
                        experiencedSection.setVisibility(View.GONE);
                        qualificationLayout.setVisibility(View.GONE);
                        isExperienced.setBackgroundColor(getResources().getColor(R.color.white));
                        isExperienced.setTextColor(getResources().getColor(R.color.colorPrimary));
                        isFresher.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        isFresher.setTextColor(getResources().getColor(R.color.white));
                    }
                });

                isExperienced.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isCandidateExperienced = 1;
                        experiencedSection.setVisibility(View.VISIBLE);
                        isExperienced.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        isExperienced.setTextColor(getResources().getColor(R.color.white));
                        isFresher.setBackgroundColor(getResources().getColor(R.color.white));
                        isFresher.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                });

                selectExp = (Button) view.findViewById(R.id.select_experience);
                selectExp.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View arg0) {
                        showExperiencePicker();
                    }
                });

                getAllLanguages(getCandidateExperienceProfileStaticResponse.getLanguageObjectList(), 0);

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
                addLanguage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        //addding first 3 as checked
                        for(int x=0; x<3 ; x++){
                            LanguageObject.Builder languageBuilder = LanguageObject.newBuilder();
                            languageBuilder.setLanguageId(languageIdList.get(x));
                            languageBuilder.setLanguageName(String.valueOf(allLanguageList[x]));
                            checkedLanguage.add(languageBuilder.build());
                        }

                        final AlertDialog alertDialog = new AlertDialog.Builder(
                                getContext())
                                .setCancelable(false)
                                .setTitle("All Languages")
                                .setPositiveButton("Done",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                getAllLanguages(checkedLanguage, 1);
                                                dialog.dismiss();
                                            }
                                        })
                                .setMultiChoiceItems(allLanguageList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                        if (b) {
                                            // If the user checked the item, add it to the selected items
                                            LanguageObject.Builder languageBuilder = LanguageObject.newBuilder();
                                            languageBuilder.setLanguageId(languageIdList.get(i));
                                            languageBuilder.setLanguageName(String.valueOf(allLanguageList[i]));
                                            checkedLanguage.add(languageBuilder.build());
                                        } else{
                                            boolean flag = false;
                                            for(int x=0; x<checkedLanguage.size(); x++){
                                                if(checkedLanguage.get(x).getLanguageId() == languageIdList.get(x)){
                                                    flag = true;
                                                    pos = i;
                                                    break;
                                                }
                                            }
                                            if(flag){
                                                checkedLanguage.remove(pos);
                                            }
                                        }
                                    }
                                }).create();
                        alertDialog.show();

                    }
                });

                currentJobRole.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final CharSequence[] jobRoleList = new CharSequence[candidateInfoActivity.candidateInfo.getJobRolesCount()];
                        final List<Long> jobRoleIdList = new ArrayList<Long>();
                        for (int i = 0; i < candidateInfoActivity.candidateInfo.getJobRolesCount(); i++) {
                            jobRoleList[i] = candidateInfoActivity.candidateInfo.getJobRoles(i).getJobRoleName();
                            jobRoleIdList.add(candidateInfoActivity.candidateInfo.getJobRoles(i).getJobRoleId());
                        }
                        final AlertDialog alertDialog = new AlertDialog.Builder(
                                getContext())
                                .setCancelable(true)
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
                    }
                });

                saveExperienceBtn = (Button) view.findViewById(R.id.save_experience_btn);
                saveExperienceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UpdateCandidateExperienceProfileRequest.Builder experienceBuilder = UpdateCandidateExperienceProfileRequest.newBuilder();
                        boolean check = true;

                        if(isCandidateExperienced == -1){
                            check = false;
                            System.out.println("Please select Fresher or exp  =-=-=-=-=-=-");
                        }
                        if(isEmployed == null){
                            check = false;
                            System.out.println("Please select are you employed?  =-=-=-=-=-=-");
                        } else if(isEmployed == 1 && (lastWithdrawnSalary.getText().toString().length() == 0)){
                            check = false;
                            System.out.println("Please select salary if you are currently employed?  =-=-=-=-=-=-");
                        } else if(isCandidateExperienced == 1 && (expInYears == null || expInYears == 0)){
                            check = false;
                            System.out.println("Please select years of experience?  =-=-=-=-=-=-");
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

                    for(CandidateSkillObject skill : candidateInfoActivity.candidateInfo.getCandidate().getCandidateSkillObjectList()){
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

    public void getAllLanguages(List<LanguageObject> languageObjectList, int val){
        int count = 0, breakFlag = 1;
        LinearLayout languageListView = (LinearLayout) view.findViewById(R.id.language_list_view);
        if(languageListView.getChildCount() > 0){
            languageListView.removeAllViews();
        }
        for(final LanguageObject languageObject : languageObjectList){
            LayoutInflater inflater = null;
            inflater = (LayoutInflater) getActivity().getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View mLinearView = inflater.inflate(R.layout.language_list_item, null);
            TextView languageName = (TextView) mLinearView
                    .findViewById(R.id.language_name);

            languageName.setText(languageObject.getLanguageName());
            languageListView.addView(mLinearView);

            final CheckBox languageReadWrite = (CheckBox) mLinearView.findViewById(R.id.lang_read_write);
            final CheckBox languageUnderstand = (CheckBox) mLinearView.findViewById(R.id.lang_understand);
            final CheckBox languageSpeak = (CheckBox) mLinearView.findViewById(R.id.lang_speak);

            for(LanguageKnownObject language : candidateInfoActivity.candidateInfo.getCandidate().getLanguageKnownObjectList()){
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
            count++;
            if(val == 0){
                if(count == 3){
                    breakFlag = 0;
                }
            }
            if(breakFlag == 0){
                break;
            }

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
}
