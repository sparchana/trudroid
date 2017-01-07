package in.trujobs.dev.trudroid.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class ServerConstants {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final int NO_USER = 3;
    public static final int WRONG_PASSWORD = 4;
    public static final int NO_AUTH = 5;

    public static final int SIGNUP_SUCCESS = 1;
    public static final int SIGNUP_EXISTS = 3;

    public static final int NO_USER_TO_SEND_OTP = 3;

    public static final int JOB_APPLY_SUCCESS = 1;
    public static final int JOB_APPLY_FAILURE = 2;
    public static final int JOB_ALREADY_APPLIED = 3;
    public static final int JOB_APPLY_NO_CANDIDATE = 4;
    public static final int JOB_APPLY_NO_JOB = 4;
    public static final int APPLICATION_LIMIT_REACHED = 5;

    public static Integer HOMELOCALITY_YES = 1;
    public static Integer JOBPREFERENCE_YES = 1;

    public static String ALL_JOBS = "All Jobs";
    public static final String ERROR_WHILE_SAVING_HOME_LOCALITY = "Something went wrong while saving home locality. Please try again later!";
    public static final String NETWORK_NOT_FOUND = "Please turn on your wifi/mobile data in order to use this feature";

    public static String CANDIDATE_STATUS_NOT_GOING = "Not Going";
    public static String CANDIDATE_STATUS_DELAYED = "Delayed";
    public static String CANDIDATE_STATUS_ON_THE_WAY = "On the way";
    public static String CANDIDATE_STATUS_REACHED = "Reached";

    public static final int CANDIDATE_STATUS_NOT_GOING_VAL = 1;
    public static final int CANDIDATE_STATUS_DELAYED_VAL = 2;
    public static final int CANDIDATE_STATUS_STARTED_VAL = 3;
    public static final int CANDIDATE_STATUS_REACHED_VAL = 4;

    public static final int CANDIDATE_STATUS_RESCHEDULED_INTERVIEW_ACCEPT = 1;
    public static final int CANDIDATE_STATUS_RESCHEDULED_INTERVIEW_REJECT = 0;

    public static final int JWF_STATUS_INTERVIEW_REJECTED_BY_RECRUITER_SUPPORT = 6;
    public static final int JWF_STATUS_INTERVIEW_REJECTED_BY_CANDIDATE = 7;
    public static final int JWF_STATUS_INTERVIEW_RESCHEDULE = 8;
    public static final int JWF_STATUS_INTERVIEW_CONFIRMED = 9;

    public static final int JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_NOT_GOING = 10;
    public static final int JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_DELAYED = 11;
    public static final int JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_STARTED = 12;
    public static final int JWF_STATUS_CANDIDATE_INTERVIEW_STATUS_REACHED = 13;

    public static final int JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_SELECTED = 14;
    public static final int JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_COMPLETE_REJECTED = 15;
    public static final int JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_NO_SHOW = 16;
    public static final int JWF_STATUS_CANDIDATE_FEEDBACK_STATUS_NOT_QUALIFIED = 17;


    /* android notification intent type */
    public static final int ANDROID_INTENT_ACTIVITY_SEARCH_JOBS = 1;
    public static final int ANDROID_INTENT_ACTIVITY_MY_JOBS_PENDING = 2;
    public static final int ANDROID_INTENT_ACTIVITY_MY_JOBS_CONFIRMED = 3;
    public static final int ANDROID_INTENT_ACTIVITY_MY_JOBS_COMPLETED = 4;
    public static final int ANDROID_INTENT_ACTIVITY_MY_PROFILE = 5;
    public static final int ANDROID_INTENT_ACTIVITY_REFER = 6;
    public static final int ANDROID_INTENT_ACTIVITY_FEEDBACK = 7;
    public static final int ANDROID_INTENT_ACTIVITY_INTERVIEW_TIPS = 8;

    //reason type
    public static final int INTERVIEW_REJECT_TYPE_REASON = 1;
    public static final int INTERVIEW_NOT_GOING_TYPE_REASON = 2;
    public static final int INTERVIEW_NOT_SELECTED_TYPE_REASON = 3;
    public static final int CANDIDATE_ETA = 4;

    /* API KEYS */
    public static final String GOOGLE_SERVER_API_KEY = "AIzaSyCKHf7GijuzKW84Ggz0fFWWHD0y9_onUhg";

}
