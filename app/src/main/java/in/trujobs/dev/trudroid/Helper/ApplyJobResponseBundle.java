package in.trujobs.dev.trudroid.Helper;

import android.widget.Button;
import android.widget.ImageView;

import in.trujobs.proto.ApplyJobResponse;

/**
 * Created by zero on 26/11/16.
 */

public class ApplyJobResponseBundle {

    private Button applyingJobButton;
    private Button applyingJobButtonDetail;
    private ImageView applyingJobColor;
    private ApplyJobResponse applyJobResponse;

    public Button getApplyingJobButton() {
        return applyingJobButton;
    }

    public void setApplyingJobButton(Button applyingJobButton) {
        this.applyingJobButton = applyingJobButton;
    }

    public Button getApplyingJobButtonDetail() {
        return applyingJobButtonDetail;
    }

    public void setApplyingJobButtonDetail(Button applyingJobButtonDetail) {
        this.applyingJobButtonDetail = applyingJobButtonDetail;
    }

    public ImageView getApplyingJobColor() {
        return applyingJobColor;
    }

    public void setApplyingJobColor(ImageView applyingJobColor) {
        this.applyingJobColor = applyingJobColor;
    }

    public ApplyJobResponse getApplyJobResponse() {
        return applyJobResponse;
    }

    public void setApplyJobResponse(ApplyJobResponse applyJobResponse) {
        this.applyJobResponse = applyJobResponse;
    }
}
