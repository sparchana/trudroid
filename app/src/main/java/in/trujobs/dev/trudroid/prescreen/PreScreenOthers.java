package in.trujobs.dev.trudroid.prescreen;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.proto.AssetObject;
import in.trujobs.proto.PreScreenAssetObject;

public class PreScreenOthers extends Fragment {
    View view;

    private Button maleBtn, femaleBtn;
    private DatePickerDialog dobDatePicker;
    private EditText candidateDob;
    private Integer genderValue = -1;
    private Long shiftValue = Long.valueOf(-1);
    private AutoCompleteTextView mHomeLocalityTxtView;
    ProgressDialog pd;
    public android.support.design.widget.TextInputLayout shiftLayout;
    public LinearLayout genderBtnLayout;
    public LinearLayout assetListView;

    // currently as per app flow, total exp resides within experience card
    // hence we won't show it here.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        view = inflater.inflate(R.layout.pre_screen_other_fragment, container, false);

        candidateDob = (EditText) view.findViewById(R.id.date_of_birth_edit_text);

        mHomeLocalityTxtView = (AutoCompleteTextView) view.findViewById(R.id.home_locality_auto_complete_edit_text);

        genderBtnLayout = (LinearLayout) view.findViewById(R.id.gender_button_layout);
        maleBtn = (Button) view.findViewById(R.id.gender_male);
        femaleBtn = (Button) view.findViewById(R.id.gender_female);

        assetListView = (LinearLayout) view.findViewById(R.id.asset_list_view);
        PreScreenAssetObject preScreenAssetObject;
        Bundle bundle = getArguments();

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

        ((PreScreenActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // track screen view
        ((PreScreenActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_EDIT_OTHER_DETAIL_PRESCREEN);


        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Important Details");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        pd = CustomProgressDialog.get(getActivity());

        if(PreScreenActivity.propertyIdQueue.contains(2)) {
            // render assets
            try {
                preScreenAssetObject = PreScreenAssetObject.parseFrom(bundle.getByteArray("asset"));
                for(AssetObject assetObject : preScreenAssetObject.getJobPostAssetList()){
                    View mLinearView = inflater.inflate(R.layout.pre_screen_asset_item, null);
                    TextView assetTitle = (TextView) mLinearView
                            .findViewById(R.id.asset_title);
                    assetTitle.setText(assetObject.getAssetTitle());
                    assetListView.addView(mLinearView);
                    final CheckBox assetCheckbox = (CheckBox) mLinearView.findViewById(R.id.asset_checkbox);
                    assetCheckbox.setId(assetObject.getAssetId());
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        Tlog.i("remaining ids, that needed to be shown in one fragment: ");
        return textView;
    }
}
