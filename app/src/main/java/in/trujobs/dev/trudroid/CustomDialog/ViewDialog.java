package in.trujobs.dev.trudroid.CustomDialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.trujobs.dev.trudroid.CandidateProfileActivity;
import in.trujobs.dev.trudroid.MyAppliedJobs;
import in.trujobs.dev.trudroid.SearchJobsActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.api.MessageConstants;

/**
 * Created by batcoder1 on 28/7/16.
 */
public class ViewDialog {
    private Integer PERMISSIONS_REQUEST_CALL_PHONE = 1;

    public void showDialog(final Context ctx, String msg, String heading, String subHeading, int image_res, int category) {

        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_alert_dialog);

        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        TextView headOne = (TextView) dialog.findViewById(R.id.heading_one);
        TextView headTwo = (TextView) dialog.findViewById(R.id.heading_two);

        text.setText(msg);

        if (subHeading == "") {
            headTwo.setVisibility(View.GONE);
        }

        headOne.setText(heading);
        headTwo.setText(subHeading);

        ImageView alertImage = (ImageView) dialog.findViewById(R.id.alert_image_view);
        ImageView closeDialog = (ImageView) dialog.findViewById(R.id.close_dialog);
        alertImage.setImageResource(image_res);

        LinearLayout completeProfileLayout = (LinearLayout) dialog.findViewById(R.id.completeprofile_dialog);
        LinearLayout newJobsLayout = (LinearLayout) dialog.findViewById(R.id.newjob_dialog);
        LinearLayout assessmentLayout = (LinearLayout) dialog.findViewById(R.id.completeassessment_dialog);
        LinearLayout referLayout = (LinearLayout) dialog.findViewById(R.id.refer_dialog);
        LinearLayout viewJobsLayout = (LinearLayout) dialog.findViewById(R.id.view_my_jobs);

        LinearLayout referSms = (LinearLayout) dialog.findViewById(R.id.sms_share);
        LinearLayout referWhatsapp = (LinearLayout) dialog.findViewById(R.id.whatsapp_share);

        if (category == 1) { //complete profile now
            completeProfileLayout.setVisibility(View.VISIBLE);
            newJobsLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.GONE);
            assessmentLayout.setVisibility(View.GONE);
            viewJobsLayout.setVisibility(View.GONE);
        }
        else if (category == 2 ) { // new jobs, apply now
            completeProfileLayout.setVisibility(View.GONE);
            newJobsLayout.setVisibility(View.VISIBLE);
            referLayout.setVisibility(View.GONE);
            assessmentLayout.setVisibility(View.GONE);
            viewJobsLayout.setVisibility(View.GONE);
        }
        else if(category == 3) { //refer now
            completeProfileLayout.setVisibility(View.GONE);
            newJobsLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.VISIBLE);
            assessmentLayout.setVisibility(View.GONE);
            viewJobsLayout.setVisibility(View.GONE);
        }
        else if(category == 4) { // complete assessment now
            completeProfileLayout.setVisibility(View.GONE);
            newJobsLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.GONE);
            assessmentLayout.setVisibility(View.VISIBLE);
            viewJobsLayout.setVisibility(View.GONE);
        }

        else if(category == 5) { // View my jobs
            completeProfileLayout.setVisibility(View.GONE);
            newJobsLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.GONE);
            assessmentLayout.setVisibility(View.GONE);
            viewJobsLayout.setVisibility(View.VISIBLE);
        }
        else {
            completeProfileLayout.setVisibility(View.GONE);
            newJobsLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.GONE);
            assessmentLayout.setVisibility(View.GONE);
            viewJobsLayout.setVisibility(View.GONE);
        }

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        viewJobsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate user to my jobs section
                Intent completeProfileIntent = new Intent(view.getContext(), MyAppliedJobs.class);
                view.getContext().startActivity(completeProfileIntent);
                dialog.cancel();
            }
        });

        completeProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate user to edit profile screen
                Intent completeProfileIntent = new Intent(view.getContext(), CandidateProfileActivity.class);
                view.getContext().startActivity(completeProfileIntent);
                dialog.cancel();
            }
        });

        newJobsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate user to search jobs screen
                Intent applyJobsIntent = new Intent(view.getContext(), SearchJobsActivity.class);
                view.getContext().startActivity(applyJobsIntent);
                dialog.cancel();
            }
        });

        assessmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:8880007799"));
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ctx,
                            new String[]{Manifest.permission.CALL_PHONE},
                            PERMISSIONS_REQUEST_CALL_PHONE);
                }
                ctx.startActivity(callIntent);
                dialog.cancel();
            }
        });

        //refer whatsapp and sms buttons

        referWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                intent.putExtra(Intent.EXTRA_TEXT, MessageConstants.REFER_MESSAGE_TEXT);
                ctx.startActivity(intent);
                dialog.cancel();
            }
        });

        referSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("sms_body", MessageConstants.REFER_MESSAGE_TEXT);
                ctx.startActivity(smsIntent);
                dialog.cancel();
            }
        });

        dialog.show();
    }
}
