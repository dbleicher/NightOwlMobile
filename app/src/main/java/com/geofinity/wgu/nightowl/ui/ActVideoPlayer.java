package com.geofinity.wgu.nightowl.ui;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.squareup.okhttp.OkHttpClient;

/**
 * Created by davidbleicher on 11/5/14.
 *     Done: Resolve aspect ratio issues
 *     See:  http://stackoverflow.com/a/7974200/2259418
 *
 *     TODO: Add video information to Portrait view
 *     TODO: Add Chromecast support
 */
public class ActVideoPlayer extends Activity {

    private RelativeLayout flBackground;
    private VideoView myVideoView;
    private int position = 0;
    private MaterialDialog progressDialog;
    private MediaController mediaControls;
    private int myBGColor = 0x000000;

    private String deliveryID;
    private String streamURL;
    private String standardVidURL = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // String vidAddress = "http://oc.panopto.edgesuite.net/sessions/27d15315-509d-42fc-902e-1d9e4a374e55/074368bb-caba-4982-8e6d-9c2209a2ca18-95cd916b-2f48-42a6-bc51-2e60ff7bce7d.mp4?invocationId=7bd8bbb3-af65-e411-9461-22000a94d455";

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // set the main layout of the activity
        setContentView(R.layout.act_videoplayer);

        ////////////////////////////////////////////////
        // Get the video's DeliveryID from Extras and
        // create a "fake" url.
        //
        // Use GetStreamURL to visit the fake url and
        // be redirected to the StreamURL.
        //
        // Finally use setVideoURI(Uri.parse(StreamURL));
        // to kick off the display.  Nuttin' but net!
        //
        /////////////////////////////////////////////////

        ///////////////////////////////////////////
        //  Get the URI from an implicit Intent  //
        ///////////////////////////////////////////
        try {
            streamURL = getIntent().getDataString();
            myBGColor = getResources().getColor(R.color.wguPrimaryDark);
        } catch (Exception e) {
            // No problem, try the Intent extras
        }

        //////////////////////////////////////////
        //  Get the DeliveryID from the bundle  //
        //////////////////////////////////////////
        if (streamURL == null) {
            Bundle myExtras = getIntent().getExtras();
            if (myExtras != null) {
                deliveryID = myExtras.getString("DELIVERY_ID");
                myBGColor = myExtras.getInt("BACKGROUND_COLOR");
                if (myBGColor == 0) {
                    myBGColor = getResources().getColor(R.color.wguPrimaryDark);
                }
                standardVidURL = myExtras.getString("STANDARD_VIDURL");
            }

            if (standardVidURL != null && standardVidURL.length() > 4) {
                streamURL = standardVidURL;
            } else {
                new GetStreamURL().execute();
            }
        }

        //set the media controller buttons
        if (mediaControls == null) {
            mediaControls = new MediaController(ActVideoPlayer.this);
        }

        flBackground = (RelativeLayout) findViewById(R.id.flBackground);
        flBackground.setBackgroundColor(myBGColor);

        //initialize the VideoView
        myVideoView = (VideoView) findViewById(R.id.video_view);


        // create a progress bar while the video file is loading
        progressDialog = new MaterialDialog.Builder(this)
                .title("Loading Video")
                .content("please wait...")
                .cancelable(true)
                .progress(true, 0)
                .show();

        try {
            //set the media controller in the VideoView
            myVideoView.setMediaController(mediaControls);

            //set the uri of the video to be played
            // myVideoView.setVideoURI(Uri.parse(vidAddress));

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            // e.printStackTrace();
        }

        myVideoView.requestFocus();
        //we also set an setOnPreparedListener in order to know when the video file is ready for playback
        myVideoView.setOnPreparedListener(new OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer) {
                // close the progress bar and play the video
                progressDialog.dismiss();
                //if we have a position on savedInstanceState, the video playback should start from here
                myVideoView.seekTo(position);
                if (position == 0) {
                    myVideoView.start();
                } else {
                    //if we come from a resumed activity, video playback will be paused
                    myVideoView.pause();
                }
            }
        });

        if (streamURL != null) {
            myVideoView.setVideoURI(Uri.parse(streamURL));
        }
    }


    public class GetStreamURL extends AsyncTask<Void, Void, Void> {
        private final OkHttpClient okClient = NOMApp.nlm.getOkClient();

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (streamURL != null) {
                myVideoView.setVideoURI(Uri.parse(streamURL));
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            streamURL = NOMApp.reqHelp.getPanoptoStreamUrl(deliveryID);
            return null;

        }
    }
}