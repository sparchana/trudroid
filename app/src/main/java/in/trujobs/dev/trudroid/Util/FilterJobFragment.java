package in.trujobs.dev.trudroid.Util;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.CandidateProfileActivity;
import in.trujobs.dev.trudroid.SearchJobsActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.JobFilterRequest;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobSearchRequest;

/**
 * Created by zero on 9/8/16.
 */
public class FilterJobFragment extends Fragment implements OnClickListener {

    public ProgressDialog pd;

    public JobSearchRequest.Builder jobSearchRequest;

    public JobFilterRequest.Builder jobFilterRequest;

    public ListView jobPostListView;

    public static AsyncTask<JobSearchRequest, Void, JobPostResponse> mJobSearchAsyncTask;


    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.

    /* sort by */
    LinearLayout ftrSortBySalary;
    LinearLayout ftrSortByDatePosted;
    /* gender */
    LinearLayout ftrGenderMale;
    LinearLayout ftrGenderFemale;
    /* salary */
    TextView ftrSalaryEightKPlus;
    TextView ftrSalaryTenKPlus;
    TextView ftrSalaryTwelveKPlus;
    TextView ftrSalaryFifteenKPlus;
    TextView ftrSalaryTwentyKPlus;
    /* experience view */
    TextView ftrExperienceFresher;
    TextView ftrExperienceExperienced;
    /* education view */
    TextView ftrEduLtTen;
    TextView ftrEduTenPass;
    TextView ftrEduTwelvePass;
    TextView ftrEduUg;
    TextView ftrEduPg;
    /* misc */
    LinearLayout ftrDone;
    LinearLayout ftrClearAll;
    /* sub-filter_selected ui elements */
    ImageView imgSortBySalary;
    ImageView imgSortByDatePosted;
    ImageView imgFilterByMale;
    ImageView imgFilterFemale;
    TextView txtSortByDatePosted;
    TextView txtSortBySalary;
    TextView txtFilterByMale;
    TextView txtFilterByFemale;

    public boolean shouldClear = false;
    //filterBtn
    ImageView filterImage;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        View jobFilterRootView = inflater.inflate(R.layout.filter_container_layout, container, false);

        //checking if device android version is above 5 or not
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
            Tlog.e("Device has android 5 or above");
            int result = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
            }
            LinearLayout filterContainer = (LinearLayout) jobFilterRootView.findViewById(R.id.filter_panel_main_container);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            Tlog.e("Device has android 5 or above + statusbar height = " + result);
            layoutParams.setMargins(0, result, 0, 0);
            filterContainer.setLayoutParams(layoutParams);
        }

        // track screen view
        ((SearchJobsActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_VIEW_FILTER);

        /* Sort by */
        ftrSortBySalary = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_sort_by_salary);
        ftrSortByDatePosted = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_sort_by_date_posted);

        /* Gender */
        ftrGenderMale = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_gender_male);
        ftrGenderFemale = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_gender_female);
        /* salary */
        ftrSalaryEightKPlus = (TextView) jobFilterRootView.findViewById(R.id.ftr_salary_eight_k_plus);
        ftrSalaryTenKPlus = (TextView) jobFilterRootView.findViewById(R.id.ftr_salary_ten_k_plus);
        ftrSalaryTwelveKPlus = (TextView) jobFilterRootView.findViewById(R.id.ftr_salary_twelve_k_plus);
        ftrSalaryFifteenKPlus = (TextView) jobFilterRootView.findViewById(R.id.ftr_salary_fifteen_k_plus);
        ftrSalaryTwentyKPlus = (TextView) jobFilterRootView.findViewById(R.id.ftr_salary_twenty_k_plus);
        /* experience view */
        ftrExperienceFresher = (TextView) jobFilterRootView.findViewById(R.id.ftr_experience_fresher);
        ftrExperienceExperienced = (TextView) jobFilterRootView.findViewById(R.id.ftr_experience_experienced);
        /* education view */
        ftrEduLtTen = (TextView) jobFilterRootView.findViewById(R.id.ftr_edu_lt_ten);
        ftrEduTenPass = (TextView) jobFilterRootView.findViewById(R.id.ftr_edu_ten_pass);
        ftrEduTwelvePass = (TextView) jobFilterRootView.findViewById(R.id.ftr_edu_twelve_pass);
        ftrEduUg = (TextView) jobFilterRootView.findViewById(R.id.ftr_edu_ug);
        ftrEduPg = (TextView) jobFilterRootView.findViewById(R.id.ftr_edu_pg);

        ftrDone = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_done);
        ftrClearAll = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_clear_all);


        /* sub filter_selected element for UI manipulation */
        imgSortBySalary = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_sort_by_salary);
        imgSortByDatePosted = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_sort_by_date_posted);
        imgFilterByMale = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_ftr_by_male);
        imgFilterFemale = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_ftr_by_female);
        txtSortByDatePosted = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_sort_by_date_posted);
        txtSortBySalary = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_sort_by_salary);
        txtFilterByMale = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_ftr_by_male);
        txtFilterByFemale = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_ftr_by_female);

        filterImage = (ImageView) getActivity().findViewById(R.id.btn_job_filter);

        resetFragmentUI();
        if(SearchJobsActivity.jobFilterRequestBkp != null && jobFilterRequest == null){
            Tlog.i("jobFilterRequest is preserved");
            jobFilterRequest = SearchJobsActivity.jobFilterRequestBkp;
            onLoadUpdateFilterFragmentUI(jobFilterRequest.build());
        } else {
            jobFilterRequest = JobFilterRequest.newBuilder();
            Tlog.i("jobFilterReq init");
            jobFilterRequest.setCandidateMobile(Prefs.candidateMobile.get());
            assignSearchedLatLng();
        }

        pd = CustomProgressDialog.get(getActivity());

        ftrSortByDatePosted.setOnClickListener(this);
        ftrSortBySalary.setOnClickListener(this);
        ftrGenderMale.setOnClickListener(this);
        ftrGenderFemale.setOnClickListener(this);
        ftrExperienceFresher.setOnClickListener(this);
        ftrExperienceExperienced.setOnClickListener(this);
        ftrEduLtTen.setOnClickListener(this);
        ftrEduTenPass.setOnClickListener(this);
        ftrEduTwelvePass.setOnClickListener(this);
        ftrEduUg.setOnClickListener(this);
        ftrEduPg.setOnClickListener(this);
        ftrDone.setOnClickListener(this);
        ftrSalaryEightKPlus.setOnClickListener(this);
        ftrSalaryTenKPlus.setOnClickListener(this);
        ftrSalaryTwelveKPlus.setOnClickListener(this);
        ftrSalaryFifteenKPlus.setOnClickListener(this);
        ftrSalaryTwentyKPlus.setOnClickListener(this);
        ftrClearAll.setOnClickListener(this);

        LinearLayout closeFilterFragment = (LinearLayout) jobFilterRootView.findViewById(R.id.close_filter);
        closeFilterFragment.setOnClickListener(this);

        return jobFilterRootView;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here
        Tlog.i("InstanceState");
        outState.putInt("int", 10);
        outState.putByteArray("jobFilterRequest", jobFilterRequest.buildPartial().toByteArray());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //Restore the fragment's state here
            try {
                Tlog.i("retrieving data from saveInstanceState IntSaved: "+savedInstanceState.getInt("int"));
                jobFilterRequest = JobFilterRequest.parseFrom(savedInstanceState.getByteArray("jobFilterRequest")).toBuilder();
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        // Perform action on click
        switch (view.getId()) {
            case R.id.ftr_sort_by_date_posted:
                if (jobFilterRequest != null && !jobFilterRequest.getSortByDatePosted()) {
                    jobFilterRequest.setSortByDatePosted(true);
                    jobFilterRequest.setSortBySalary(false);
                    DateSalaryBtnManipulation(view.getId(), true);
                } else {
                    /* De-Select on double tap */
                    jobFilterRequest.setSortByDatePosted(false);
                    DateSalaryBtnManipulation(view.getId(), false);
                }
                break;
            case R.id.ftr_sort_by_salary:
                if (jobFilterRequest != null && !jobFilterRequest.getSortBySalary()) {
                    jobFilterRequest.setSortBySalary(true);
                    jobFilterRequest.setSortByDatePosted(false);
                    DateSalaryBtnManipulation(view.getId(), true);

                } else {
                    /* De-Select on double tap */
                    jobFilterRequest.setSortBySalary(false);
                    DateSalaryBtnManipulation(view.getId(), false);
                }
                break;
            /* SALARY */
            case R.id.ftr_salary_eight_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.EIGHT_K_PLUS) {
                    /* De-Select */
                    jobFilterRequest.clearSalary();
                    SalaryBtnManipulation(view.getId(), false);
                } else {
                    /* Select */
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.EIGHT_K_PLUS);
                    SalaryBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_salary_ten_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.TEN_K_PLUS) {
                    jobFilterRequest.clearSalary();
                    SalaryBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.TEN_K_PLUS);
                    SalaryBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_salary_twelve_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.TWELVE_K_PLUS) {
                    jobFilterRequest.clearSalary();
                    SalaryBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.TWELVE_K_PLUS);
                    SalaryBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_salary_fifteen_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.FIFTEEN_K_PLUS) {
                    jobFilterRequest.clearSalary();
                    SalaryBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.FIFTEEN_K_PLUS);
                    SalaryBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_salary_twenty_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.TWENTY_K_PLUS) {
                    jobFilterRequest.clearSalary();
                    SalaryBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.TWENTY_K_PLUS);
                    SalaryBtnManipulation(view.getId(), true);
                }
                break;

            /* EXPERIENCE */
            case R.id.ftr_experience_experienced:
                if (jobFilterRequest.getExp() == JobFilterRequest.Experience.EXPERIENCED) {
                    jobFilterRequest.clearExp();
                    ExpBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setExp(JobFilterRequest.Experience.EXPERIENCED);
                    ExpBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_experience_fresher:
                if (jobFilterRequest.getExp() == JobFilterRequest.Experience.FRESHER) {
                    jobFilterRequest.clearExp();
                    ExpBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setExp(JobFilterRequest.Experience.FRESHER);
                    ExpBtnManipulation(view.getId(), true);
                }
                break;
            /* EDUCATION */
            case R.id.ftr_edu_lt_ten:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.LT_TEN) {
                    jobFilterRequest.clearEdu();
                    EduBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.LT_TEN);
                    EduBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_edu_ten_pass:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.TEN_PASS) {
                    jobFilterRequest.clearEdu();
                    EduBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.TEN_PASS);
                    EduBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_edu_twelve_pass:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.TWELVE_PASS) {
                    jobFilterRequest.clearEdu();
                    EduBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.TWELVE_PASS);
                    EduBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_edu_ug:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.UG) {
                    jobFilterRequest.clearEdu();
                    EduBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.UG);
                    EduBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_edu_pg:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.PG) {
                    jobFilterRequest.clearEdu();
                    EduBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.PG);
                    EduBtnManipulation(view.getId(), true);
                }
                break;
            /* GENDER */
            case R.id.ftr_gender_male:
                if (jobFilterRequest.getGender() == JobFilterRequest.Gender.MALE) {
                    jobFilterRequest.clearGender();
                    GenderBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setGender(JobFilterRequest.Gender.MALE);
                    GenderBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_gender_female:
                if (jobFilterRequest.getGender() == JobFilterRequest.Gender.FEMALE) {
                    jobFilterRequest.clearGender();
                    GenderBtnManipulation(view.getId(), false);
                } else {
                    jobFilterRequest.setGender(JobFilterRequest.Gender.FEMALE);
                    GenderBtnManipulation(view.getId(), true);
                }
                break;
            case R.id.ftr_done:
                // send data for submission
                //
                if(shouldClear){
                    jobFilterRequest.clear();
                    if(SearchJobsActivity.jobFilterRequestBkp!= null)
                        SearchJobsActivity.jobFilterRequestBkp.clear();
                        assignSearchedLatLng();
                        SearchJobsActivity.btnFilterJob.setImageDrawable(getContext().getResources().getDrawable(R.drawable.filter_not_selected));
                } else {
                        SearchJobsActivity.btnFilterJob.setImageDrawable(getContext().getResources().getDrawable(R.drawable.filter_selected));
                }
                if(jobFilterRequest.isInitialized()){
                    assignSearchedLatLng();
                    jobSearchRequest.setJobFilterRequest(jobFilterRequest.build());
                    if(SearchJobsActivity.jobRolesFilter != null)jobSearchRequest.setJobSearchByJobRoleRequest(SearchJobsActivity.jobRolesFilter);
                    if(SearchJobsActivity.mSearchAddressOutput != null && !SearchJobsActivity.mSearchAddressOutput.trim().isEmpty()){
                        Tlog.i("Attach Locality: "+SearchJobsActivity.mSearchAddressOutput);
                        jobSearchRequest.setLocalityName(SearchJobsActivity.mSearchAddressOutput);
                    }
                    mJobSearchAsyncTask = new JobSearchAsyncTask();
                    mJobSearchAsyncTask.execute(jobSearchRequest.build());
                    SearchJobsActivity.jobFilterRequestBkp = jobFilterRequest;
                }

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_APPLY_FILTER);
                break;
            case R.id.ftr_clear_all:
                shouldClear = true;
                resetFragmentUI();

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_CLEAR_FILTER);
                break;

            case R.id.close_filter:
                shouldClear = false;
                getActivity().getSupportFragmentManager().popBackStack();

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_CLOSE_FILTER);
                break;
            default:
                break;
        }
    }

    public void resetFragmentUI() {
         /* deactivate all */
        SalaryBtnManipulation(null, false);
        EduBtnManipulation(null, false);
        ExpBtnManipulation(null, false);
        GenderBtnManipulation(null, false);
        DateSalaryBtnManipulation(null, false);
    }

    /* filter_selected ui component manipulation */
    private void SalaryBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
        ftrSalaryEightKPlus.setBackgroundResource(R.drawable.cust_border);
        ftrSalaryEightKPlus.setTextColor(Color.parseColor("#000000"));
        ftrSalaryTenKPlus.setBackgroundResource(R.drawable.cust_border);
        ftrSalaryTenKPlus.setTextColor(Color.parseColor("#000000"));
        ftrSalaryTwelveKPlus.setBackgroundResource(R.drawable.cust_border);
        ftrSalaryTwelveKPlus.setTextColor(Color.parseColor("#000000"));
        ftrSalaryFifteenKPlus.setBackgroundResource(R.drawable.cust_border);
        ftrSalaryFifteenKPlus.setTextColor(Color.parseColor("#000000"));
        ftrSalaryTwentyKPlus.setBackgroundResource(R.drawable.cust_border);
        ftrSalaryTwentyKPlus.setTextColor(Color.parseColor("#000000"));

        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_salary_eight_k_plus ){
                ftrSalaryEightKPlus.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrSalaryEightKPlus.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SALARY_8K_PLUS);
            } else if(shouldEnable && id == R.id.ftr_salary_ten_k_plus ){
                ftrSalaryTenKPlus.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrSalaryTenKPlus.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SALARY_10K_PLUS);
            } else if(shouldEnable && id == R.id.ftr_salary_twelve_k_plus ){
                ftrSalaryTwelveKPlus.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrSalaryTwelveKPlus.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SALARY_12K_PLUS);
            } else if(shouldEnable && id == R.id.ftr_salary_fifteen_k_plus ){
                ftrSalaryFifteenKPlus.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrSalaryFifteenKPlus.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SALARY_15K_PLUS);
            } else if(shouldEnable && id == R.id.ftr_salary_twenty_k_plus ){
                ftrSalaryTwentyKPlus.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrSalaryTwentyKPlus.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SALARY_20K_PLUS);
            }
        }
    }
    private void EduBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
        ftrEduLtTen.setBackgroundResource(R.drawable.cust_border);
        ftrEduLtTen.setTextColor(Color.parseColor("#000000"));
        ftrEduTenPass.setBackgroundResource(R.drawable.cust_border);
        ftrEduTenPass.setTextColor(Color.parseColor("#000000"));
        ftrEduTwelvePass.setBackgroundResource(R.drawable.cust_border);
        ftrEduTwelvePass.setTextColor(Color.parseColor("#000000"));
        ftrEduUg.setBackgroundResource(R.drawable.cust_border);
        ftrEduUg.setTextColor(Color.parseColor("#000000"));
        ftrEduPg.setBackgroundResource(R.drawable.cust_border);
        ftrEduPg.setTextColor(Color.parseColor("#000000"));

        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_edu_lt_ten){
                ftrEduLtTen.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrEduLtTen.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_EDU_LT_TEN);
            } else if(shouldEnable && id == R.id.ftr_edu_ten_pass){
                ftrEduTenPass.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrEduTenPass.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_EDU_TEN_PASS);
            } else if(shouldEnable && id == R.id.ftr_edu_twelve_pass){
                ftrEduTwelvePass.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrEduTwelvePass.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_EDU_TWELVE_PASS);
            } else if(shouldEnable && id == R.id.ftr_edu_ug){
                ftrEduUg.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrEduUg.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_EDU_UG);
            } else if(shouldEnable && id == R.id.ftr_edu_pg){
                ftrEduPg.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrEduPg.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_EDU_PG);
            }
        }
    }
    private void ExpBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
        ftrExperienceFresher.setBackgroundResource(R.drawable.cust_border);
        ftrExperienceFresher.setTextColor(Color.parseColor("#000000"));
        ftrExperienceExperienced.setBackgroundResource(R.drawable.cust_border);
        ftrExperienceExperienced.setTextColor(Color.parseColor("#000000"));

        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_experience_fresher){
                ftrExperienceFresher.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrExperienceFresher.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SORT_BY_EXP_FRESHER);
            } else if(shouldEnable &&  id == R.id.ftr_experience_experienced){
                ftrExperienceExperienced.setBackgroundResource(R.drawable.rounded_corner_button);
                ftrExperienceExperienced.setTextColor(Color.parseColor("#ffffff"));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SORT_BY_EXP_EXPERIENCED);
            }
        }
    }
    private void GenderBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
        imgFilterByMale.setImageResource(R.drawable.male_disable);
        imgFilterFemale.setImageResource(R.drawable.female_disable);
        txtFilterByMale.setTextColor(Color.parseColor("#000000"));
        txtFilterByFemale.setTextColor(Color.parseColor("#000000"));

        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_gender_male){
                imgFilterByMale.setImageResource(R.drawable.male);
                txtFilterByMale.setTextColor(getResources().getColor(R.color.colorPrimary));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SORT_BY_GENDER_MALE);
            } else if(shouldEnable && id == R.id.ftr_gender_female){
                imgFilterFemale.setImageResource(R.drawable.female);
                txtFilterByFemale.setTextColor(getResources().getColor(R.color.colorPrimary));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SORT_BY_GENDER_FEMALE);
            }
        }
    }
    private void DateSalaryBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
          /* ui manipulation */
        imgSortByDatePosted.setBackgroundResource(R.drawable.sort_by_circle_disable);
        imgSortByDatePosted.setImageResource(R.drawable.latest_not_selected);
        txtSortByDatePosted.setTextColor(getResources().getColor(R.color.boxOption));

        imgSortBySalary.setBackgroundResource(R.drawable.sort_by_circle_disable);
        imgSortBySalary.setImageResource(R.drawable.rupee_not_selected);
        txtSortBySalary.setTextColor(getResources().getColor(R.color.boxOption));


        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_sort_by_date_posted){
                imgSortByDatePosted.setBackgroundResource(R.drawable.sort_by_circle_enable);
                imgSortByDatePosted.setImageResource(R.drawable.latest);
                txtSortByDatePosted.setTextColor(getResources().getColor(R.color.back_grey_light_item));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SORT_BY_DATE_POSTED);
            } else if(shouldEnable && id == R.id.ftr_sort_by_salary){
                imgSortBySalary.setBackgroundResource(R.drawable.sort_by_circle_enable);
                imgSortBySalary.setImageResource(R.drawable.rupee);
                txtSortBySalary.setTextColor(getResources().getColor(R.color.back_grey_light_item));

                //Track this action
                ((SearchJobsActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_VIEW_FILTER, Constants.GA_ACTION_SORT_BY_SALARY);
            }
        }
    }

    private void assignSearchedLatLng() {
        jobSearchRequest = JobSearchRequest.newBuilder();
        jobSearchRequest.setCandidateMobile(Prefs.candidateMobile.get());
        Double lat = SearchJobsActivity.getmSearchLat()!=null?SearchJobsActivity.getmSearchLat()
                : Double.parseDouble(Prefs.candidateHomeLat.get());
        Double lng = SearchJobsActivity.getmSearchLng()!=null?SearchJobsActivity.getmSearchLng()
                : Double.parseDouble(Prefs.candidateHomeLng.get());
        jobSearchRequest.setLatitude(lat);
        jobFilterRequest.setJobSearchLatitude(lat);
        jobSearchRequest.setLongitude(lng);
        jobFilterRequest.setJobSearchLongitude(lng);
    }

    private void onLoadUpdateFilterFragmentUI(JobFilterRequest jobFilterRequest) {
        /* misc */
        if(jobFilterRequest.getSortByDatePosted()){
            DateSalaryBtnManipulation(R.id.ftr_sort_by_date_posted, true);
        }
        if(jobFilterRequest.getSortBySalary()){
            DateSalaryBtnManipulation(R.id.ftr_sort_by_salary, true);
        }
         /* salary */
        switch (jobFilterRequest.getSalary()){
            case EIGHT_K_PLUS: SalaryBtnManipulation(R.id.ftr_salary_eight_k_plus, true);
                break;
            case TEN_K_PLUS: SalaryBtnManipulation(R.id.ftr_salary_ten_k_plus, true);
                break;
            case TWELVE_K_PLUS: SalaryBtnManipulation(R.id.ftr_salary_twelve_k_plus, true);
                break;
            case FIFTEEN_K_PLUS: SalaryBtnManipulation(R.id.ftr_salary_fifteen_k_plus, true);
                break;
            case TWENTY_K_PLUS: SalaryBtnManipulation(R.id.ftr_salary_twenty_k_plus, true);
                break;
            default: break;
        }
        /* edu */
        switch (jobFilterRequest.getEdu()){
            case LT_TEN: EduBtnManipulation(R.id.ftr_edu_lt_ten, true);
                break;
            case TEN_PASS: EduBtnManipulation(R.id.ftr_edu_ten_pass, true);
                break;
            case TWELVE_PASS: EduBtnManipulation(R.id.ftr_edu_twelve_pass, true);
                break;
            case UG: EduBtnManipulation(R.id.ftr_edu_ug, true);
                break;
            case PG: EduBtnManipulation(R.id.ftr_edu_pg, true);
                break;
            default: break;
        }
        /* exp */
        switch (jobFilterRequest.getExp()){
            case FRESHER: ExpBtnManipulation(R.id.ftr_experience_fresher, true);
                break;
            case EXPERIENCED: ExpBtnManipulation(R.id.ftr_experience_experienced, true);
                break;
            default: break;
        }
        /* gender */
        switch (jobFilterRequest.getGender()){
            case MALE: GenderBtnManipulation(R.id.ftr_gender_male, true);
                break;
            case FEMALE: GenderBtnManipulation(R.id.ftr_gender_female, true);
                break;
            default: break;
        }
    }


    private class JobSearchAsyncTask extends AsyncTask<JobSearchRequest,
            Void, JobPostResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            if (jobPostListView != null) {
                jobPostListView.setAdapter(null);
            }
            Tlog.i("------ [FilterJobs] Before Calling JobSearchAsyncTask (FilterJobFragment)-----");
            Tlog.i("jobFilter status: "+jobSearchRequest.hasJobFilterRequest());
            Tlog.i("jobSearchByJobRoleRequest status: "+jobSearchRequest.hasJobSearchByJobRoleRequest());
            Tlog.i("lat/lng status: " + jobSearchRequest.getLatitude() + "/" + jobSearchRequest.getLongitude());
            pd.show();
        }

        @Override
        protected JobPostResponse doInBackground(JobSearchRequest... params) {
            Tlog.i("http req for getFilteredJobPost...");
            return HttpRequest.searchJobs(params[0]);
        }

        @Override
        protected void onPostExecute(JobPostResponse jobPostResponse) {
            super.onPostExecute(jobPostResponse);
            pd.cancel();
            if (jobPostResponse == null) {
                Tlog.i("No jobs received");
                ImageView errorImageView = (ImageView) getActivity().findViewById(R.id.something_went_wrong_image);
                ImageView noJobsImageView = (ImageView) getActivity().findViewById(R.id.no_jobs_image);
                errorImageView.setVisibility(View.VISIBLE);
                jobPostListView.setVisibility(View.GONE);
                Tlog.w("Null JobPosts Response");
                return;
            }
            updateJobPostUI(jobPostResponse.getJobPostList());
        }

        private void updateJobPostUI(List<JobPostObject> jobPostObjectList) {

            ((SearchJobsActivity) getActivity()).updateJobPostUI(jobPostObjectList);
            getActivity().onBackPressed();
        }

        public void showToast(String text) {
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        }

    }
}
