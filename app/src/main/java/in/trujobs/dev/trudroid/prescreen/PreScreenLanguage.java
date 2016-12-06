package in.trujobs.dev.trudroid.prescreen;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.GenericResponse;
import in.trujobs.proto.LanguageKnownObject;
import in.trujobs.proto.LanguageObject;
import in.trujobs.proto.PreScreenLanguageObject;
import in.trujobs.proto.UpdateCandidateLanguageRequest;

public class PreScreenLanguage extends Fragment{
    ProgressDialog pd;
    LinearLayout languageListView;

    public boolean isFinalFragment = false;
    public Long jobPostId;
    private int totalCount;
    private int rank;

    private AsyncTask<UpdateCandidateLanguageRequest, Void, GenericResponse> mUpdateLanguageAsyncTask;

    final List<LanguageKnownObject> candidateLanguageKnown = new ArrayList<LanguageKnownObject>();
    LanguageKnownObject.Builder existingLanguageKnown = LanguageKnownObject.newBuilder();
    int pos = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View view = inflater.inflate(R.layout.pre_screen_language, container, false);
        PreScreenLanguageObject preScreenLanguageObject = null;
        Bundle bundle = getArguments();
        languageListView = (LinearLayout) view.findViewById(R.id.ps_language_list_view);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

        ((PreScreenActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // track screen view
        ((PreScreenActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_EDIT_LANGUAGE_PRESCREEN);


        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Language Details");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        pd = CustomProgressDialog.get(getActivity());

        try {
            preScreenLanguageObject = PreScreenLanguageObject.parseFrom(bundle.getByteArray("language"));

            isFinalFragment = bundle.getBoolean("isFinalFragment");
            jobPostId = bundle.getLong("jobPostId");
            rank = bundle.getInt("rank");
            totalCount = bundle.getInt("totalCount");

            String preScreenCompanyName = bundle.getString("companyName");
            String preScreenJobTitle = bundle.getString("jobTitle");

            TextView headingApplicationForm= (TextView) view.findViewById(R.id.headingApplicationForm);
            headingApplicationForm.setText("Application form for "+preScreenJobTitle+" at "+preScreenCompanyName);

            LinearLayout progressLayout = (LinearLayout) view.findViewById(R.id.progressCount);
            for(int i= 1; i<=totalCount; i++){
                ImageView progressDot = new ImageView(getContext());
                progressDot.setBackgroundResource(R.drawable.circle_small);
                if(i == rank) {
                    progressDot.setLayoutParams(new LinearLayout.LayoutParams(25, 25));
                }else {
                    progressDot.setLayoutParams(new LinearLayout.LayoutParams(10, 10));
                }
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) progressDot.getLayoutParams();
                lp.setMargins(5,35,5,35);
                progressDot.setLayoutParams(lp);
                progressLayout.addView(progressDot);
            }

            if(preScreenLanguageObject.isInitialized() && !preScreenLanguageObject.getIsMatching() ) {
                initLanguages(preScreenLanguageObject.getJobPostLanguageList());

                Button saveLanguageDetail = (Button) view.findViewById(R.id.save_language_btn);
                saveLanguageDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UpdateCandidateLanguageRequest.Builder languageBuilder = UpdateCandidateLanguageRequest.newBuilder();
                        if(candidateLanguageKnown.size() < 1){
                            PreScreenActivity.showRequiredFragment(getActivity());
                        } else {
                            //Track this action
                            ((PreScreenActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_EDIT_LANGUAGE_PRESCREEN, Constants.GA_ACTION_SAVE_LANGUAGE_PRESCREEN);

                            languageBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                            languageBuilder.addAllLanguageKnownObject(candidateLanguageKnown);
                            languageBuilder.setJobPostId(jobPostId);
                            languageBuilder.setIsFinalFragment(isFinalFragment);

                            mUpdateLanguageAsyncTask = new UpdateLanguageAsyncTask();
                            mUpdateLanguageAsyncTask.execute(languageBuilder.build());
                        }
                    }
                });
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return view;
    }


    public void initLanguages(List<LanguageObject> languageObjectList) {
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

            languageReadWrite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    boolean flag = false;
                    //check if this is there in list or not
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
                        } else {
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

    private class UpdateLanguageAsyncTask extends AsyncTask<UpdateCandidateLanguageRequest,
            Void, GenericResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected GenericResponse doInBackground(UpdateCandidateLanguageRequest... params) {
            return HttpRequest.updateCandidateLanguage(params[0]);
        }

        @Override
        protected void onPostExecute(GenericResponse genericResponse) {
            super.onPostExecute(genericResponse);
            pd.cancel();
            // move to next view
            PreScreenActivity.showRequiredFragment(getActivity());
        }
    }
}
