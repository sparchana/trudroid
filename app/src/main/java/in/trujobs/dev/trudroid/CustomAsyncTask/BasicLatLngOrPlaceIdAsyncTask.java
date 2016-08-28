package in.trujobs.dev.trudroid.CustomAsyncTask;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.LatLngOrPlaceIdRequest;
import in.trujobs.proto.LocalityObjectResponse;

/**
 * Created by zero on 27/8/16.
 */
public class BasicLatLngOrPlaceIdAsyncTask extends AsyncTask<LatLngOrPlaceIdRequest, Void, LocalityObjectResponse> {

    @Override
    protected LocalityObjectResponse doInBackground(LatLngOrPlaceIdRequest... params) {
        return HttpRequest.getLocalityForLatLngOrPlaceId(params[0]);
    }
}
