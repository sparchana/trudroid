package in.trujobs.dev.trudroid.Helper;

/**
 * Created by zero on 5/8/16.
 */
public class PlaceAPIHelper extends LatLngAPIHelper {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String toString(){
        return this.getDescription();
    }
}
