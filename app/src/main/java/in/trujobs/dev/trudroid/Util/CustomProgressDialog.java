package in.trujobs.dev.trudroid.Util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.R;

/**
 * Created by batcoder1 on 19/8/16.
 */
public class CustomProgressDialog extends ProgressDialog {
    private Toast mProgressDialogLong;
    private AnimationDrawable mAnimationDrawable;

    public static ProgressDialog get(final Context context) {
        CustomProgressDialog dialog = new CustomProgressDialog(context, R.style.SpinnerTheme);
        return dialog;
    }

    private CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.loader);
        setCancelable(false) ;
        ImageView imageView = (ImageView) findViewById(R.id.customProgressImageView);
        imageView.setBackgroundResource(R.drawable.custom_loader_animation);
        mAnimationDrawable = (AnimationDrawable) imageView.getBackground();
    }

    @Override
    public void show() {
        super.show();
        if(mAnimationDrawable!=null){
            mAnimationDrawable.start();
            mAnimationDrawable.setVisible(true, false);
            new Handler().postDelayed(getWaitRunnable(), 15000);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(mAnimationDrawable!=null){
            mAnimationDrawable.setVisible(false, false);
            mAnimationDrawable.stop();
        }
    }

    private Runnable getWaitRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                if(mAnimationDrawable.isVisible()){
                    dismiss();
                   showToast("Something went wrong. Please try again");
                }
            }
        };
    }
    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String msg) {
        try{ mProgressDialogLong.getView().isShown();     // true if visible
            mProgressDialogLong.setText(msg);
        } catch (Exception e) {         // invisible if exception
            mProgressDialogLong = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        }
        mProgressDialogLong.show();
    }
}