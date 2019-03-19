package nl.cwi.dis.aro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;

import nl.cwi.dis.aro.extras.UserSession;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Annotation extends AppCompatActivity {
    private final static String LOG_TAG = "Annotation";
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private RadioGroup valence_group;
    private RadioGroup arousal_group;
    private String valence = "";
    private String arousal = "";

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
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_annotation);

        UserSession session = getIntent().getParcelableExtra("session");
        Log.d(LOG_TAG, "Launched Annotation activity: " + session.getCurrentVideoPath());

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        Intent intent = getIntent();
        final UserSession session = intent.getParcelableExtra("session");

        valence_group = findViewById(R.id.valence_RadioGroup);
        arousal_group = findViewById(R.id.arousal_RadioGroup);

        Button btn2 = findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (valence_group.getCheckedRadioButtonId() != -1) {
                    RadioButton rb = findViewById(valence_group.getCheckedRadioButtonId());
                    valence = rb.getText().toString();
                } else {
                    Toast.makeText(Annotation.this,"Please input a value for valence!", Toast.LENGTH_LONG).show();
                }

                if (arousal_group.getCheckedRadioButtonId() != -1) {
                    RadioButton rb = findViewById(arousal_group.getCheckedRadioButtonId());
                    arousal = rb.getText().toString();
                } else {
                    Toast.makeText(Annotation.this,"Please input a value for arousal!", Toast.LENGTH_LONG).show();
                }

                if(valence_group.getCheckedRadioButtonId() != -1 && arousal_group.getCheckedRadioButtonId() != -1) {
                    session.addQuestionnaireResponse(
                            Double.parseDouble(arousal),
                            Double.parseDouble(valence)
                    );

                    File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    session.writeToFile(downloadDir);

                    session.incrementVideoIndex();

                    Log.d(LOG_TAG, "Incremented index: " + session.getVideoIndex());
                    Log.d(LOG_TAG, "New video path: " + session.getCurrentVideoPath());

                    Intent videoIntent;

                    if (session.getCurrentVideoPath() != null) {
                        videoIntent = new Intent(Annotation.this, VideoPlayer.class);
                    } else {
                        videoIntent = new Intent(Annotation.this, Ending.class);
                    }

                    videoIntent.putExtra("session", session);
                    startActivity(videoIntent);
                }
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
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

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
