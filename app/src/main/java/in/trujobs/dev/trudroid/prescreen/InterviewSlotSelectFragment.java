package in.trujobs.dev.trudroid.prescreen;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.GenericResponse;
import in.trujobs.proto.GetInterviewSlotsRequest;
import in.trujobs.proto.GetInterviewSlotsResponse;
import in.trujobs.proto.InterviewSlot;
import in.trujobs.proto.UpdateCandidateInterviewDetailRequest;

/**
 * A simple {@link Fragment} subclass.
 */
public class InterviewSlotSelectFragment extends Fragment {

    public String preScreenCompanyName;
    public String preScreenJobTitle;
    public String preScreenJobRoleTitle;
    public Long preScreenJobPostId;
    SpinnerAdapter adapter;
    String[] interviewSlotArray = new String[0];
    Long[] interviewSlotIdArray = new Long[0];
    View view;

    ProgressDialog pd;

    public InterviewSlotSelectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.interview_slot_select, container, false);
        Bundle bundle = getArguments();

        preScreenCompanyName = bundle.getString("companyName");
        preScreenJobRoleTitle = bundle.getString("jobRoleTitle");
        preScreenJobTitle = bundle.getString("jobTitle");

        TextView companyName = (TextView) view.findViewById(R.id.interview_company_title);
        TextView jobTitle = (TextView) view.findViewById(R.id.interview_job_title);
        companyName.setText(preScreenCompanyName);
        jobTitle.setText(preScreenJobTitle);

        // setting the following values run in paralle
        interviewSlotArray[0] = "Select Interview Slot";
        interviewSlotIdArray[0] = Long.valueOf(-1);

        pd = CustomProgressDialog.get(getActivity());

        return view;
    }

    private class GetInterviewSlotAsyncTask extends AsyncTask<GetInterviewSlotsRequest,
            Void, GetInterviewSlotsResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected GetInterviewSlotsResponse doInBackground(GetInterviewSlotsRequest... params) {
            return HttpRequest.getInterviewSlots(params[0]);
        }

        @Override
        protected void onPostExecute(GetInterviewSlotsResponse response) {
            super.onPostExecute(response);
            pd.cancel();
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if(response == null){
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
            } else {

                // construct value
                final Spinner interviewSlot = (Spinner) view.findViewById(R.id.interview_slot_select);

                for(int i = 0; i< response.getInterviewSlotsList().size(); ++i) {
                    response.getInterviewSlotsList().get(i).getInterviewTimeSlotObject().getSlotId();
                    response.getInterviewSlotsList().get(i).getInterviewTimeSlotObject().getSlotTitle();
                    response.getInterviewSlotsList().get(i).getInterviewDateSlot();
                    interviewSlotArray[i+1] = 
                }
                for(InterviewSlot slot: response.getInterviewSlotsList()) {

                }

                adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, interviewSlotArray);
                interviewSlot.setAdapter(adapter);

            }
        }
    }

    private class UpdateCandidateInterviewAsyncTask extends AsyncTask<UpdateCandidateInterviewDetailRequest,
            Void, GenericResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected GenericResponse doInBackground(UpdateCandidateInterviewDetailRequest... params) {
            return HttpRequest.updateCandidateInterviewDetail(params[0]);
        }

        @Override
        protected void onPostExecute(GenericResponse response) {
            super.onPostExecute(response);
            pd.cancel();
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if(response == null){
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
            } else {
                if (response.getStatus() == GenericResponse.Status.SUCCESS) {
                    // back to search

                }
            }
        }
    }

}
