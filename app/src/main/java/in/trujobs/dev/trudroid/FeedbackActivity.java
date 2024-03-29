package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.AddFeedbackRequest;
import in.trujobs.proto.AddFeedbackResponse;
import in.trujobs.proto.FeedbackReasonObject;
import in.trujobs.proto.FeedbackReasonResponse;

public class FeedbackActivity extends TruJobsBaseActivity {

    private TextView ratingStatus;
    private TextView reasonHeading;
    private ImageView ratingOne;
    private ImageView ratingTwo;
    private ImageView ratingThree;
    private ImageView ratingFour;
    private ImageView ratingFive;
    private Button submitFeedback;
    private EditText feedbackComment;

    private ProgressDialog pd;

    private LinearLayout feedbackReason;
    private int ratingScore = 0;

    private List<FeedbackReasonObject> feedbackReasonObjectList;
    final List<FeedbackReasonObject> selectedReasonList = new ArrayList<FeedbackReasonObject>();

    private AsyncTask<AddFeedbackRequest, Void, AddFeedbackResponse> mAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Let us know your feedback");

        ratingOne = (ImageView) findViewById(R.id.rating_1);
        ratingTwo = (ImageView) findViewById(R.id.rating_2);
        ratingThree = (ImageView) findViewById(R.id.rating_3);
        ratingFour = (ImageView) findViewById(R.id.rating_4);
        ratingFive = (ImageView) findViewById(R.id.rating_5);

        ratingOne.setBackgroundResource(R.drawable.ic_1_star_disable);
        ratingTwo.setBackgroundResource(R.drawable.ic_2_star_disable);
        ratingThree.setBackgroundResource(R.drawable.ic_3_star_disable);
        ratingFour.setBackgroundResource(R.drawable.ic_4_star_disable);
        ratingFive.setBackgroundResource(R.drawable.ic_5_star_disable);

        reasonHeading = (TextView) findViewById(R.id.reason_heading);
        ratingStatus = (TextView) findViewById(R.id.rating_status);
        ratingStatus.setVisibility(View.GONE);
        reasonHeading.setVisibility(View.GONE);

        submitFeedback = (Button) findViewById(R.id.submit_feedback);
        submitFeedback.setVisibility(View.GONE);

        feedbackReason = (LinearLayout) findViewById(R.id.feedback_reason);
        feedbackComment = (EditText) findViewById(R.id.feedback_comment);
        feedbackComment.setVisibility(View.GONE);

        //getting all the feedback reasons
        AsyncTask<Void, Void, FeedbackReasonResponse> reasonAsyncTask = new FeedbackActivity.FeedbackReasonAsyncTask();
        reasonAsyncTask.execute();

    }

    private class FeedbackReasonAsyncTask extends AsyncTask<Void,Void,in.trujobs.proto.FeedbackReasonResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(FeedbackActivity.this);
            pd.show();
        }

        @Override
        protected in.trujobs.proto.FeedbackReasonResponse doInBackground(Void... params) {
            return HttpRequest.getAllFeedbackReason();
        }

        @Override
        protected void onPostExecute(in.trujobs.proto.FeedbackReasonResponse feedbackReasonResponse) {
            super.onPostExecute(feedbackReasonResponse);
            pd.cancel();

            feedbackReasonObjectList = new ArrayList<>();

            if(feedbackReasonResponse != null){
                for(final FeedbackReasonObject feedbackReasonObject : feedbackReasonResponse.getFeedbackReasonObjectList()){
                    feedbackReasonObjectList.add(feedbackReasonObject);
                }

                ratingOne.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ratingOne.setBackgroundResource(R.drawable.ic_1_star);
                        ratingTwo.setBackgroundResource(R.drawable.ic_2_star_disable);
                        ratingThree.setBackgroundResource(R.drawable.ic_3_star_disable);
                        ratingFour.setBackgroundResource(R.drawable.ic_4_star_disable);
                        ratingFive.setBackgroundResource(R.drawable.ic_5_star_disable);
                        ratingScore = 1;
                        ratingStatus.setVisibility(View.VISIBLE);
                        String title = "<b>Very Bad</b>";
                        ratingStatus.setText(Html.fromHtml(title));

                        ratingStatus.setTextColor(getResources().getColor(R.color.colorRed));

                        reasonHeading.setVisibility(View.VISIBLE);
                        reasonHeading.setText("Sorry to hear that! Please tell us what went wrong");

                        feedbackComment.setVisibility(View.VISIBLE);
                        submitFeedback.setVisibility(View.VISIBLE);
                        populateFeedbackReasons(1);

                        return false;
                    }
                });

                ratingTwo.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ratingOne.setBackgroundResource(R.drawable.ic_1_star_disable);
                        ratingTwo.setBackgroundResource(R.drawable.ic_2_star);
                        ratingThree.setBackgroundResource(R.drawable.ic_3_star_disable);
                        ratingFour.setBackgroundResource(R.drawable.ic_4_star_disable);
                        ratingFive.setBackgroundResource(R.drawable.ic_5_star_disable);
                        ratingScore = 2;
                        ratingStatus.setVisibility(View.VISIBLE);
                        submitFeedback.setVisibility(View.VISIBLE);
                        String title = "<b>Bad</b>";
                        ratingStatus.setText(Html.fromHtml(title));

                        ratingStatus.setTextColor(getResources().getColor(R.color.colorRed));

                        reasonHeading.setVisibility(View.VISIBLE);
                        reasonHeading.setText("Sorry to hear that! Please tell us what went wrong");

                        feedbackComment.setVisibility(View.VISIBLE);
                        populateFeedbackReasons(1);

                        return false;
                    }
                });

                ratingThree.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ratingOne.setBackgroundResource(R.drawable.ic_1_star_disable);
                        ratingTwo.setBackgroundResource(R.drawable.ic_2_star_disable);
                        ratingThree.setBackgroundResource(R.drawable.ic_3_star);
                        ratingFour.setBackgroundResource(R.drawable.ic_4_star_disable);
                        ratingFive.setBackgroundResource(R.drawable.ic_5_star_disable);
                        ratingScore = 3;
                        ratingStatus.setVisibility(View.VISIBLE);
                        submitFeedback.setVisibility(View.VISIBLE);
                        String title = "<b>Average</b>";
                        ratingStatus.setText(Html.fromHtml(title));

                        ratingStatus.setTextColor(getResources().getColor(R.color.colorOrange));

                        reasonHeading.setVisibility(View.VISIBLE);
                        reasonHeading.setText("Okay! How can we improve?");
                        feedbackComment.setVisibility(View.VISIBLE);
                        populateFeedbackReasons(2);

                        return false;
                    }
                });

                ratingFour.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ratingOne.setBackgroundResource(R.drawable.ic_1_star_disable);
                        ratingTwo.setBackgroundResource(R.drawable.ic_2_star_disable);
                        ratingThree.setBackgroundResource(R.drawable.ic_3_star_disable);
                        ratingFour.setBackgroundResource(R.drawable.ic_4_star);
                        ratingFive.setBackgroundResource(R.drawable.ic_5_star_disable);
                        ratingScore = 4;
                        ratingStatus.setVisibility(View.VISIBLE);
                        submitFeedback.setVisibility(View.VISIBLE);
                        String title = "<b>Good</b>";
                        ratingStatus.setText(Html.fromHtml(title));
                        ratingStatus.setTextColor(getResources().getColor(R.color.colorGreen));

                        reasonHeading.setVisibility(View.VISIBLE);
                        reasonHeading.setText("Okay! How can we improve?");

                        feedbackComment.setVisibility(View.VISIBLE);
                        populateFeedbackReasons(2);

                        return false;
                    }
                });

                ratingFive.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ratingOne.setBackgroundResource(R.drawable.ic_1_star_disable);
                        ratingTwo.setBackgroundResource(R.drawable.ic_2_star_disable);
                        ratingThree.setBackgroundResource(R.drawable.ic_3_star_disable);
                        ratingFour.setBackgroundResource(R.drawable.ic_4_star_disable);
                        ratingFive.setBackgroundResource(R.drawable.ic_5_star);
                        ratingScore = 5;
                        ratingStatus.setVisibility(View.VISIBLE);
                        submitFeedback.setVisibility(View.VISIBLE);
                        String title = "<b>Very Good</b>";
                        ratingStatus.setText(Html.fromHtml(title));
                        ratingStatus.setTextColor(getResources().getColor(R.color.colorGreen));

                        reasonHeading.setVisibility(View.VISIBLE);
                        reasonHeading.setText("Happy to know that! What did you like?");
                        feedbackComment.setVisibility(View.VISIBLE);
                        populateFeedbackReasons(3);

                        return false;
                    }
                });

                submitFeedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(ratingScore > 0){
                            AddFeedbackRequest.Builder requestBuilder = AddFeedbackRequest.newBuilder();
                            requestBuilder.setCandidateId(Prefs.candidateId.get());
                            requestBuilder.setRatingScore(ratingScore);
                            requestBuilder.addAllFeedbackReasonObject(selectedReasonList);
                            requestBuilder.setFeedbackComment(feedbackComment.getText().toString());

                            if (mAsyncTask != null) {
                                mAsyncTask.cancel(true);
                            }
                            mAsyncTask = new FeedbackActivity.AddFeedbackAsyncTask();
                            mAsyncTask.execute(requestBuilder.build());

                        } else{
                            showToast("Please select a rating");
                        }
                    }
                });
            } else{
                showToast(MessageConstants.SOMETHING_WENT_WRONG);
                finish();
            }
        }
    }

    private class AddFeedbackAsyncTask extends AsyncTask<AddFeedbackRequest,
            Void, AddFeedbackResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected AddFeedbackResponse doInBackground(AddFeedbackRequest... params) {
            return HttpRequest.addFeedbackRequest(params[0]);
        }

        @Override
        protected void onPostExecute(AddFeedbackResponse addFeedbackResponse) {
            super.onPostExecute(addFeedbackResponse);
            mAsyncTask = null;
            pd.cancel();
            if(!Util.isConnectedToInternet(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (addFeedbackResponse == null) {
                showToast(MessageConstants.FAILED_REQUEST);
                Log.w("","Null signIn Response");
                return;
            }

            if(addFeedbackResponse.getStatusValue() == AddFeedbackResponse.Status.SUCCESS_VALUE){
                showToast("Thank you for your valuable feedback.");
                finish();
            } else {
                showToast(MessageConstants.SOMETHING_WENT_WRONG);
            }
        }
    }


    public void populateFeedbackReasons(int typeId){
        //remove all views
        feedbackReason.removeAllViews();
        selectedReasonList.clear();
        LayoutInflater inflater = null;
        for(final FeedbackReasonObject feedbackReasonObject : feedbackReasonObjectList){
            if(feedbackReasonObject.getReasonType() == typeId){

                inflater = (LayoutInflater) getApplication().getApplicationContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View mLinearView = inflater.inflate(R.layout.sample_list_view, null);
                TextView skillName = (TextView) mLinearView
                        .findViewById(R.id.list_name);
                skillName.setText(feedbackReasonObject.getReasonTitle());
                feedbackReason.addView(mLinearView);

                final CheckBox reasonCheckbox = (CheckBox) mLinearView.findViewById(R.id.list_checkbox);

                reasonCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(reasonCheckbox.isChecked()){
                            selectedReasonList.add(feedbackReasonObject);
                        } else{
                            removeReasonObject(feedbackReasonObject);
                        }
                    }
                });


            }
        }
    }

    public void removeReasonObject(FeedbackReasonObject feedbackReasonObject){
        for(int i=0; i<selectedReasonList.size(); i++){
            if(selectedReasonList.get(i).getReasonId() == feedbackReasonObject.getReasonId()){
                selectedReasonList.remove(i);
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
