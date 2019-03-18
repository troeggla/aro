package nl.cwi.dis.aro;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.media.MediaPlayer;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.VideoView;
import android.net.Uri;

import java.lang.Math;
import java.util.TimerTask;

import nl.cwi.dis.aro.views.MyRockerView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlayer extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private double angle = 0;
    private double level = 0;
    private double valence = 5;
    private double arousal = 5;
    private Context context = this;
    private String logging_string = "";
    private TextView valence_txt;
    private TextView arousal_txt;
    private ImageView emoji;
    private ImageView back_ground;
    private static final String LOG_TAG = "VideoPlayer";
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private MyRockerView mRockerViewXY;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_player);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        // 摇杆初始化
        mRockerViewXY = findViewById(R.id.rockerXY_View);
        //视频初始化
        VideoView videoView =findViewById(R.id.videoView);
        videoView.setOnClickListener(new VideoView.OnClickListener(){
            @Override
            public void onClick(View view) {
                toggle();
            }

        });
    }

    public void onResume(){
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
        final VideoView videoView =findViewById(R.id.videoView);
        final VideoView videoView_pre =findViewById(R.id.videoView_pre);
        final VideoView videoView_after =findViewById(R.id.videoView_after);
        arousal_txt = findViewById(R.id.arousal_txt);
        valence_txt = findViewById(R.id.valence_txt);
        back_ground = findViewById(R.id.imageView2);
        //arousal_txt.setVisibility(View.INVISIBLE);
        //valence_txt.setVisibility(View.INVISIBLE);
        Intent intent = getIntent();
        // get the intent
        final int videoID = intent.getIntExtra("videoID", -1);
        final String videopath = "android.resource://"+getPackageName()+"/"+(R.raw.video_1+videoID);
        final String pre_play = "android.resource://"+getPackageName()+"/"+(R.raw.pre_play);
        final String after_play = "android.resource://"+getPackageName()+"/"+(R.raw.after_play);
        videoView_pre.setVisibility(View.VISIBLE);
        videoView_after.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.INVISIBLE);
        //start the video player
        Uri uri_pre = Uri.parse(pre_play);
        videoView_pre.setVideoURI(uri_pre);
        videoView_pre.start();
        initMyClick();
        final Timer logging =new java.util.Timer(true);
        final TimerTask task = new TimerTask() {
            public void run() {
                logging_string=logging_string+valence+","+arousal+"\n";
            }
        };
        videoView_pre.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //start the video player
                videoView_pre.setVisibility(View.INVISIBLE);
                videoView.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse(videopath);
                videoView.setVideoURI(uri);
                videoView.start();
                String ss = ""+System.currentTimeMillis();
                String ss_time = ss.substring(0,ss.length() -3);
                String ss_time_m = ss.substring(ss.length() -3,ss.length() -2);
                logging_string=logging_string+ss_time+"."+ss_time_m+"\n";
                logging.schedule(task,0,100);
            }
        });
        final String user_name = intent.getStringExtra("user_name");
        final String user_age = intent.getStringExtra("user_age");
        final String user_gender = intent.getStringExtra("user_gender");
        final String annotation =intent.getStringExtra("annotation");
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //start the video player
                logging.cancel();
                String ss = ""+System.currentTimeMillis();
                String ss_time = ss.substring(0,ss.length() -3);
                String ss_time_m = ss.substring(ss.length() -3,ss.length() -2);
                logging_string=logging_string+ss_time+"."+ss_time_m;
                videoView_after.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.INVISIBLE);
                Uri uri = Uri.parse(after_play);
                videoView_after.setVideoURI(uri);
                videoView_after.start();
            }
        });
        videoView_after.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent i = new Intent(VideoPlayer.this , Annotation.class);
                String file_name = user_name+"_"+user_age+"_"+user_gender+"_"+videoID;
                this.writeDataToFile(file_name+".csv", logging_string);
                i.putExtra("annotation",annotation);
                i.putExtra("videoID", videoID);
                i.putExtra("user_name",user_name);
                i.putExtra("user_age",user_age);
                i.putExtra("user_gender",user_gender);
                startActivity(i);
            }

            private void writeDataToFile(String fname, String data) {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File dataFile = new File(downloadDir, fname);
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
                    fileOutputStream.write(data.getBytes());
                    fileOutputStream.close();
                } catch (FileNotFoundException fnf) {
                    Log.e(LOG_TAG, "File not found: " + fnf);
                } catch (IOException ioe) {
                    Log.e(LOG_TAG, "IO Exception: " + ioe);
                }
                DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
                downloadManager.addCompletedDownload(dataFile.getName(), dataFile.getName(), true, "text/plain", dataFile.getAbsolutePath(), dataFile.length(), true);
            }
        });
    }
    private void initMyClick() {
        mRockerViewXY.setOnAngleChangeListener(new MyRockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {}

            @Override
            public void angle(double angle_) {
                angle = Math.toRadians(angle_);
                valence = level * Math.cos(angle) + 5;
                arousal = -level * Math.sin(angle) + 5;

                valence = (valence > 9) ? 9 : (valence < 1) ? 1 : valence;
                arousal = (arousal > 9) ? 9 : (arousal < 1) ? 1 : arousal;

                valence_txt.setText(String.format("valence = %.1f", valence));
                arousal_txt.setText(String.format("arousal = %.1f", arousal));

                if (valence > 5) {
                    back_ground.setImageDrawable(getResources().getDrawable(R.drawable.b_p));
                } else if (valence < 5) {
                    back_ground.setImageDrawable(getResources().getDrawable(R.drawable.b_n));
                }

                emoji = findViewById(R.id.emoji_image);

                if(level > 1) {
                    if(angle < 1.57 && angle>=0) {
                        emoji.setImageDrawable(getResources().getDrawable(R.drawable.relieved));
                    } else if (angle >= 1.57 && angle < 3.14) {
                        emoji.setImageDrawable(getResources().getDrawable(R.drawable.pensive));
                    } else if (angle >= 3.14 && angle < 4.71) {
                        emoji.setImageDrawable(getResources().getDrawable(R.drawable.angry));
                    } else if (angle >= 4.71 && angle < 6.28) {
                        emoji.setImageDrawable(getResources().getDrawable(R.drawable.smile));
                    }
                }

            }

            @Override
            public void onFinish() {}
        });

        mRockerViewXY.setOnShakeListener(MyRockerView.DirectionMode.DIRECTION_8, new MyRockerView.OnShakeListener() {
            @Override
            public void onStart() {}

            @Override
            public void direction(MyRockerView.Direction direction) {
                back_ground = findViewById(R.id.imageView2);

                if (direction == MyRockerView.Direction.DIRECTION_CENTER) {
                    valence = 5;
                    arousal = 5;
                    emoji = findViewById(R.id.emoji_image);
                    emoji.setImageDrawable(getResources().getDrawable(R.drawable.neutral_face));
                }
            }

            @Override
            public void onFinish() {}
        });


        mRockerViewXY.setOnDistanceLevelListener(new MyRockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level_) {
                level = level_;
                valence =  level*Math.cos(angle)+5;
                arousal = -level*Math.sin(angle)+5;
                if (valence >9) valence=9; if(valence<1) valence=1;
                if (arousal >9) arousal=9; if (arousal <1) arousal=1;
                String arousal_ = String.format("%.1f",level);
                String valence_ = String.format("%.1f",valence);
                valence_txt.setText("valence = "+valence_);
                arousal_txt.setText("arousal = "+arousal_);
                if (valence >5) back_ground.setImageDrawable(getResources().getDrawable(R.drawable.b_p));
                else if (valence <5)back_ground.setImageDrawable(getResources().getDrawable(R.drawable.b_n));
                emoji = findViewById(R.id.emoji_image);
                if (level_<=6&&level_>5)
                {
                    adjust_box(110);
                }
                else if (level_<=5&&level_>4)
                    adjust_box(100);
                else if (level_<=4&&level_>3)
                    adjust_box(90);
                else if (level_<=3&&level_>2)
                    adjust_box(80);
                else if (level_<=2&&level_>1)
                    adjust_box(70);
                else if (level_<=1&&level_>=0)
                {
                    adjust_box(60);
                }
                else
                    adjust_box(70);
                    emoji.setImageDrawable(getResources().getDrawable(R.drawable.neutral_face));
            }
        });
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            hide();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    private int dpTopx(Context context, float dp){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    public void adjust_box(int dp){
        //emoji_top = findViewById(R.id.emoji_top);
        //emoji_left= findViewById(R.id.emoji_left);
        //emoji_right = findViewById(R.id.emoji_right);
        // = findViewById(R.id.emoji_bottom);
        //int scale = (dp-60)/2;
        ViewGroup.LayoutParams lp = emoji.getLayoutParams();
        //lp.height = dpTopx(context,290-scale);
        //emoji_top.setLayoutParams(lp);
        //lp = emoji_left.getLayoutParams();
        //lp.width = dpTopx(context,610-scale);
        //emoji_left.setLayoutParams(lp);
        //lp = emoji_bottom.getLayoutParams();
        //lp.height = dpTopx(context,260-scale);
        //emoji_bottom.setLayoutParams(lp);
        //lp = emoji_right.getLayoutParams();
        //lp.width = dpTopx(context,63-scale);
        //emoji_right.setLayoutParams(lp);
        lp.width =dpTopx(context,dp) ;
        lp.height =dpTopx(context,dp) ;
        emoji.setLayoutParams(lp);
    }
}
