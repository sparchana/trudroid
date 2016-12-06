package in.trujobs.dev.trudroid.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.trujobs.dev.trudroid.R;

/**
 * Created by batcoder1 on 13/8/16.
 */
public class SpinnerAdapter extends ArrayAdapter<String> {

    private Context ctx;
    private String[] contentArray;

    public SpinnerAdapter(Context context, int resource, String[] objects) {
        super(context, resource, R.id.spinnerTextViewQualification, objects);
        this.ctx = context;
        this.contentArray = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spinner_layout, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.spinnerTextViewQualification);
        textView.setText(contentArray[position]);
        return convertView;
    }
}

