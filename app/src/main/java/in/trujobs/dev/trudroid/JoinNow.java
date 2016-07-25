package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class JoinNow extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_now);

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.linear_layout_join_now);

        Button buttonLogin = (Button) findViewById(R.id.login_now_btn);
        Button buttonJoinNow = (Button) findViewById(R.id.join_now_btn);

        relativeLayout.setBackgroundResource(R.drawable.join_now_background);
        AnimationDrawable backgroundAnimation = (AnimationDrawable) relativeLayout.getBackground();
        backgroundAnimation.start();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(JoinNow.this, Login.class);
                startActivity(intent);
            }
        });

        buttonJoinNow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(JoinNow.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
}
