package nl.cwi.dis.aro;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.util.Locale;
import java.util.Timer;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;
import android.net.Uri;

import java.lang.Math;
import java.util.TimerTask;

import nl.cwi.dis.aro.extras.UserSession;
import nl.cwi.dis.aro.views.BorderFrame;
import nl.cwi.dis.aro.views.RockerView;

public class VideoPlayerActivity extends AppCompatActivity {
    private static final String LOG_TAG = "VideoPlayerActivity";

    private double angle = 0;
    private double level = 0;
    private double valence = 5;
    private double arousal = 5;

    private TextView valence_txt;
    private TextView arousal_txt;
    private TextView moodText;
    private BorderFrame borderFrame;
    private RockerView mRockerViewXY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mRockerViewXY = findViewById(R.id.rockerXY_View);
        arousal_txt = findViewById(R.id.arousal_txt);
        valence_txt = findViewById(R.id.valence_txt);
        borderFrame = findViewById(R.id.borderFrame);
        moodText = findViewById(R.id.mood_text);

        this.hideSystemUI();
    }

    @Override
    public void onResume(){
        super.onResume();

        Intent intent = getIntent();
        final UserSession session = intent.getParcelableExtra("session");

        final String videoPath = session.getCurrentVideoPath();
        Log.d(LOG_TAG, "Current video path: " + videoPath);

        final TextView fullscreenText = findViewById(R.id.fullscreenText);
        final VideoView videoView = findViewById(R.id.videoView);

        final Timer loggingTimer = new java.util.Timer(true);
        final TimerTask task = new TimerTask() {
            public void run() {
                session.addAnnotation(arousal, valence);
            }
        };

        fullscreenText.setText(R.string.pre_text);
        fullscreenText.setOnClickListener(v -> {
            fullscreenText.setVisibility(View.INVISIBLE);

            Uri uri = Uri.parse(videoPath);
            videoView.setVideoURI(uri);
            videoView.start();

            loggingTimer.schedule(task, 0, 100);
        });

        videoView.setOnCompletionListener(mp -> {
            loggingTimer.cancel();
            videoView.stopPlayback();

            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            session.writeResponsesToFile(downloadDir);

            fullscreenText.setText(R.string.post_text);
            fullscreenText.setOnClickListener(v -> {
                Intent annotationIntent = new Intent(VideoPlayerActivity.this, AnnotationActivity.class);
                annotationIntent.putExtra("session", session);

                startActivity(annotationIntent);
            });
            fullscreenText.setVisibility(View.VISIBLE);
        });

        this.initRocker();
    }

    private void initRocker() {
        mRockerViewXY.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
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
                    borderFrame.setFrameColor(255, 234, 137, 104);
                } else if (valence < 5) {
                    borderFrame.setFrameColor(255, 108, 170, 204);
                }

                if(level > 1) {
                    if(angle < 1.57 && angle>=0) {
                        moodText.setText(R.string.relieved);
                    } else if (angle >= 1.57 && angle < 3.14) {
                        moodText.setText(R.string.pensive);
                    } else if (angle >= 3.14 && angle < 4.71) {
                        moodText.setText(R.string.angry);
                    } else if (angle >= 4.71 && angle < 6.28) {
                        moodText.setText(R.string.smile);
                    }
                }

            }

            @Override
            public void onFinish() {}
        });

        mRockerViewXY.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {}

            @Override
            public void direction(RockerView.Direction direction) {
                if (direction == RockerView.Direction.DIRECTION_CENTER) {
                    valence = 5;
                    arousal = 5;

                    moodText.setText(R.string.neutral);
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
                borderFrame.setFrameColor(255, 234, 137, 104);
            } else if (valence < 5) {
                borderFrame.setFrameColor(255, 108, 170, 204);
            }

            int textSize = 30;

            if (level_ <= 6 && level_ > 5) {
                textSize = 55;
            } else if (level_ <= 5 && level_ > 4) {
                textSize = 50;
            } else if (level_ <= 4 && level_ > 3) {
                textSize = 45;
            } else if (level_ <= 3 && level_ > 2) {
                textSize = 40;
            } else if (level_ <= 2 && level_ > 1) {
                textSize = 35;
            }

            moodText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            moodText.setText(R.string.neutral);
        });
    }

    @Override
    public void onBackPressed() {
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
}
