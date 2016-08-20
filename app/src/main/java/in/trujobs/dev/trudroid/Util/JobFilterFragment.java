package in.trujobs.dev.trudroid.Util;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.JobActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobFilterRequest;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobSearchRequest;

/**
 * Created by zero on 9/8/16.
 */
public class JobFilterFragment extends Fragment implements OnClickListener {

    public ProgressDialog pd;

    public JobSearchRequest.Builder jobSearchRequest;

    public JobFilterRequest.Builder jobFilterRequest;

    public ListView jobPostListView;

    public static AsyncTask<JobFilterRequest, Void, JobPostResponse> mFilterJobAsyncTask;
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
    /* sub-filter ui elements */
    ImageView imgSortBySalary;
    ImageView imgSortByDatePosted;
    ImageView imgFilterByMale;
    ImageView imgFilterFemale;
    TextView txtSortByDatePosted;
    TextView txtSortBySalary;
    TextView txtFilterByMale;
    TextView txtFilterByFemale;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        View jobFilterRootView = inflater.inflate(R.layout.filter_container_layout, container, false);

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


        /* sub filter element for UI manipulation */
        imgSortBySalary = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_sort_by_salary);
        imgSortByDatePosted = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_sort_by_date_posted);
        imgFilterByMale = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_ftr_by_male);
        imgFilterFemale = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_ftr_by_female);
        txtSortByDatePosted = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_sort_by_date_posted);
        txtSortBySalary = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_sort_by_salary);
        txtFilterByMale = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_ftr_by_male);
        txtFilterByFemale = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_ftr_by_female);


        if(JobActivity.jobFilterRequestBkp != null && jobFilterRequest == null){
            Tlog.i("jobFilterRequest is preserved");
            jobFilterRequest = JobActivity.jobFilterRequestBkp.toBuilder();
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
                if(jobFilterRequest.isInitialized()){
                    assignSearchedLatLng();
                    jobSearchRequest.setJobFilterRequest(jobFilterRequest.build());
                    if(JobActivity.jobRoles != null)jobSearchRequest.setJobSearchByJobRoleRequest(JobActivity.jobRoles);
                    mJobSearchAsyncTask = new JobSearchAsyncTask();
                    mJobSearchAsyncTask.execute(jobSearchRequest.build());
                    JobActivity.jobFilterRequestBkp = jobFilterRequest.build();
                }
                break;
            case R.id.ftr_clear_all:
                jobFilterRequest.clear();
                if(JobActivity.jobFilterRequestBkp!= null)
                    JobActivity.jobFilterRequestBkp.toBuilder().clear();
                assignSearchedLatLng();
                resetFragmentUI();
                break;
            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void resetFragmentUI() {
         /* deactivate all then enable one */
        SalaryBtnManipulation(null, false);
        EduBtnManipulation(null, false);
        ExpBtnManipulation(null, false);
        GenderBtnManipulation(null, false);
        DateSalaryBtnManipulation(null, false);
    }

    /* filter ui component manipulation */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void SalaryBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
        ftrSalaryEightKPlus.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrSalaryEightKPlus.setTextColor(Color.parseColor("#000000"));
        ftrSalaryTenKPlus.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrSalaryTenKPlus.setTextColor(Color.parseColor("#000000"));
        ftrSalaryTwelveKPlus.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrSalaryTwelveKPlus.setTextColor(Color.parseColor("#000000"));
        ftrSalaryFifteenKPlus.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrSalaryFifteenKPlus.setTextColor(Color.parseColor("#000000"));
        ftrSalaryTwentyKPlus.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrSalaryTwentyKPlus.setTextColor(Color.parseColor("#000000"));

        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_salary_eight_k_plus ){
                ftrSalaryEightKPlus.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrSalaryEightKPlus.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable && id == R.id.ftr_salary_ten_k_plus ){
                ftrSalaryTenKPlus.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrSalaryTenKPlus.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable && id == R.id.ftr_salary_twelve_k_plus ){
                ftrSalaryTwelveKPlus.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrSalaryTwelveKPlus.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable && id == R.id.ftr_salary_fifteen_k_plus ){
                ftrSalaryFifteenKPlus.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrSalaryFifteenKPlus.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable && id == R.id.ftr_salary_twenty_k_plus ){
                ftrSalaryTwentyKPlus.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrSalaryTwentyKPlus.setTextColor(Color.parseColor("#ffffff"));
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void EduBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
        ftrEduLtTen.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrEduLtTen.setTextColor(Color.parseColor("#000000"));
        ftrEduTenPass.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrEduTenPass.setTextColor(Color.parseColor("#000000"));
        ftrEduTwelvePass.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrEduTwelvePass.setTextColor(Color.parseColor("#000000"));
        ftrEduUg.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrEduUg.setTextColor(Color.parseColor("#000000"));
        ftrEduPg.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrEduPg.setTextColor(Color.parseColor("#000000"));

        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_edu_lt_ten){
                ftrEduLtTen.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrEduLtTen.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable && id == R.id.ftr_edu_ten_pass){
                ftrEduTenPass.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrEduTenPass.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable && id == R.id.ftr_edu_twelve_pass){
                ftrEduTwelvePass.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrEduTwelvePass.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable && id == R.id.ftr_edu_ug){
                ftrEduUg.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrEduUg.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable && id == R.id.ftr_edu_pg){
                ftrEduPg.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrEduPg.setTextColor(Color.parseColor("#ffffff"));
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void ExpBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
        ftrExperienceFresher.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrExperienceFresher.setTextColor(Color.parseColor("#000000"));
        ftrExperienceExperienced.setBackground(getResources().getDrawable(R.drawable.cust_border));
        ftrExperienceExperienced.setTextColor(Color.parseColor("#000000"));

        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_experience_fresher){
                ftrExperienceFresher.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrExperienceFresher.setTextColor(Color.parseColor("#ffffff"));
            } else if(shouldEnable &&  id == R.id.ftr_experience_experienced){
                ftrExperienceExperienced.setBackgroundColor(Color.parseColor("#749cf4"));
                ftrExperienceExperienced.setTextColor(Color.parseColor("#ffffff"));
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
                txtFilterByMale.setTextColor(Color.parseColor("#749cf4"));
            } else if(shouldEnable && id == R.id.ftr_gender_female){
                imgFilterFemale.setImageResource(R.drawable.female);
                txtFilterByFemale.setTextColor(Color.parseColor("#749cf4"));
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void DateSalaryBtnManipulation(Integer id, boolean shouldEnable){
        /* deactivate all then enable one */
          /* ui manipulation */
        imgSortByDatePosted.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_disable));
        imgSortByDatePosted.setImageResource(R.drawable.latest_not_selected);
        txtSortByDatePosted.setTextColor(getResources().getColor(R.color.boxOption));

        imgSortBySalary.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_disable));
        imgSortBySalary.setImageResource(R.drawable.rupee_not_selected);
        txtSortBySalary.setTextColor(getResources().getColor(R.color.boxOption));


        if(id != null && shouldEnable){
            if(shouldEnable && id == R.id.ftr_sort_by_date_posted){
                imgSortByDatePosted.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_enable));
                imgSortByDatePosted.setImageResource(R.drawable.latest);
                txtSortByDatePosted.setTextColor(getResources().getColor(R.color.back_grey_light_item));
            } else if(shouldEnable && id == R.id.ftr_sort_by_salary){
                imgSortBySalary.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_enable));
                imgSortBySalary.setImageResource(R.drawable.rupee);
                txtSortBySalary.setTextColor(getResources().getColor(R.color.back_grey_light_item));
            }
        }
    }

    private void assignSearchedLatLng() {
        jobSearchRequest = JobSearchRequest.newBuilder();
        jobSearchRequest.setCandidateMobile(Prefs.candidateMobile.get());
        if(JobActivity.getmSearchLat()!=null){
            jobSearchRequest.setLatitude(JobActivity.getmSearchLat());
            jobFilterRequest.setJobSearchLatitude(JobActivity.getmSearchLat());
            Tlog.i("filter set to searched lat : "+JobActivity.getmSearchLat());

        }
        if(JobActivity.getmSearchLng() != null){
            jobSearchRequest.setLongitude(JobActivity.getmSearchLng());
            jobFilterRequest.setJobSearchLongitude(JobActivity.getmSearchLng());
            Tlog.i("filter set to searched ln : "+JobActivity.getmSearchLng());
        }
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
            try{
                jobPostListView = (ListView) getActivity().findViewById(R.id.jobs_list_view);
                if (jobPostObjectList.size() > 0) {
                    if(jobPostListView.getVisibility() == View.GONE){
                        jobPostListView.setVisibility(View.VISIBLE);
                    }
                    Tlog.i(jobPostObjectList.size()+" jobs received");
                    Tlog.i("DataSize: " + jobPostObjectList.size());
                    JobPostAdapter jobPostAdapter = new JobPostAdapter(getActivity(), jobPostObjectList);
                    jobPostListView.setAdapter(jobPostAdapter);
                } else {
                    ImageView noJobsImageView = (ImageView) getActivity().findViewById(R.id.no_jobs_image);
                    noJobsImageView.setVisibility(View.VISIBLE);
                    jobPostListView.setVisibility(View.GONE);

                    showToast("No jobs found !!");
                }
            } catch (NullPointerException np){
                Tlog.e(""+np.getStackTrace());
            }
            getActivity().onBackPressed();
        }

        public void showToast(String text) {
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        }

    }
}
