package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import in.trujobs.dev.trudroid.CustomDialog.ViewDialog;
import in.trujobs.dev.trudroid.api.MessageConstants;

public class ReferFriends extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_friends);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Refer to Friends");

        ViewDialog alert = new ViewDialog();
        alert.showDialog(ReferFriends.this, MessageConstants.REFER_MESSAGE, MessageConstants.REFER_MESSAGE_TEXT, "", R.drawable.refer, 3);

        Button referSms = (Button) findViewById(R.id.refer_msg);
        Button referWhatsapp = (Button) findViewById(R.id.refer_whatsapp);
        referWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                intent.putExtra(Intent.EXTRA_TEXT, MessageConstants.REFER_MESSAGE_TEXT);
                startActivity(intent);
            }
        });

        referSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("sms_body",MessageConstants.REFER_MESSAGE_TEXT);
                startActivity(smsIntent);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
