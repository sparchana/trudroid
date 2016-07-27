package in.trujobs.dev.trudroid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import in.trujobs.dev.trudroid.R;

/**
 * Created by batcoder1 on 26/7/16.
 */
public class JobRoleAdapter extends BaseAdapter {
    private Context mContext;
    private final String[] web;
    private final int[] imageId;
    private final int[] tick;

    public JobRoleAdapter(Context c,String[] web, int[] imageId, int[] tick) {
        mContext = c;
        this.imageId = imageId;
        this.web = web;
        this.tick = tick;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return web.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_view_layout, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
            ImageView imageViewTick = (ImageView)grid.findViewById(R.id.grid_image_tick);
            textView.setText(web[position]);
            imageView.setImageResource(imageId[position]);
            imageViewTick.setImageResource(tick[position]);
        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
