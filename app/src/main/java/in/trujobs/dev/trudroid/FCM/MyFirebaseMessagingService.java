package in.trujobs.dev.trudroid.FCM;

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

import in.trujobs.dev.trudroid.CandidateProfileActivity;
import in.trujobs.dev.trudroid.CandidateProfileBasic;
import in.trujobs.dev.trudroid.FeedbackActivity;
import in.trujobs.dev.trudroid.InterviewTipsActivity;
import in.trujobs.dev.trudroid.JobApplicationActivity;
import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.ReferFriends;
import in.trujobs.dev.trudroid.SearchJobsActivity;
import in.trujobs.dev.trudroid.SplashScreenActivity;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.ServerConstants;

import static android.app.Notification.DEFAULT_LIGHTS;
import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        sendNotification(remoteMessage);
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(RemoteMessage messageBody) {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_SEARCH_JOBS))){ //search jobs activity
                intent = new Intent(this, SearchJobsActivity.class);
            } else if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_MY_JOBS_PENDING))){ //My Applications (pending tab)
                intent = new Intent(this, JobApplicationActivity.class);
                Prefs.defaultMyJobsTab.put(0);
            } else if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_MY_JOBS_CONFIRMED))){ //My Applications (confirmed tab tab)
                intent = new Intent(this, JobApplicationActivity.class);
                Prefs.defaultMyJobsTab.put(1);
            } else if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_MY_JOBS_COMPLETED))){ //My Applications (pending tab)
                intent = new Intent(this, JobApplicationActivity.class);
                Prefs.defaultMyJobsTab.put(2);
            } else if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_MY_PROFILE))){ //My Profile
                intent = new Intent(this, CandidateProfileActivity.class);
            } else if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_REFER))){ //refer
                intent = new Intent(this, ReferFriends.class);
            } else if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_INTERVIEW_TIPS))){ //interview tips
                intent = new Intent(this, InterviewTipsActivity.class);
            } else if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_FEEDBACK))){ //feedback
                intent = new Intent(this, FeedbackActivity.class);
            } else if(Objects.equals(messageBody.getData().get("type"), String.valueOf(ServerConstants.ANDROID_INTENT_ACTIVITY_JOB_DETAIL))){ //jobDetails page
                if(Objects.equals(messageBody.getData().get("jpId"), "null")){
                    //if job post id is null, open search job intent
                    intent = new Intent(this, SearchJobsActivity.class);

                } else{
                    Prefs.jobPostId.put(Long.valueOf(messageBody.getData().get("jpId")));
                    intent = new Intent(this, JobDetailActivity.class);
                    intent.putExtra("methodName","startActivity");

                }
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(getNotificationIcon())
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle("TruJobs.in - " + messageBody.getData().get("title"))
                    .setContentText(messageBody.getData().get("message"))
                    .setAutoCancel(true)
                    .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS)
                    .setContentIntent(pendingIntent);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        notificationManager.notify(m, notificationBuilder.build());
    }
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_notification_icon : R.drawable.launcher_icon;
    }
}