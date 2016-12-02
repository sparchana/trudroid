package in.trujobs.dev.trudroid.prescreen;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
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
import static in.trujobs.dev.trudroid.prescreen.PreScreenActivity.redirectToSearch;

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

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

        ((PreScreenActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // track screen view
        ((PreScreenActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_EDIT_EDUCATION_PRESCREEN);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Education Details");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));


        GetInterviewSlotsRequest.Builder req = GetInterviewSlotsRequest.newBuilder();
        req.setJobPostId(preScreenJobPostId);
        Tlog.i("jobPostId: " + preScreenJobPostId);
        PreScreenActivity.interviewSlotOpenned = true;
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
                ArrayList<String> interviewSlotList = new ArrayList<>();
                interviewSlotIdArray = new Integer[response.getInterviewSlotsList().size() * 7 + 1];
                interviewSlotDateArray = new Date[response.getInterviewSlotsList().size() * 7 + 1];

                // setting the following values run in paralle
                interviewSlotList.add("Select Interview Slot");
                interviewSlotIdArray[0] = Integer.valueOf(-1);
                interviewSlotDateArray[0] = null;

                for(int i = 1, k = 2; k < 9; ++k) {
                        Calendar newCalendar = Calendar.getInstance();
                        newCalendar.get(Calendar.YEAR);
                        newCalendar.get(Calendar.MONTH);
                        newCalendar.get(Calendar.DAY_OF_MONTH);
                        Date today = newCalendar.getTime();

                        Calendar c = Calendar.getInstance();
                        c.setTime(today);
                    c.add(Calendar.DATE, k);
                    Date x = c.getTime();
                    Tlog.i("creating for :" + x);

                    for(int j = 0; j< response.getInterviewSlotsList().size(); ++j) {

                        Tlog.i("slot no :" + j);
                        // in a day , create entry for each different time slot
                        String interviewDays = response.getInterviewSlotsList().get(j).getInterviewDays();

                        if (InterviewUtil.checkSlotAvailability(x, interviewDays)) {
                            interviewSlotIdArray[i] = response.getInterviewSlotsList().get(j).getInterviewTimeSlotObject().getSlotId();
                            interviewSlotDateArray[i] = x;
                            String slotString = getDayVal(x.getDay())+ ", "
                                    + x.getDate() + " " + getMonthVal((x.getMonth() + 1))
                                    + " (" + response.getInterviewSlotsList().get(j).getInterviewTimeSlotObject().getSlotTitle() + ")" ;
                            interviewSlotList.add(slotString);
                            i++;
                        }
                    }
                }
                interviewSlotArray = new String[interviewSlotList.size()];
                //First Step: convert ArrayList to an Object array.
                Object[] objDays = interviewSlotList.toArray();

                //Second Step: convert Object array to String array
                interviewSlotArray = Arrays.copyOf(objDays, objDays.length, String[].class);

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
                    // show successfully applied message and redirect to search screen
                    showDialog("Interview Scheduled Successfully");
                } else {
                    showDialog("Something went wrong. Please try again.");
                }
            }
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
                        redirectToSearch();
                    }
                });
        alertDialog.show();
    }

}
