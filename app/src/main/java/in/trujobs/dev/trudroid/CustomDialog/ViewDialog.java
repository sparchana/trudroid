package in.trujobs.dev.trudroid.CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.trujobs.dev.trudroid.CandidateProfileActivity;
import in.trujobs.dev.trudroid.JobActivity;
import in.trujobs.dev.trudroid.R;

/**
 * Created by batcoder1 on 28/7/16.
 */
public class ViewDialog {
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
        alertImage.setImageResource(image_res);

        LinearLayout completeProfileLayout = (LinearLayout) dialog.findViewById(R.id.completeprofile_dialog);
        LinearLayout newJobsLayout = (LinearLayout) dialog.findViewById(R.id.newjob_dialog);
        LinearLayout referLayout = (LinearLayout) dialog.findViewById(R.id.refer_dialog);

        LinearLayout referSms = (LinearLayout) dialog.findViewById(R.id.sms_share);
        LinearLayout referWhatsapp = (LinearLayout) dialog.findViewById(R.id.whatsapp_share);

        if (category == 1) {
            completeProfileLayout.setVisibility(View.VISIBLE);
            newJobsLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.GONE);
        } else if (category ==2 ) {
            completeProfileLayout.setVisibility(View.GONE);
            newJobsLayout.setVisibility(View.VISIBLE);
            referLayout.setVisibility(View.GONE);
        } else if(category == 3) {
            completeProfileLayout.setVisibility(View.GONE);
            newJobsLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.VISIBLE);
        } else {
            completeProfileLayout.setVisibility(View.GONE);
            newJobsLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.GONE);
        }

        completeProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate user to edit profile screen
                Intent completeProfileIntent = new Intent(view.getContext(), CandidateProfileActivity.class);
                view.getContext().startActivity(completeProfileIntent);
            }
        });
        dialog.show();

        newJobsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate user to search jobs screen
                Intent applyJobsIntent = new Intent(view.getContext(), JobActivity.class);
                view.getContext().startActivity(applyJobsIntent);
            }
        });
        dialog.show();

        //refer whatsapp and sms buttons

        referWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                intent.putExtra(Intent.EXTRA_TEXT, "Hey. Register yourself at www.trujobs.in and get jobs");
                ctx.startActivity(intent);
            }
        });

        referSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("sms_body","Hey. Register yourself at www.trujobs.in and get jobs");
                ctx.startActivity(smsIntent);
            }
        });

    }
}
