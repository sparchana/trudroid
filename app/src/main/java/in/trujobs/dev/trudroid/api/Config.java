package in.trujobs.dev.trudroid.api;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class Config {
    /**
     * AWS prod-like-test-server for all trujobs-dev requirements is hosted @ 52.42.8.205:80
     */
//     public static final String BASE_HTTP_URL = "http://52.42.8.205:80";
    /**
     * replace ip below with your local ip for development
     * and uncomment the line, also comment out aws prod-like-test-server ip above
     */
    public static final String BASE_HTTP_URL = "http://192.168.1.33:9000";

    public static final String URL_SIGN_UP = BASE_HTTP_URL + "/mSignUp";
    public static final String URL_LOGIN = BASE_HTTP_URL + "/mLoginSubmit";
    public static final String URL_ADD_PASSWORD = BASE_HTTP_URL + "/mAddPassword";
    public static final String URL_FORGOT_PASSWORD_SEND_OTP = BASE_HTTP_URL + "/mFindUserAndSendOtp";
    public static final String URL_APPLY_JOB = BASE_HTTP_URL + "/mApplyJob";
    public static final String URL_ALL_JOB_ROLES = BASE_HTTP_URL + "/mGetAllJobRoles";
    public static final String URL_ALL_JOB_POSTS = BASE_HTTP_URL + "/mGetAllJobPosts";
    public static final String URL_MATCHING_JOB_POSTS = BASE_HTTP_URL + "/mGetMatchingJobPosts";
    public static final String URL_ADD_HOMELOCALITY = BASE_HTTP_URL + "/mAddHomeLocality";
    public static final String URL_ADD_JOB_PREFS = BASE_HTTP_URL + "/mAddJobPref";
    public static final String URL_GET_CANDIDATE_INFO = BASE_HTTP_URL + "/mGetCandidateInformation";
    public static final String URL_JOB_POST_DETAILS = BASE_HTTP_URL + "/mGetJobPostInfo";
}
