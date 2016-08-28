package in.trujobs.dev.trudroid.Adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import java.util.ArrayList;

import in.trujobs.dev.trudroid.Helper.PlaceAPIHelper;
import in.trujobs.dev.trudroid.PlaceAPI;
import in.trujobs.dev.trudroid.Util.Tlog;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<Object> implements Filterable {

    ArrayList<PlaceAPIHelper> resultList;

    Context mContext;
    int mResource;

    PlaceAPI mPlaceAPI = new PlaceAPI();

    public PlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;
    }
    @Override
    public String toString(){
        return resultList.get(0).getDescription();
    }
    @Override
    public int getCount() {
        // Last item will be the footer
        return resultList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length() > 2) {
                    resultList = mPlaceAPI.autocomplete(constraint.toString());
                    ArrayList<String> suggestions = new ArrayList<>();
                    for(PlaceAPIHelper apiHelper: resultList) {
                        suggestions.add(apiHelper.getDescription());
                    }

                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                } else {
                    Toast.makeText(getContext(), "Please Select Locaity within Bengaluru.", Toast.LENGTH_SHORT);
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    Tlog.i("result: "+results.values.toString());
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}