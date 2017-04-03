package android.software.com.e_learning;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;
import android.os.PowerManager;


public class VidActivity extends Activity {

//    protected PowerManager.WakeLock mWakeLock;

    String portAddress;
    VideoView vidView;
    int pos;
    ProgressDialog pDialog;
    Intent intent;
    Bundle bundle;
    String videoPath;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        portAddress = getResources().getString(R.string.portAddress);
        intent = getIntent();
        bundle = intent.getExtras();
        videoPath = bundle.getString("videopath");
       // pos = vidView.getCurrentPosition();
        Log.e("STATUS", "OSIS,vposition = " + String.valueOf(pos));
        vidView.pause();
        outState.putInt("position", pos);
        //Log.d("SAVED-POS", String.valueOf(pos));
        Log.e("port address",portAddress);
        Log.e("videoPath",videoPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        portAddress = getResources().getString(R.string.portAddress);
        intent = getIntent();
        bundle = intent.getExtras();
        videoPath = bundle.getString("videopath");
//        int postn = savedInstanceState.getInt("position");
//        Log.e("STATUS","ORIS,vposition = " + String.valueOf(postn));
        pos = savedInstanceState.getInt("position");
        Log.e("STATUS","ORIS,vposition = " + String.valueOf(pos));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        portAddress = getResources().getString(R.string.portAddress);
        intent = getIntent();
        bundle = intent.getExtras();
        videoPath = bundle.getString("videopath");
        Log.e("on Create","called");

        if ( savedInstanceState != null ){
            pos = savedInstanceState.getInt("position");
            Log.e("pos",String.valueOf(pos));

        }
        setContentView(R.layout.activity_vid);
        //try commenting this out while making orientation landscape
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();

//        // Create a progressbar
        pDialog = new ProgressDialog(VidActivity.this);
        pDialog.setTitle("Please Wait For A While");
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();


        vidView = (VideoView) findViewById(R.id.Video);
        String videoURL = portAddress + videoPath;
        Uri videoUri = Uri.parse(videoURL);
        Log.e("here", videoURL);
        vidView.setVideoURI(videoUri);
        try {
            MediaController vidControl = new MediaController(this);
            vidControl.setAnchorView(vidView);
            vidView.setMediaController(vidControl);


        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        vidView.requestFocus();
        vidView.start();


        vidView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                mp.start();

                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int arg1, int arg2) {

                        Log.e("here1", "Changed");

                        mp.start();
                    }
                });

            }

        });

    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(String.valueOf(event.getKeyCode())=="4"){
            finish();
            Log.e("key pressed", String.valueOf(event.getKeyCode()));
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e("here2", "onPause called");
      //  vidView.pause();
        pDialog.dismiss();
        pos=vidView.getCurrentPosition();
        Log.e("PAUSE-POS", String.valueOf(pos) + this.toString());
    }


    @Override
    public void onResume() {
        super.onResume();
        portAddress = getResources().getString(R.string.portAddress);
        intent = getIntent();
        bundle = intent.getExtras();
        videoPath = bundle.getString("videopath");
        Log.e("here3", "onResume called");
        pDialog.show();
        vidView.seekTo(pos);
        Log.e("RESUME-POS", String.valueOf(pos) + this.toString());
        vidView.start();
       // pDialog.dismiss();
    }




}

