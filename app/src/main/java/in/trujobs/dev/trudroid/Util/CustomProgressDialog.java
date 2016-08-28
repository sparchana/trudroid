package in.trujobs.dev.trudroid.Util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import in.trujobs.dev.trudroid.R;

/**
 * Created by batcoder1 on 19/8/16.
 */
public class CustomProgressDialog extends ProgressDialog {
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
        if(mAnimationDrawable!=null) mAnimationDrawable.start();
   }

    @Override
    public void dismiss() {
        super.dismiss();
        if(mAnimationDrawable!=null) mAnimationDrawable.stop();
    }
}