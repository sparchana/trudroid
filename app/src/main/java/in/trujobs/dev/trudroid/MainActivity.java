package in.trujobs.dev.trudroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.trujobs.proto.TestMessage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TestMessage.Builder pseudoTestMessage = TestMessage.newBuilder();
    }
}
