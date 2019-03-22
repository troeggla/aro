package nl.cwi.dis.aro;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

public class VideoPlayerActivity extends AppCompatActivity {
    private static final String LOG_TAG = "VideoPlayerActivity";

    private double angle = 0;
    private double level = 0;
    private double valence = 5;
    private double arousal = 5;

    private int playbackIndex = 0;

    private TextView valence_txt;
    private TextView arousal_txt;
    private ImageView emoji;
    private ImageView borderFrame;
    private MyRockerView mRockerViewXY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mRockerViewXY = findViewById(R.id.rockerXY_View);
        arousal_txt = findViewById(R.id.arousal_txt);
        valence_txt = findViewById(R.id.valence_txt);
        borderFrame = findViewById(R.id.borderFrame);

        this.hideSystemUI();
    }

    public void onResume(){
        super.onResume();

        final VideoView videoView = findViewById(R.id.videoView);

        Intent intent = getIntent();
        final UserSession session = intent.getParcelableExtra("session");

        final String videoPath = session.getCurrentVideoPath();
        Log.d(LOG_TAG, "Current video path: " + videoPath);

        final String pre_play = "android.resource://" + getPackageName() + "/" + (R.raw.pre_play);
        final String after_play = "android.resource://" + getPackageName() + "/" + (R.raw.after_play);

        Uri uri_pre = Uri.parse(pre_play);
        videoView.setVideoURI(uri_pre);
        videoView.start();

        initMyClick();

        final Timer loggingTimer = new java.util.Timer(true);
        final TimerTask task = new TimerTask() {
            public void run() {
                session.addAnnotation(arousal, valence);
            }
        };

        videoView.setOnCompletionListener(mp -> {
            if (playbackIndex == 0) {
                videoView.stopPlayback();

                //start the video player
                Uri uri = Uri.parse(videoPath);
                videoView.setVideoURI(uri);
                videoView.start();

                loggingTimer.schedule(task, 0, 100);

                playbackIndex = 1;
            } else if (playbackIndex == 1) {
                loggingTimer.cancel();
                videoView.stopPlayback();

                Uri uri = Uri.parse(after_play);
                videoView.setVideoURI(uri);
                videoView.start();

                playbackIndex = 2;
            } else if (playbackIndex == 2) {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                session.writeResponsesToFile(downloadDir);

                Intent annotationIntent = new Intent(VideoPlayerActivity.this, AnnotationActivity.class);
                annotationIntent.putExtra("session", session);

                startActivity(annotationIntent);
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

                valence_txt.setText(String.format(Locale.ENGLISH, "valence = %.1f", valence));
                arousal_txt.setText(String.format(Locale.ENGLISH, "arousal = %.1f", arousal));

                if (valence > 5) {
                    borderFrame.setImageDrawable(getDrawable(R.drawable.b_p));
                } else if (valence < 5) {
                    borderFrame.setImageDrawable(getDrawable(R.drawable.b_n));
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
                borderFrame.setImageDrawable(getDrawable(R.drawable.b_p));
            } else if (valence < 5) {
                borderFrame.setImageDrawable(getDrawable(R.drawable.b_n));
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

    private void hideSystemUI() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private int dpToPx(Context context, float dp){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void adjustBox(int dp) {
        ViewGroup.LayoutParams lp = emoji.getLayoutParams();

        lp.width = dpToPx(this, dp) ;
        lp.height = dpToPx(this, dp) ;

        emoji.setLayoutParams(lp);
    }
}
