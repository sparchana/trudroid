package in.trujobs.dev.trudroid.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.DashboardActivity;
import in.trujobs.dev.trudroid.EnterPassword;
import in.trujobs.dev.trudroid.JobPreference;
import in.trujobs.dev.trudroid.R;

/**
 * Created by batcoder1 on 27/7/16.
 */
public class DashboardOptionsAdapter extends BaseAdapter {
    String [] result;
    Context context;
    private static LayoutInflater inflater=null;
    public DashboardOptionsAdapter(DashboardActivity dashboardActivity, String[] options) {
        result = options;
        context = dashboardActivity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView tv;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.list_view_item, null);
        holder.tv=(TextView) rowView.findViewById(R.id.option_text_view);
        holder.tv.setText(result[position]);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(position == 1){
                Intent intent= new Intent(context, JobPreference.class);
                context.startActivity(intent);
            } else{
                Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();
            }
            }
        });
        return rowView;
    }
}
