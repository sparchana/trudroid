package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.CandidateSkillObject;
import in.trujobs.proto.GetCandidateExperienceProfileStaticResponse;
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
    LanguageKnownObject.Builder existingLanguageKnown = LanguageKnownObject.newBuilder();
    CandidateSkillObject.Builder existingSkill = CandidateSkillObject.newBuilder();

    final List<LanguageKnownObject> candidateLanguageKnown = new ArrayList<LanguageKnownObject>();
    final List<CandidateSkillObject> candidateSkill = new ArrayList<CandidateSkillObject>();
    int pos = -1;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.candidate_experience_profile, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateInfoActivity)getActivity()).setSupportActionBar(toolbar);

        mAsyncTask = new GetExperienceStaticAsyncTask();
        mAsyncTask.execute();

        Button saveExperienceBtn = (Button) view.findViewById(R.id.save_experience_btn);
        saveExperienceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Clicked",
                        Toast.LENGTH_LONG).show();
                UpdateCandidateExperienceProfileRequest.Builder experienceBuilder = UpdateCandidateExperienceProfileRequest.newBuilder();
                experienceBuilder.addAllCandidateLanguage(candidateLanguageKnown);
                experienceBuilder.addAllCandidateSkill(candidateSkill);
                experienceBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                experienceBuilder.setCandidateTotalExperience(12);
                experienceBuilder.setCandidateCurrentCompany("Glasswing");
                experienceBuilder.setCandidateCurrentSalary(22000);

                mUpdateExperienceAsyncTask = new UpdateExperienceProfileAsyncTask();
                mUpdateExperienceAsyncTask.execute(experienceBuilder.build());
            }
        });
        return view;
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
        protected void onPostExecute(GetCandidateExperienceProfileStaticResponse getCandidateExperienceProfileStaticResponse) {
            super.onPostExecute(getCandidateExperienceProfileStaticResponse);
            pd.cancel();

            if (getCandidateExperienceProfileStaticResponse == null) {
                Toast.makeText(getContext(), "Something went wrong in fetching data",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null Response");
                return;
            } else {
                LinearLayout languageListView = (LinearLayout) view.findViewById(R.id.language_list_view);
                LinearLayout skillListView = (LinearLayout) view.findViewById(R.id.skill_list_view);
                for(final LanguageObject languageObject : getCandidateExperienceProfileStaticResponse.getLanguageObjectList()){
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
