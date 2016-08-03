package in.trujobs.dev.trudroid;

import android.Manifest;
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

        LinearLayout assessmentLayout = (LinearLayout) dialog.findViewById(R.id.assessment_dialog);
        LinearLayout referLayout = (LinearLayout) dialog.findViewById(R.id.refer_dialog);
        LinearLayout referSms = (LinearLayout) dialog.findViewById(R.id.sms_share);
        LinearLayout referWhatsapp = (LinearLayout) dialog.findViewById(R.id.whatsapp_share);

        if (category == 1) {
            assessmentLayout.setVisibility(View.VISIBLE);
            referLayout.setVisibility(View.GONE);
        } else if(category == 3) {
            assessmentLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.VISIBLE);
        } else {
            assessmentLayout.setVisibility(View.GONE);
            referLayout.setVisibility(View.GONE);
        }

        assessmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:8880007799"));
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                ctx.startActivity(callIntent);
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
