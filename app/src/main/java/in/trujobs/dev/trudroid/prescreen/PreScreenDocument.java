package in.trujobs.dev.trudroid.prescreen;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.protobuf.InvalidProtocolBufferException;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.proto.IdProofObject;
import in.trujobs.proto.PreScreenDocumentObject;

public class PreScreenDocument extends Fragment {
    ProgressDialog pd;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View view = inflater.inflate(R.layout.pre_screen_document, container, false);
        PreScreenDocumentObject preScreenDocumentObject = null;
        Bundle bundle = getArguments();
        LinearLayout documentListView = (LinearLayout) view.findViewById(R.id.document_list_view);


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
                for(IdProofObject idProofObject : preScreenDocumentObject.getJobPostIdProofList()) {

                    View mLinearView = inflater.inflate(R.layout.document_list_view, null);
                    EditText idProofTitle = (EditText) mLinearView
                            .findViewById(R.id.idproof_value);

                    Tlog.i("idProofId Name : " + idProofObject.getIdProofName());
                    idProofTitle.setHint(idProofObject.getIdProofName());
                    documentListView.addView(mLinearView);
                    final CheckBox documentCheckbox = (CheckBox) mLinearView.findViewById(R.id.idproof_checkbox);
                    documentCheckbox.setId(idProofObject.getIdProofId());
                }
                Button saveDocumentDetail = (Button) view.findViewById(R.id.save_document_btn);
                saveDocumentDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PreScreenActivity.showRequiredFragment(getActivity());
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
}