package in.trujobs.dev.trudroid.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import in.trujobs.dev.trudroid.R;
import in.trujobs.proto.JobRoleObject;

/**
 * Created by batcoder1 on 26/7/16.
 */
public class JobRoleAdapter extends ArrayAdapter<JobRoleObject> {

    public JobRoleAdapter(Context context, List<JobRoleObject> jobRoleList) {
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
        final JobRoleObject jobRole = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_view_layout, parent, false);
        }

        Log.e("jobPreference: ", "jobRole: " + jobRole.getJobRoleName());

        holder.jobRoleTextView = (TextView) convertView.findViewById(R.id.grid_text);
        holder.jobRoleTextView.setText(jobRole.getJobRoleName());

        holder.jobRoleImageView = (ImageView) convertView.findViewById(R.id.grid_image);
        Picasso.with(getContext()).load(jobRole.getJobRoleIcon()).into(holder.jobRoleImageView);

        holder.imageViewTick = (ImageView) convertView.findViewById(R.id.grid_image_tick);
        holder.imageViewTick.setImageResource(R.drawable.trans);

        return convertView ;
    }
}
