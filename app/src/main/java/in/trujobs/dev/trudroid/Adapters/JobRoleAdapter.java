package in.trujobs.dev.trudroid.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.proto.JobPost;
import in.trujobs.proto.JobRole;

/**
 * Created by batcoder1 on 26/7/16.
 */
public class JobRoleAdapter extends ArrayAdapter<JobRole> {

    public JobRoleAdapter(Context context, List<JobRole> jobRoleList) {
        super(context, 0, jobRoleList);
    }

    public class Holder
    {
        TextView jobRoleTextView;
        ImageView jobRoleImageView, imageViewTick;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        final JobRole jobRole = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_view_layout, parent, false);
        }

        Log.e("jobPreference: ", "jobRole: " + jobRole.getJobRoleName());

        holder.jobRoleTextView = (TextView) convertView.findViewById(R.id.grid_text);
        holder.jobRoleTextView.setText(jobRole.getJobRoleName());

        holder.jobRoleImageView = (ImageView) convertView.findViewById(R.id.grid_image);
        holder.jobRoleImageView.setImageResource(R.drawable.job_apply);

        holder.imageViewTick = (ImageView) convertView.findViewById(R.id.grid_image_tick);
        holder.imageViewTick.setImageResource(R.drawable.trans);

        return convertView ;
    }
}
