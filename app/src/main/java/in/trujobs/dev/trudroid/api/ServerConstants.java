package in.trujobs.dev.trudroid.api;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class ServerConstants {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final int NO_USER = 3;
    public static final int WRONG_PASSWORD = 4;

    public static final int SIGNUP_SUCCESS = 1;
    public static final int SIGNUP_EXISTS = 3;

    public static final int NO_USER_TO_SEND_OTP = 3;

    public static final int JOB_APPLY_SUCCESS = 1;
    public static final int JOB_APPLY_FAILURE = 2;
    public static final int JOB_ALREADY_APPLIED = 3;
    public static final int JOB_APPLY_NO_CANDIDATE = 4;
    public static final int JOB_APPLY_NO_JOB = 4;

    public static Integer HOMELOCALITY_YES = 1;
    public static Integer JOBPREFERENCE_YES = 1;

    public static String ALL_JOBS = "All Jobs";
    public static final String ERROR_WHILE_SAVING_HOME_LOCALITY = "Something went wrong while saving home locality. Please try again later!";
    public static final String NETWORK_NOT_FOUND = "Please turn on your wifi/mobile data in order to use this feature";

    /* API KEYS */
    public static final String GOOGLE_SERVER_API_KEY = "AIzaSyCKHf7GijuzKW84Ggz0fFWWHD0y9_onUhg";

}
