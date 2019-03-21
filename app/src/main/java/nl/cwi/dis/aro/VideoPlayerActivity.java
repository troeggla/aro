package nl.cwi.dis.aro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

import java.io.File;
import java.util.Locale;
import java.util.Timer;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.VideoView;
import android.net.Uri;

import java.lang.Math;
import java.util.TimerTask;

import nl.cwi.dis.aro.extras.UserSession;
import nl.cwi.dis.aro.views.MyRockerView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlayerActivity extends AppCompatActivity {
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
    private TextView valence_txt;
    private TextView arousal_txt;
    private ImageView emoji;
    private ImageView back_ground;
    private MyRockerView mRockerViewXY;

    private static final String LOG_TAG = "VideoPlayerActivity";

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
    private final Runnable mHideRunnable = this::hide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_player);

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mRockerViewXY = findViewById(R.id.rockerXY_View);

        VideoView videoView = findViewById(R.id.videoView);
        videoView.setOnClickListener(view -> hide());
    }

    public void onResume(){
        super.onResume();

        final VideoView videoView = findViewById(R.id.videoView);
        final VideoView videoView_pre = findViewById(R.id.videoView_pre);
        final VideoView videoView_after = findViewById(R.id.videoView_after);

        arousal_txt = findViewById(R.id.arousal_txt);
        valence_txt = findViewById(R.id.valence_txt);
        back_ground = findViewById(R.id.imageView2);

        Intent intent = getIntent();
        final UserSession session = intent.getParcelableExtra("session");

        final String videoPath = session.getCurrentVideoPath();
        Log.d(LOG_TAG, "Current video path: " + videoPath);

        final String pre_play = "android.resource://" + getPackageName() + "/" + (R.raw.pre_play);
        final String after_play = "android.resource://" + getPackageName() + "/" + (R.raw.after_play);

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
                session.addAnnotation(arousal, valence);
            }
        };

        videoView_pre.setOnCompletionListener(mp -> {
            //start the video player
            videoView_pre.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);

            Uri uri = Uri.parse(videoPath);
            videoView.setVideoURI(uri);
            videoView.start();

            logging.schedule(task,0,100);
        });

        videoView.setOnCompletionListener(mp -> {
            //start the video player
            logging.cancel();

            videoView_after.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);

            Uri uri = Uri.parse(after_play);
            videoView_after.setVideoURI(uri);
            videoView_after.start();
        });

        videoView_after.setOnCompletionListener(mp -> {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            session.writeResponsesToFile(downloadDir);

            Intent annotationIntent = new Intent(VideoPlayerActivity.this, AnnotationActivity.class);
            annotationIntent.putExtra("session", session);

            startActivity(annotationIntent);
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

                valence_txt.setText(String.format(Locale.ENGLISH, "valence = %.1f", valence));
                arousal_txt.setText(String.format(Locale.ENGLISH, "arousal = %.1f", arousal));

                if (valence > 5) {
                    back_ground.setImageDrawable(getDrawable(R.drawable.b_p));
                } else if (valence < 5) {
                    back_ground.setImageDrawable(getDrawable(R.drawable.b_n));
                }

                emoji = findViewById(R.id.emoji_image);

                if(level > 1) {
                    if(angle < 1.57 && angle>=0) {
                        emoji.setImageDrawable(getDrawable(R.drawable.relieved));
                    } else if (angle >= 1.57 && angle < 3.14) {
                        emoji.setImageDrawable(getDrawable(R.drawable.pensive));
                    } else if (angle >= 3.14 && angle < 4.71) {
                        emoji.setImageDrawable(getDrawable(R.drawable.angry));
                    } else if (angle >= 4.71 && angle < 6.28) {
                        emoji.setImageDrawable(getDrawable(R.drawable.smile));
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
                    emoji.setImageDrawable(getDrawable(R.drawable.neutral_face));
                }
            }

            @Override
            public void onFinish() {}
        });

        mRockerViewXY.setOnDistanceLevelListener(level_ -> {
            level = level_;

            valence = level * Math.cos(angle) + 5;
            arousal = -level * Math.sin(angle) + 5;

            valence = (valence > 9) ? 9 : (valence < 1) ? 1 : valence;
            arousal = (arousal > 9) ? 9 : (arousal < 1) ? 1 : arousal;

            valence_txt.setText(String.format(Locale.ENGLISH, "%.1f", valence));
            arousal_txt.setText(String.format(Locale.ENGLISH, "%.1f", arousal));

            if (valence > 5) {
                back_ground.setImageDrawable(getDrawable(R.drawable.b_p));
            } else if (valence < 5) {
                back_ground.setImageDrawable(getDrawable(R.drawable.b_n));
            }

            emoji = findViewById(R.id.emoji_image);

            if (level_ <= 6 && level_ > 5) {
                adjustBox(110);
            } else if (level_ <= 5 && level_ > 4) {
                adjustBox(100);
            } else if (level_ <= 4 && level_ > 3) {
                adjustBox(90);
            } else if (level_ <= 3 && level_ > 2) {
                adjustBox(80);
            } else if (level_ <= 2 && level_ > 1) {
                adjustBox(70);
            } else if (level_ <= 1 && level_ >= 0) {
                adjustBox(60);
            } else {
                adjustBox(70);
            }

            emoji.setImageDrawable(getDrawable(R.drawable.neutral_face));
        });
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 100);
    }

    private int dpToPx(Context context, float dp){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void adjustBox(int dp) {
        ViewGroup.LayoutParams lp = emoji.getLayoutParams();

        lp.width = dpToPx(context, dp) ;
        lp.height = dpToPx(context, dp) ;

        emoji.setLayoutParams(lp);
    }
}
