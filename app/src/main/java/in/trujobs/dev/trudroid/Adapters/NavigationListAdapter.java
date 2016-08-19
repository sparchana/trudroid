package in.trujobs.dev.trudroid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import in.trujobs.dev.trudroid.NavItem;
import in.trujobs.dev.trudroid.R;

/**
 * Created by batcoder1 on 19/8/16.
 */
public class NavigationListAdapter extends ArrayAdapter<NavItem> {
    public NavigationListAdapter(Context context, List<NavItem> navigationItems) {
        super(context, 0, navigationItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_item, null);
        }

        NavItem navItem = getItem(position);

        TextView titleView = (TextView) convertView.findViewById(R.id.title);
        ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);

        titleView.setText(navItem.mTitle);
        iconView.setImageResource(navItem.mIcon);

        return convertView;
    }
}
