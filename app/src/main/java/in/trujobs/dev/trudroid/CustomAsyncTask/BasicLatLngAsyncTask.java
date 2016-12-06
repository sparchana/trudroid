package in.trujobs.dev.trudroid.CustomAsyncTask;

import in.trujobs.dev.trudroid.Helper.LatLngAPIHelper;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.LatLngAPI;

/**
 * Created by zero on 12/8/16.
 */
public class BasicLatLngAsyncTask extends AsyncTask<String, Void, LatLngAPIHelper> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Tlog.i("Fetching LatLng ....");
        }

        @Override
        protected LatLngAPIHelper doInBackground(String... params) {
            return LatLngAPI.getLatLngFor(params[0]);
        }

        @Override
        protected void onPostExecute(LatLngAPIHelper latLngAPIHelper) {
            super.onPostExecute(latLngAPIHelper);
        }
}
