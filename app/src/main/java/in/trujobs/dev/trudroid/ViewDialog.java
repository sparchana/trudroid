package in.trujobs.dev.trudroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by batcoder1 on 28/7/16.
 */
public class ViewDialog {
    public void showDialog(Context ctx, String msg){
        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_alert_dialog);

        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);

        dialog.show();

    }
}
