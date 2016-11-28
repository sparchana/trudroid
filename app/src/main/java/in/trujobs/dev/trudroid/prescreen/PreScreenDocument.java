package in.trujobs.dev.trudroid.prescreen;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Validator;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.GenericResponse;
import in.trujobs.proto.IdProofObject;
import in.trujobs.proto.IdProofObjectWithNumber;
import in.trujobs.proto.PreScreenDocumentObject;
import in.trujobs.proto.UpdateCandidateDocumentRequest;

public class PreScreenDocument extends Fragment {
    ProgressDialog pd;
    List<IdProofObjectWithNumber> candidateDocument = new ArrayList<IdProofObjectWithNumber>();
    final Map<Integer, IdProofObjectWithNumber> candidateDocumentMap = new HashMap<>();
    private AsyncTask<UpdateCandidateDocumentRequest, Void, GenericResponse> mUpdateDocumentAsyncTask;
    LinearLayout documentListView;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        view = inflater.inflate(R.layout.pre_screen_document, container, false);
        PreScreenDocumentObject preScreenDocumentObject = null;
        Bundle bundle = getArguments();
        documentListView = (LinearLayout) view.findViewById(R.id.ps_document_list_view);


        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

        ((PreScreenActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // track screen view
        ((PreScreenActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_EDIT_DOCUMENT_PRESCREEN);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Document Details");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        pd = CustomProgressDialog.get(getActivity());

        try {
            preScreenDocumentObject = PreScreenDocumentObject.parseFrom(bundle.getByteArray("document"));

            if(preScreenDocumentObject.isInitialized() && !preScreenDocumentObject.getIsMatching() ) {
                getAllAssets(preScreenDocumentObject.getJobPostIdProofList());


                Button saveDocumentDetail = (Button) view.findViewById(R.id.save_document_btn);
                saveDocumentDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UpdateCandidateDocumentRequest.Builder documentBuilder = UpdateCandidateDocumentRequest.newBuilder();
                        boolean check = true;
                        candidateDocument =  new ArrayList<IdProofObjectWithNumber>();

                        for(Map.Entry<Integer, IdProofObjectWithNumber> entry: candidateDocumentMap.entrySet()){
                            candidateDocument.add(entry.getValue());
                        }
                        Tlog.i("size: " + candidateDocument.size());
                        if(candidateDocument.size() > 0) {
                            // validate document
                            for (IdProofObjectWithNumber idProofObjectWithNumber: candidateDocument) {
                                Tlog.i("id: " + idProofObjectWithNumber.getIdProof().getIdProofId() + " val: " +
                                        idProofObjectWithNumber.getIdProofNumber());
                                if(!ValidateIdProof(idProofObjectWithNumber.getIdProof().getIdProofId(),
                                        idProofObjectWithNumber.getIdProofNumber())){
                                    check = false;
                                    break;
                                }
                            }
                        }
                        if(check) {
                            //Track this action
                            ((PreScreenActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_EDIT_DOCUMENT_PRESCREEN, Constants.GA_ACTION_SAVE_DOCUMENT_PRESCREEN);

                            documentBuilder.setCandidateMobile(Prefs.candidateMobile.get());

                            documentBuilder.addAllIdProof(candidateDocument);

                            // doc update async task
                            mUpdateDocumentAsyncTask = new UpdateDocumentAsyncTask();
                            mUpdateDocumentAsyncTask.execute(documentBuilder.build());
                        }
                    }
                });
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        if(preScreenDocumentObject != null) {
            Tlog.i("document value" + preScreenDocumentObject.getJobPostIdProofList().size());
        }
        return view;
    }

    private void getAllAssets(List<IdProofObject> jobPostIdProofList) {
        for(IdProofObject idProofObject : jobPostIdProofList) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View mLinearView = inflater.inflate(R.layout.document_list_item, null);
            EditText idProofTitle = (EditText) mLinearView
                    .findViewById(R.id.idproof_value);

            Tlog.i("idProofId Name : " + idProofObject.getIdProofName());
            idProofTitle.setHint(idProofObject.getIdProofName());
            documentListView.addView(mLinearView);

            final CheckBox documentCheckbox = (CheckBox) mLinearView.findViewById(R.id.idproof_checkbox);
            final EditText documentValue = (EditText) mLinearView.findViewById(R.id.idproof_value);
            documentCheckbox.setId(idProofObject.getIdProofId());

            documentValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    IdProofObjectWithNumber.Builder document = IdProofObjectWithNumber.newBuilder();
                    if(documentCheckbox.isChecked()){
                        IdProofObject.Builder idProof = IdProofObject.newBuilder();
                        idProof.setIdProofId(documentCheckbox.getId());
                        document.setIdProof(idProof);
                        document.setIdProofNumber(String.valueOf(documentValue.getText()));

                        if(candidateDocumentMap.containsKey(document.getIdProof().getIdProofId())){
                            Tlog.i(document.getIdProof().getIdProofId() + " rm added ");
                            candidateDocumentMap.remove(document.getIdProof().getIdProofId());
                            candidateDocumentMap.put(document.getIdProof().getIdProofId(), document.build());
                        } else {
                            Tlog.i(document.getIdProof().getIdProofId() + " added ");
                            candidateDocumentMap.put(document.getIdProof().getIdProofId(), document.build());
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            documentCheckbox.setOnCheckedChangeListener(new  CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    IdProofObjectWithNumber.Builder document = IdProofObjectWithNumber.newBuilder();

                    if(!b){
                        Tlog.i(documentCheckbox.getId() + " removed ");
                        candidateDocumentMap.remove(documentCheckbox.getId());
                    } else {
                        if(documentValue.getText().toString() != ""){
                            IdProofObject.Builder idProof = IdProofObject.newBuilder();
                            idProof.setIdProofId(documentCheckbox.getId());
                            document.setIdProof(idProof);
                            document.setIdProofNumber(String.valueOf(documentValue.getText()));
                            if(candidateDocumentMap.containsKey(document.getIdProof().getIdProofId())){
                                candidateDocumentMap.remove(document.getIdProof().getIdProofId());
                                candidateDocumentMap.put(document.getIdProof().getIdProofId(), document.build());
                            } else {
                                candidateDocumentMap.put(document.getIdProof().getIdProofId(), document.build());
                            }
                        }
                    }
                }
            });
        }
    }


    private class UpdateDocumentAsyncTask extends AsyncTask<UpdateCandidateDocumentRequest,
                Void, GenericResponse> {
        @Override
        protected GenericResponse doInBackground(UpdateCandidateDocumentRequest... params) {
            return HttpRequest.updateCandidateDocument(params[0]);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected void onPostExecute(GenericResponse genericResponse) {
            super.onPostExecute(genericResponse);
            pd.cancel();
            PreScreenActivity.showRequiredFragment(getActivity());
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

    public boolean ValidateIdProof(int id, String value){
        if(value == null) {
            return false;
        }
        boolean flag;

        switch (id){
            case 1:
                flag = Validator.validateDL(value);
                if(!flag){
                    showDialog("Please provide a valid Driving Licence  Number");
                }
                return flag;
            case 2:
                flag = Validator.validatePASSPORT(value);
                if(!flag){
                    showDialog("Please provide a valid Passport Number");
                }
                return flag;
            case 3:
                flag = Validator.validateAadhaar(value);
                if(!flag){
                    Tlog.e("aadhar card value: " + value);
                    showDialog("Please provide a valid Aadhaar Number");
                }
                return flag;
            case 4:
                flag = Validator.validatePAN(value);
                if(!flag){
                    showDialog("Please provide a valid PAN card Number");
                }
                return flag;
        }
        return false;
    }

}