package in.trujobs.dev.trudroid.api;

import android.util.Base64;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.proto.AddFeedbackRequest;
import in.trujobs.proto.AddFeedbackResponse;
import in.trujobs.proto.CandidateAppliedJobPostWorkFlowResponse;
import in.trujobs.proto.CandidateAppliedJobsRequest;
import in.trujobs.proto.FeedbackReasonResponse;
import in.trujobs.proto.GetCandidateBasicProfileStaticResponse;
import in.trujobs.proto.GetCandidateEducationProfileStaticResponse;
import in.trujobs.proto.GetCandidateExperienceProfileStaticResponse;
import in.trujobs.proto.FetchCandidateAlertRequest;
import in.trujobs.proto.FetchCandidateAlertResponse;
import in.trujobs.proto.HomeLocalityRequest;
import in.trujobs.proto.HomeLocalityResponse;
import in.trujobs.proto.AddJobRoleRequest;
import in.trujobs.proto.AddJobRoleResponse;
import in.trujobs.proto.ApplyJobRequest;
import in.trujobs.proto.ApplyJobResponse;
import in.trujobs.proto.CandidateInformationRequest;
import in.trujobs.proto.CheckInterviewSlotRequest;
import in.trujobs.proto.CheckInterviewSlotResponse;
import in.trujobs.proto.GenericResponse;
import in.trujobs.proto.GetCandidateInformationResponse;
import in.trujobs.proto.GetInterviewSlotsRequest;
import in.trujobs.proto.GetInterviewSlotsResponse;
import in.trujobs.proto.GetJobPostDetailsRequest;
import in.trujobs.proto.GetJobPostDetailsResponse;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobRoleResponse;
import in.trujobs.proto.JobSearchRequest;
import in.trujobs.proto.LatLngOrPlaceIdRequest;
import in.trujobs.proto.LocalityObjectResponse;
import in.trujobs.proto.LogInRequest;
import in.trujobs.proto.LogInResponse;
import in.trujobs.proto.PreScreenPopulateProtoRequest;
import in.trujobs.proto.PreScreenPopulateProtoResponse;
import in.trujobs.proto.LogoutCandidateRequest;
import in.trujobs.proto.LogoutCandidateResponse;
import in.trujobs.proto.NotGoingReasonResponse;
import in.trujobs.proto.ResetPasswordRequest;
import in.trujobs.proto.ResetPasswordResponse;
import in.trujobs.proto.SignUpRequest;
import in.trujobs.proto.SignUpResponse;
import in.trujobs.proto.UpdateCandidateBasicProfileRequest;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;
import in.trujobs.proto.UpdateCandidateDocumentRequest;
import in.trujobs.proto.UpdateCandidateEducationProfileRequest;
import in.trujobs.proto.UpdateCandidateExperienceProfileRequest;
import in.trujobs.proto.UpdateCandidateExperienceRequest;
import in.trujobs.proto.UpdateCandidateInterviewDetailRequest;
import in.trujobs.proto.UpdateCandidateLanguageRequest;
import in.trujobs.proto.UpdateCandidateOtherRequest;
import in.trujobs.proto.UpdateCandidateStatusRequest;
import in.trujobs.proto.UpdateCandidateStatusResponse;
import in.trujobs.proto.UpdateInterviewRequest;
import in.trujobs.proto.UpdateInterviewResponse;
import in.trujobs.proto.UpdateTokenRequest;
import in.trujobs.proto.UpdateTokenResponse;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class HttpRequest {
    public static LogInResponse loginRequest(LogInRequest logInRequest) {
        String responseString = postToServer(Config.URL_LOGIN,
                Base64.encodeToString(logInRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        LogInResponse logInResponse = null;
        try {
            logInResponse = LogInResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return logInResponse;
    }

    public static SignUpResponse signUpRequest(SignUpRequest signUpRequest) {
        String responseString = postToServer(Config.URL_SIGN_UP,
                Base64.encodeToString(signUpRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        SignUpResponse signUpResponse = null;
        try {
            signUpResponse = SignUpResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return signUpResponse;
    }

    public static LogInResponse addPassword(LogInRequest logInRequest) {
        String responseString = postToServer(Config.URL_ADD_PASSWORD,
                Base64.encodeToString(logInRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        LogInResponse logInResponse = null;
        try {
            logInResponse = LogInResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return logInResponse;
    }

    public static ResetPasswordResponse findUserAndSendOtp(ResetPasswordRequest resetPasswordRequest) {
        String responseString = postToServer(Config.URL_FORGOT_PASSWORD_SEND_OTP,
                Base64.encodeToString(resetPasswordRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        ResetPasswordResponse resetPasswordResponse = null;
        try {
            resetPasswordResponse = ResetPasswordResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return resetPasswordResponse;
    }

    public static JobRoleResponse getJobRoles() {
        JobRoleResponse.Builder requestBuilder =
                JobRoleResponse.newBuilder();

        String responseString = postToServer(Config.URL_ALL_JOB_ROLES,
                Base64.encodeToString(requestBuilder.build().toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        JobRoleResponse jobRoleResponse = null;
        try {
            jobRoleResponse =
                    jobRoleResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException ignored) {}

        if (jobRoleResponse != null && jobRoleResponse.getJobRoleCount() != 0) {
            return jobRoleResponse;
        } else {
            return null;
        }
    }

    public static NotGoingReasonResponse getAllNotGoingReason() {
        NotGoingReasonResponse.Builder requestBuilder =
                NotGoingReasonResponse.newBuilder();

        String responseString = postToServer(Config.URL_ALL_NOT_GOING_REASON,
                Base64.encodeToString(requestBuilder.build().toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        NotGoingReasonResponse notGoingReasonResponse = null;
        try {
            notGoingReasonResponse =
                    notGoingReasonResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException ignored) {}

        if (notGoingReasonResponse != null && notGoingReasonResponse.getReasonObjectCount() != 0) {
            return notGoingReasonResponse;
        } else {
            return null;
        }
    }

    public static FeedbackReasonResponse getAllFeedbackReason() {
        FeedbackReasonResponse.Builder requestBuilder =
                FeedbackReasonResponse.newBuilder();

        String responseString = postToServer(Config.URL_ALL_FEEDBACK_REASON,
                Base64.encodeToString(requestBuilder.build().toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        FeedbackReasonResponse feedbackReasonResponse = null;
        try {
            feedbackReasonResponse =
                    feedbackReasonResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException ignored) {}

        if (feedbackReasonResponse != null && feedbackReasonResponse.getFeedbackReasonObjectCount() != 0) {
            return feedbackReasonResponse;
        } else {
            return null;
        }
    }

    public static JobPostResponse searchJobs(JobSearchRequest jobSearchRequest) {

        String responseString;
            responseString = postToServer(Config.URL_JOB_SEARCH,
                    Base64.encodeToString(jobSearchRequest.toByteArray(), Base64.DEFAULT));


        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        JobPostResponse jobPostResponse = null;
        try {
            jobPostResponse =
                    jobPostResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {}


        return jobPostResponse;
    }

    public static ApplyJobResponse applyJob(ApplyJobRequest applyJobRequest) {
        String responseString = postToServer(Config.URL_APPLY_JOB,
                Base64.encodeToString(applyJobRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        ApplyJobResponse applyJobResponse = null;
        try {
            applyJobResponse = ApplyJobResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return applyJobResponse;
    }

    public static AddJobRoleResponse addJobPrefs(AddJobRoleRequest addJobRoleRequest) {
        String responseString = postToServer(Config.URL_ADD_JOB_PREFS,
                Base64.encodeToString(addJobRoleRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        AddJobRoleResponse addJobRoleResponse = null;
        try {
            addJobRoleResponse = AddJobRoleResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return addJobRoleResponse;
    }

    public static GetCandidateInformationResponse getCandidateInfo(CandidateInformationRequest candidateInformationRequest){
        String responseString = postToServer(Config.URL_GET_CANDIDATE_INFO,
                Base64.encodeToString(candidateInformationRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GetCandidateInformationResponse getCandidateInformationResponse = null;
        try {
            getCandidateInformationResponse =
                    getCandidateInformationResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {}

        if (getCandidateInformationResponse != null) {
            return getCandidateInformationResponse;
        } else {
            return null;
        }
    }

    public static GetJobPostDetailsResponse getJobPostDetails(GetJobPostDetailsRequest getJobPostDetailsRequest) {
        String responseString = postToServer(Config.URL_JOB_POST_DETAILS,
                Base64.encodeToString(getJobPostDetailsRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GetJobPostDetailsResponse getJobPostDetailsResponse = null;
        try {
            getJobPostDetailsResponse = GetJobPostDetailsResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return getJobPostDetailsResponse;
    }

    public static CandidateAppliedJobPostWorkFlowResponse getMyJobs(CandidateAppliedJobsRequest candidateAppliedJobsRequest) {
        String responseString = postToServer(Config.URL_CANDIDATE_APPLIED_JOBS,
                Base64.encodeToString(candidateAppliedJobsRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        CandidateAppliedJobPostWorkFlowResponse candidateAppliedJobPostWorkFlowResponse = null;
        try {
            candidateAppliedJobPostWorkFlowResponse = CandidateAppliedJobPostWorkFlowResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return candidateAppliedJobPostWorkFlowResponse;
    }

    public static UpdateInterviewResponse updateInterview(UpdateInterviewRequest updateInterviewRequest) {
        String responseString = postToServer(Config.URL_UPDATE_INTERVIEW,
                Base64.encodeToString(updateInterviewRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        UpdateInterviewResponse updateInterviewResponse = null;
        try {
            updateInterviewResponse = UpdateInterviewResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return updateInterviewResponse;
    }

    public static UpdateCandidateStatusResponse updateCandidateStatus(UpdateCandidateStatusRequest updateCandidateStatusRequest) {
        String responseString = postToServer(Config.URL_UPDATE_CANDIDATE_STATUS,
                Base64.encodeToString(updateCandidateStatusRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        UpdateCandidateStatusResponse updateCandidateStatusResponse = null;
        try {
            updateCandidateStatusResponse = UpdateCandidateStatusResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return updateCandidateStatusResponse;
    }

    public static GetCandidateBasicProfileStaticResponse getCandidateBasicProfileStatic() {
        GetCandidateBasicProfileStaticResponse.Builder basicStaticBuilder =
                GetCandidateBasicProfileStaticResponse.newBuilder();

        String responseString;
        responseString = postToServer(Config.URL_ALL_BASIC_PROFILE_STATIC,
                Base64.encodeToString(basicStaticBuilder.build().toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GetCandidateBasicProfileStaticResponse getCandidateBasicProfileStaticResponse = null;
        try {
            getCandidateBasicProfileStaticResponse =
                    getCandidateBasicProfileStaticResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {}
        return getCandidateBasicProfileStaticResponse;
    }

    public static GetCandidateExperienceProfileStaticResponse getCandidateExperienceProfileStatic() {
        GetCandidateExperienceProfileStaticResponse.Builder basicStaticBuilder =
                GetCandidateExperienceProfileStaticResponse.newBuilder();

        String responseString;
        responseString = postToServer(Config.URL_ALL_EXPERIENCE_PROFILE_STATIC + "/" + Prefs.jobPrefString.get(),
                Base64.encodeToString(basicStaticBuilder.build().toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GetCandidateExperienceProfileStaticResponse getCandidateExperienceProfileStaticResponse = null;
        try {
            getCandidateExperienceProfileStaticResponse =
                    getCandidateExperienceProfileStaticResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {}
        return getCandidateExperienceProfileStaticResponse;
    }

    public static GetCandidateEducationProfileStaticResponse getCandidateEducationProfileStatic() {
        GetCandidateEducationProfileStaticResponse.Builder educationStaticBuilder =
                GetCandidateEducationProfileStaticResponse.newBuilder();

        String responseString;
        responseString = postToServer(Config.URL_ALL_EDUCATION_PROFILE_STATIC,
                Base64.encodeToString(educationStaticBuilder.build().toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GetCandidateEducationProfileStaticResponse getCandidateEducationProfileStaticResponse = null;
        try {
            getCandidateEducationProfileStaticResponse =
                    getCandidateEducationProfileStaticResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {}
        return getCandidateEducationProfileStaticResponse;
    }

    public static UpdateCandidateBasicProfileResponse updateCandidateBasicProfile(UpdateCandidateBasicProfileRequest updateCandidateBasicProfileRequest) {
        String responseString = postToServer(Config.URL_UPDATE_BASIC_PROFILE,
                Base64.encodeToString(updateCandidateBasicProfileRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        UpdateCandidateBasicProfileResponse updateCandidateBasicProfileResponse = null;
        try {
            updateCandidateBasicProfileResponse = UpdateCandidateBasicProfileResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return updateCandidateBasicProfileResponse;
    }

    public static UpdateCandidateBasicProfileResponse updateCandidateExperienceProfile(UpdateCandidateExperienceProfileRequest updateCandidateExperienceProfileRequest) {
        String responseString = postToServer(Config.URL_UPDATE_EXPERIENCE_PROFILE,
                Base64.encodeToString(updateCandidateExperienceProfileRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        UpdateCandidateBasicProfileResponse updateCandidateBasicProfileResponse = null;
        try {
            updateCandidateBasicProfileResponse = UpdateCandidateBasicProfileResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }

        return updateCandidateBasicProfileResponse;
    }

    public static UpdateCandidateBasicProfileResponse updateCandidateEducationProfile(UpdateCandidateEducationProfileRequest updateCandidateEducationProfileRequest) {
        String responseString = postToServer(Config.URL_UPDATE_EDUCATION_PROFILE,
                Base64.encodeToString(updateCandidateEducationProfileRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        UpdateCandidateBasicProfileResponse updateCandidateBasicProfileResponse = null;
        try {
            updateCandidateBasicProfileResponse = UpdateCandidateBasicProfileResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return updateCandidateBasicProfileResponse;
    }

    public static String postToServer(String requestUrl, String request) {
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            Tlog.e(String.valueOf(e), "URL is malformed " + requestUrl);
            return "";
        }

        InputStream is = null;

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(12000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);

            conn.setRequestProperty("Content-Type", "text/plain");

            byte[] outputInBytes = request.getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(outputInBytes);
            os.close();
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            String contentAsString = readIt(is);
            return contentAsString;
        } catch (ProtocolException e) {
            Tlog.e(String.valueOf(e), "Exception");
        } catch (IOException e) {
            Tlog.e(String.valueOf(e), "Exception");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Tlog.e(String.valueOf(e), "Exception");
            }
        }
        return "";
    }

    public static String getFromServer(String requestUrl) {
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            Tlog.e(String.valueOf(e), "URL is malformed " + requestUrl);
            return "";
        }

        InputStream is = null;

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(12000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.setRequestProperty("Content-Type", "application/json");
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            return contentAsString;

        } catch (ProtocolException e) {
            Tlog.e(String.valueOf(e), "Exception");
        } catch (IOException e) {
            Tlog.e(String.valueOf(e), "Exception");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Tlog.e(String.valueOf(e), "Exception");
            }
        }
        return "";
    }

    public static String readIt(InputStream stream)
            throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }

        return sb.toString();
    }

    public static HomeLocalityResponse addHomeLocality(HomeLocalityRequest homeLocalityRequest) {
        String responseString = postToServer(Config.URL_ADD_HOMELOCALITY,
                Base64.encodeToString(homeLocalityRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        HomeLocalityResponse homeLocalityResponse = null;
        try {
            homeLocalityResponse = HomeLocalityResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return homeLocalityResponse;
    }

    public static FetchCandidateAlertResponse fetchCandidateAlert(FetchCandidateAlertRequest candidateAlertRequest) {
        String responseString = postToServer(Config.URL_CANDIDATE_STATUS_SPECIFIC_ALERT,
                Base64.encodeToString(candidateAlertRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        FetchCandidateAlertResponse candidateAlertResponse = null;
        try {
            candidateAlertResponse = FetchCandidateAlertResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return candidateAlertResponse;

    }

    public static JobPostResponse getJobsForLatLng(JobSearchRequest jobSearchRequest) {
        String responseString = postToServer(Config.URL_JOB_SEARCH,
                Base64.encodeToString(jobSearchRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        JobPostResponse jobPostResponse = null;
        try {
            jobPostResponse = JobPostResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return jobPostResponse;
    }

    public static ResetPasswordResponse resendOtp(ResetPasswordRequest resetPasswordRequest) {
        String responseString = postToServer(Config.URL_RESEND_OTP,
                Base64.encodeToString(resetPasswordRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        ResetPasswordResponse resetPasswordResponse = null;
        try {
            resetPasswordResponse = ResetPasswordResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return resetPasswordResponse;
    }

    public static LocalityObjectResponse getLocalityForLatLngOrPlaceId(LatLngOrPlaceIdRequest latLngOrPlaceIdRequest) {
        String responseString = postToServer(Config.URL_GET_LOCALITY_FOR_LATLNG,
                Base64.encodeToString(latLngOrPlaceIdRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        LocalityObjectResponse localityObjectResponse = null;
        try {
            localityObjectResponse = LocalityObjectResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return localityObjectResponse;
    }

    public static PreScreenPopulateProtoResponse getJobPostVsCandidate(PreScreenPopulateProtoRequest request) {
        String responseString = postToServer(Config.URL_GET_JOB_POST_VS_CANDIDATE,
                Base64.encodeToString(request.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        PreScreenPopulateProtoResponse preScreenPopulateResponse = null;
        try {
            preScreenPopulateResponse = PreScreenPopulateProtoResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return preScreenPopulateResponse;
    }

    public static GenericResponse updateCandidateDocument(UpdateCandidateDocumentRequest updateCandidateDocumentRequest) {
        String responseString = postToServer(Config.URL_UPDATE_CANDIDATE_PRE_SCREEN_DOCUMENT,
                Base64.encodeToString(updateCandidateDocumentRequest.toByteArray(), Base64.DEFAULT));


        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GenericResponse response = null;
        try {
            response = GenericResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return response;
    }
    public static GenericResponse updateCandidateLanguage(UpdateCandidateLanguageRequest param) {
        String responseString = postToServer(Config.URL_UPDATE_CANDIDATE_PRE_SCREEN_LANGUAGE,
                Base64.encodeToString(param.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GenericResponse response = null;
        try {
            response = GenericResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return response;
    }

    public static GenericResponse updateCandidateExperience(UpdateCandidateExperienceRequest param) {
        String responseString = postToServer(Config.URL_UPDATE_CANDIDATE_PRE_SCREEN_EXPERIENCE,
                Base64.encodeToString(param.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GenericResponse response = null;
        try {
            response = GenericResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return response;
    }

    public static GenericResponse updateCandidateOther(UpdateCandidateOtherRequest param) {
        String responseString = postToServer(Config.URL_UPDATE_CANDIDATE_PRE_SCREEN_OTHERS,
                Base64.encodeToString(param.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GenericResponse response = null;
        try {
            response = GenericResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return response;
    }
    public static CheckInterviewSlotResponse checkInterviewSlot(CheckInterviewSlotRequest param) {
        String responseString = postToServer(Config.URL_CHECK_INTERVIEW_SLOT_AVALABILITY,
                Base64.encodeToString(param.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        CheckInterviewSlotResponse response = null;
        try {
            response = CheckInterviewSlotResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return response;
    }

    public static GenericResponse updateCandidateInterviewDetail(UpdateCandidateInterviewDetailRequest param) {
        String responseString = postToServer(Config.URL_UPDATE_CANDIDATE_INTERVIEW_DETAIL,
                Base64.encodeToString(param.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GenericResponse response = null;
        try {
            response = GenericResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return response;
    }

    public static GetInterviewSlotsResponse getInterviewSlots(GetInterviewSlotsRequest param) {
        String responseString = postToServer(Config.URL_UPDATE_GET_INTERVIEW_SLOTS,
                Base64.encodeToString(param.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        GetInterviewSlotsResponse response = null;
        try {
            response = GetInterviewSlotsResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return response;
    }

    public static UpdateTokenResponse updateTokenRequest(UpdateTokenRequest updateTokenRequest) {
        String responseString = postToServer(Config.URL_UPDATE_CANDIDATE_TOKEN,
                Base64.encodeToString(updateTokenRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        UpdateTokenResponse updateTokenResponse = null;
        try {
            updateTokenResponse = UpdateTokenResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return updateTokenResponse;
    }

    public static LogoutCandidateResponse logoutCandidate(LogoutCandidateRequest logoutCandidateRequest) {
        String responseString = postToServer(Config.URL_LOGOUT_CANDIDATE,
                Base64.encodeToString(logoutCandidateRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        LogoutCandidateResponse logoutCandidateResponse = null;
        try {
            logoutCandidateResponse = LogoutCandidateResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return logoutCandidateResponse;
    }

    public static AddFeedbackResponse addFeedbackRequest(AddFeedbackRequest addFeedbackRequest) {
        String responseString = postToServer(Config.URL_ADD_FEEDBACK,
                Base64.encodeToString(addFeedbackRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        AddFeedbackResponse addFeedbackResponse = null;
        try {
            addFeedbackResponse = AddFeedbackResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Tlog.w(String.valueOf(e), "Cannot parse response");
        }
        return addFeedbackResponse;
    }
}
