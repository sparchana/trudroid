package in.trujobs.dev.trudroid.prescreen;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.InterviewUtil;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.GenericResponse;
import in.trujobs.proto.GetInterviewSlotsRequest;
import in.trujobs.proto.GetInterviewSlotsResponse;
import in.trujobs.proto.UpdateCandidateInterviewDetailRequest;

import static in.trujobs.dev.trudroid.Util.InterviewUtil.getDayVal;
import static in.trujobs.dev.trudroid.Util.InterviewUtil.getMonthVal;

/**
 * A simple {@link Fragment} subclass.
 */
public class InterviewSlotSelectFragment extends Fragment {


    private AsyncTask<UpdateCandidateInterviewDetailRequest,
            Void, GenericResponse> updateCandidateInterviewDetailAsyncTask;
    private AsyncTask<GetInterviewSlotsRequest,
            Void, GetInterviewSlotsResponse> mGetInterviewSlotAsyncTask;

    public String preScreenCompanyName;
    public String preScreenJobTitle;
    public String preScreenJobRoleTitle;
    public Long preScreenJobPostId;
    SpinnerAdapter adapter;
    String[] interviewSlotArray = new String[0];
    Integer[] interviewSlotIdArray = new Integer[0];
    Date[] interviewSlotDateArray = new Date[0];
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

        pd = CustomProgressDialog.get(getActivity());

        preScreenCompanyName = bundle.getString("companyName");
        preScreenJobRoleTitle = bundle.getString("jobRoleTitle");
        preScreenJobTitle = bundle.getString("jobTitle");
        preScreenJobPostId = bundle.getLong("jobPostId");

        TextView companyName = (TextView) view.findViewById(R.id.interview_company_title);
        TextView jobTitle = (TextView) view.findViewById(R.id.interview_job_title);
        companyName.setText(preScreenCompanyName);
        jobTitle.setText(preScreenJobTitle);

        GetInterviewSlotsRequest.Builder req = GetInterviewSlotsRequest.newBuilder();
        req.setJobPostId(preScreenJobPostId);
        Tlog.i("jobPostId: " + preScreenJobPostId);

        mGetInterviewSlotAsyncTask = new GetInterviewSlotAsyncTask();
        mGetInterviewSlotAsyncTask.execute(req.build());


        return view;
    }

    private class GetInterviewSlotAsyncTask extends AsyncTask<GetInterviewSlotsRequest,
            Void, GetInterviewSlotsResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected GetInterviewSlotsResponse doInBackground(GetInterviewSlotsRequest... params) {
            return HttpRequest.getInterviewSlots(params[0]);
        }

        @Override
        protected void onPostExecute(GetInterviewSlotsResponse response) {
            super.onPostExecute(response);
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if(response == null){
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
            } else {

                // construct value
                final Spinner interviewSlot = (Spinner) view.findViewById(R.id.interview_slot_select);

                Tlog.i("slot size: " + response.getInterviewSlotsList().size());
                interviewSlotArray = new String[response.getInterviewSlotsList().size() * 7 + 1];
                interviewSlotIdArray = new Integer[response.getInterviewSlotsList().size() * 7 + 1];
                interviewSlotDateArray = new Date[response.getInterviewSlotsList().size() * 7 + 1];

                // setting the following values run in paralle
                interviewSlotArray[0] = "Select Interview Slot";
                interviewSlotIdArray[0] = Integer.valueOf(-1);
                interviewSlotDateArray[0] = null;

                for(int i = 1, k = 2; k < 9; ++k) {
                        Calendar newCalendar = Calendar.getInstance();
                        newCalendar.get(Calendar.YEAR);
                        newCalendar.get(Calendar.MONTH);
                        newCalendar.get(Calendar.DAY_OF_MONTH);
                        Date today = newCalendar.getTime();

                        Calendar c = Calendar.getInstance();
                        Tlog.i("today:" + today);
                        c.setTime(today);
                        c.add(Calendar.DATE, k);
                        Date x = c.getTime();

                    for(int j = 0; j< response.getInterviewSlotsList().size(); ++j) {

                        // in a day , create entry for each different time slot
                        String interviewDays = response.getInterviewSlotsList().get(j).getInterviewDays();

                        if (InterviewUtil.checkSlotAvailability(today, interviewDays)) {
                            interviewSlotIdArray[i] = response.getInterviewSlotsList().get(j).getInterviewTimeSlotObject().getSlotId();
                            interviewSlotDateArray[i] = x;
                            interviewSlotArray[i] = getDayVal(x.getDay())+ ", "
                                    + x.getDate() + " " + getMonthVal((x.getMonth() + 1))
                                    + " (" + response.getInterviewSlotsList().get(j).getInterviewTimeSlotObject().getSlotTitle() + ")" ;
                            i++;
                        }
                    }
                }

                adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, interviewSlotArray);
                interviewSlot.setAdapter(adapter);

                Button saveInterviewSlot = (Button) view.findViewById(R.id.save_interview_btn);
                saveInterviewSlot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean check = true;
                        if(interviewSlot.getSelectedItemPosition() < 0){
                            check = false;
                        }
                        if(check){
                            int slotTimeId = interviewSlotIdArray[interviewSlot.getSelectedItemPosition()];
                            Date slotDate = interviewSlotDateArray[interviewSlot.getSelectedItemPosition()];

                            UpdateCandidateInterviewDetailRequest.Builder interviewDetails = UpdateCandidateInterviewDetailRequest.newBuilder();
                            interviewDetails.setJobPostId(preScreenJobPostId);
                            interviewDetails.setCandidateMobile(Prefs.candidateMobile.get());
                            // sending current date fix this
                            interviewDetails.setScheduledInterviewDateInMills(slotDate.getTime());
                            interviewDetails.setTimeSlotId(slotTimeId);

                            Tlog.i("interview date" + slotDate.toString());
                            Tlog.i("interview time" + slotTimeId);
                            updateCandidateInterviewDetailAsyncTask = new UpdateCandidateInterviewAsyncTask();
                            updateCandidateInterviewDetailAsyncTask.execute(interviewDetails.build());
                        }
                    }
                });



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
                    Tlog.i("successful interview slot selection");
                }
            }
        }
    }

}
