package in.trujobs.dev.trudroid.prescreen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.proto.IdProofObject;
import in.trujobs.proto.LanguageObject;
import in.trujobs.proto.PreScreenDocumentObject;
import in.trujobs.proto.PreScreenLanguageObject;

public class PreScreenLanguage extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View view = inflater.inflate(R.layout.pre_screen_language, container, false);
        PreScreenLanguageObject preScreenLanguageObject = null;
        Bundle bundle = getArguments();
        LinearLayout languageListView = (LinearLayout) view.findViewById(R.id.ps_language_list_view);

        try {
            preScreenLanguageObject = PreScreenLanguageObject.parseFrom(bundle.getByteArray("language"));

            if(preScreenLanguageObject.isInitialized() && !preScreenLanguageObject.getIsMatching() ) {
                for(LanguageObject languageObject : preScreenLanguageObject.getJobPostLanguageList()) {

                    View mLinearView = inflater.inflate(R.layout.language_list_view, null);
                    TextView languageTitle = (TextView) mLinearView
                            .findViewById(R.id.ps_language_name);
                    Tlog.i("langName: " + languageObject.getLanguageName());
                    languageTitle.setText(languageObject.getLanguageName());
                    languageListView.addView(mLinearView);
                }
                Button saveLanguageDetail = (Button) view.findViewById(R.id.save_language_btn);
                saveLanguageDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PreScreenActivity.showRequiredFragment(getActivity());
                    }
                });
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        if(preScreenLanguageObject != null) {
            Tlog.i("language list size" + preScreenLanguageObject.getJobPostLanguageList().size());
        }
        return view;
    }
}
