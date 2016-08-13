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

import java.util.List;

import in.trujobs.dev.trudroid.Adapters.JobPostAdapter;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobFilterRequest;
import in.trujobs.proto.JobPostObject;
import in.trujobs.proto.JobPostResponse;

/**
 * Created by zero on 9/8/16.
 */
public class JobFilterFragment extends Fragment implements OnClickListener {

    public ProgressDialog pd;

    public JobFilterRequest.Builder jobFilterRequest;

    public ListView jobPostListView;

    public static AsyncTask<JobFilterRequest, Void, JobPostResponse> mFilterJobAsyncTask;


    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.

    /* sort by */
    LinearLayout ftrSortBySalary;
    LinearLayout ftrSortByDatePosted;
    /* gender */
    LinearLayout ftrGenderMale;
    LinearLayout ftrGenderFemale;
    /* salary */
    LinearLayout ftrSalaryEightKPlus;
    LinearLayout ftrSalaryTenKPlus;
    LinearLayout ftrSalaryTwelveKPlus;
    LinearLayout ftrSalaryFifteenKPlus;
    LinearLayout ftrSalaryTwentyKPlus;
    /* experience view */
    LinearLayout ftrExperienceFresher;
    LinearLayout ftrExperienceExperienced;
    /* education view */
    LinearLayout ftrEduLtTen;
    LinearLayout ftrEduTenPass;
    LinearLayout ftrEduTwelvePass;
    LinearLayout ftrEduUg;
    LinearLayout ftrEduPg;
    /* misc */
    LinearLayout ftrDone;
    LinearLayout ftrClearAll;
    /* sub-filter ui elements */
    ImageView imgSortBySalary;
    ImageView imgSortByDatePosted;
    TextView txtSortByDatePosted;
    TextView txtSortBySalary;
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
        ftrSalaryEightKPlus = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_salary_eight_k_plus);
        ftrSalaryTenKPlus = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_salary_ten_k_plus);
        ftrSalaryTwelveKPlus = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_salary_twelve_k_plus);
        ftrSalaryFifteenKPlus = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_salary_fifteen_k_plus);
        ftrSalaryTwentyKPlus = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_salary_twenty_k_plus);
        /* experience view */
        ftrExperienceFresher = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_experience_fresher);
        ftrExperienceExperienced = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_experience_experienced);
        /* education view */
        ftrEduLtTen = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_edu_lt_ten);
        ftrEduTenPass = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_edu_ten_pass);
        ftrEduTwelvePass = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_edu_twelve_pass);
        ftrEduUg = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_edu_ug);
        ftrEduPg = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_edu_pg);

        ftrDone = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_done);
        ftrClearAll = (LinearLayout) jobFilterRootView.findViewById(R.id.ftr_clear_all);


        /* sub filter element for UI manipulation */
        imgSortBySalary = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_sort_by_salary);
        imgSortByDatePosted = (ImageView) jobFilterRootView.findViewById(R.id.ftr_img_sort_by_date_posted);
        txtSortByDatePosted = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_sort_by_date_posted);
        txtSortBySalary = (TextView) jobFilterRootView.findViewById(R.id.ftr_txt_sort_by_salary);


        jobFilterRequest = JobFilterRequest.newBuilder();
        Tlog.i("jobFilterReq init");
        jobFilterRequest.setCandidateMobile(Prefs.candidateMobile.get());
        Tlog.i("setCandidateMobile(Prefs.candidateMobile.get())");

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

        return jobFilterRootView;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {
        // Perform action on click
        switch (view.getId()) {
            case R.id.ftr_sort_by_date_posted:
                Tlog.i("SDP"+ jobFilterRequest.getSortByDatePosted() + " SS: "+
                        jobFilterRequest.getSortBySalary());
                if (jobFilterRequest != null && !jobFilterRequest.getSortByDatePosted()) {
                    jobFilterRequest.setSortByDatePosted(true);
                    jobFilterRequest.setSortBySalary(false);

                    /* ui manipulation */
                    imgSortByDatePosted.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_enable));
                    imgSortByDatePosted.setImageResource(R.drawable.latest);
                    txtSortByDatePosted.setTextColor(getResources().getColor(R.color.back_grey_light_item));
                    imgSortBySalary.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_disable));
                    imgSortBySalary.setImageResource(R.drawable.rupee_not_selected);
                    txtSortBySalary.setTextColor(getResources().getColor(R.color.boxOption));

                    Tlog.i("setSortByDatePosted(true)");
                } else {
                    /* De-Select on double tap */
                    jobFilterRequest.setSortByDatePosted(false);

                    /* ui manipulation */
                    imgSortByDatePosted.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_disable));
                    imgSortByDatePosted.setImageResource(R.drawable.latest_not_selected);
                    txtSortByDatePosted.setTextColor(getResources().getColor(R.color.boxOption));
                    Tlog.i("setSortByDatePosted(false)");
                }
                break;
            case R.id.ftr_sort_by_salary:
                Tlog.i("SDP"+ jobFilterRequest.getSortByDatePosted() + " SS: "+
                        jobFilterRequest.getSortBySalary());
                if (jobFilterRequest != null && !jobFilterRequest.getSortBySalary()) {
                    jobFilterRequest.setSortBySalary(true);
                    jobFilterRequest.setSortByDatePosted(false);

                     /* ui manipulation */
                    imgSortBySalary.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_enable));
                    imgSortBySalary.setImageResource(R.drawable.rupee);
                    txtSortBySalary.setTextColor(getResources().getColor(R.color.back_grey_light_item));
                    imgSortByDatePosted.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_disable));
                    imgSortByDatePosted.setImageResource(R.drawable.latest_not_selected);
                    txtSortByDatePosted.setTextColor(getResources().getColor(R.color.boxOption));

                    Tlog.i("setSortBySalary(true)");
                } else {
                    /* De-Select on double tap */
                    jobFilterRequest.setSortBySalary(false);
                   /* ui manipulation */
                    imgSortBySalary.setBackground(getResources().getDrawable(R.drawable.sort_by_circle_disable));
                    imgSortBySalary.setImageResource(R.drawable.rupee_not_selected);
                    txtSortBySalary.setTextColor(getResources().getColor(R.color.boxOption));
                    Tlog.i("setSortByDatePosted(false)");
                }
                break;
            /* SALARY */
            case R.id.ftr_salary_eight_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.EIGHT_K_PLUS) {
                    /* De-Select */
                    jobFilterRequest.clearSalary();
                    ftrSalaryEightKPlus.setBackgroundColor(Color.parseColor("#ffffff"));
                } else {
                    /* Select */
                    ftrSalaryEightKPlus.setBackgroundColor(Color.parseColor("#749cf4"));
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.EIGHT_K_PLUS);
                    Tlog.i("setSalary(JobFilterRequest.Salary.EIGHT_K_PLUS)");
                }
                break;
            case R.id.ftr_salary_ten_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.TEN_K_PLUS) {
                    jobFilterRequest.clearSalary();
                } else {
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.TEN_K_PLUS);
                    Tlog.i("setSalary(JobFilterRequest.Salary.TEN_K_PLUS)");
                }
                break;
            case R.id.ftr_salary_twelve_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.TWELVE_K_PLUS) {
                    jobFilterRequest.clearSalary();
                } else {
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.TWELVE_K_PLUS);
                    Tlog.i("setSalary(JobFilterRequest.Salary.TWELVE_K_PLUS)");
                }
                break;
            case R.id.ftr_salary_fifteen_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.FIFTEEN_K_PLUS) {
                    jobFilterRequest.clearSalary();
                } else {
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.FIFTEEN_K_PLUS);
                    Tlog.i("setSalary(JobFilterRequest.Salary.FIFTEEN_K_PLUS)");
                }
                break;
            case R.id.ftr_salary_twenty_k_plus:
                if (jobFilterRequest.getSalary() == JobFilterRequest.Salary.TWENTY_K_PLUS) {
                    jobFilterRequest.clearSalary();
                } else {
                    jobFilterRequest.setSalary(JobFilterRequest.Salary.TWENTY_K_PLUS);
                    Tlog.i("setSalary(JobFilterRequest.Salary.TWENTY_K_PLUS)");
                }
                break;

            /* EXPERIENCE */
            case R.id.ftr_experience_experienced:
                if (jobFilterRequest.getExp() == JobFilterRequest.Experience.EXPERIENCED) {
                    jobFilterRequest.clearExp();
                } else {
                    jobFilterRequest.setExp(JobFilterRequest.Experience.EXPERIENCED);
                    Tlog.i("setExp(JobFilterRequest.Experience.EXPERIENCED)");
                }
                break;
            case R.id.ftr_experience_fresher:
                if (jobFilterRequest.getExp() == JobFilterRequest.Experience.FRESHER) {
                    jobFilterRequest.clearExp();
                } else {
                    jobFilterRequest.setExp(JobFilterRequest.Experience.FRESHER);
                    Tlog.i("setExp(JobFilterRequest.Experience.FRESHER)");
                }
                break;
            /* EDUCATION */
            case R.id.ftr_edu_lt_ten:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.LT_TEN) {
                    jobFilterRequest.clearEdu();
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.LT_TEN);
                    Tlog.i("setEdu(JobFilterRequest.Education.LT_TEN)");
                }
                break;
            case R.id.ftr_edu_ten_pass:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.TEN_PASS) {
                    jobFilterRequest.clearEdu();
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.TEN_PASS);
                    Tlog.i("setEdu(JobFilterRequest.Education.TEN_PASS)");
                }
                break;
            case R.id.ftr_edu_twelve_pass:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.TWELVE_PASS) {
                    jobFilterRequest.clearEdu();
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.TWELVE_PASS);
                    Tlog.i("setEdu(JobFilterRequest.Education.TWELVE_PASS)");
                }
                break;
            case R.id.ftr_edu_ug:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.UG) {
                    jobFilterRequest.clearEdu();
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.UG);
                    Tlog.i("setEdu(JobFilterRequest.Education.UG)");
                }
                break;
            case R.id.ftr_edu_pg:
                if (jobFilterRequest.getEdu() == JobFilterRequest.Education.PG) {
                    jobFilterRequest.clearEdu();
                } else {
                    jobFilterRequest.setEdu(JobFilterRequest.Education.PG);
                    Tlog.i("setEdu(JobFilterRequest.Education.PG)");
                }
                break;
            /* GENDER */
            case R.id.ftr_gender_male:
                if (jobFilterRequest.getGender() == JobFilterRequest.Gender.MALE) {
                    jobFilterRequest.clearGender();
                } else {
                    jobFilterRequest.setGender(JobFilterRequest.Gender.MALE);
                    Tlog.i("setGender(JobFilterRequest.Gender.MALE)");
                }
                break;
            case R.id.ftr_gender_female:
                if (jobFilterRequest.getGender() == JobFilterRequest.Gender.FEMALE) {
                    jobFilterRequest.clearGender();
                } else {
                    jobFilterRequest.setGender(JobFilterRequest.Gender.FEMALE);
                    Tlog.i("setGender(JobFilterRequest.Gender.FEMALE)");
                }
                break;
            case R.id.ftr_done:
                // send data for submission
                //
                Tlog.i("filter submission triggered");
                if(jobFilterRequest.isInitialized()){
                    mFilterJobAsyncTask = new JobFilterAsyncTask();
                    mFilterJobAsyncTask.execute(jobFilterRequest.build());
                }
                break;
            case R.id.ftr_clear_all:
                jobFilterRequest.clear();
                break;
            default:
                break;
        }
    }

    private class JobFilterAsyncTask extends AsyncTask<JobFilterRequest,
            Void, JobPostResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            if (jobPostListView != null) {
                jobPostListView.setAdapter(null);
            }
            pd = new ProgressDialog(getActivity(), R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected JobPostResponse doInBackground(JobFilterRequest... params) {
            Tlog.i("http req for getFilteredJobPost...");
            return HttpRequest.getFilteredJobPosts(params[0]);
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
                if (jobPostObjectList.size() > 0) {
                    Tlog.i(jobPostObjectList.size()+" jobs received");
                    Tlog.i("DataSize: " + jobPostObjectList.size());
                    JobPostAdapter jobPostAdapter = new JobPostAdapter(getActivity(), jobPostObjectList);
                    jobPostListView = (ListView) getActivity().findViewById(R.id.jobs_list_view);
                    jobPostListView.setAdapter(jobPostAdapter);
                } else {
                    ImageView noJobsImageView = (ImageView) getActivity().findViewById(R.id.no_jobs_image);
                    noJobsImageView.setVisibility(View.VISIBLE);
                    showToast("No jobs found in your locality");
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
