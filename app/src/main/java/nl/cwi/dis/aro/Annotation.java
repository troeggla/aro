package nl.cwi.dis.aro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Annotation extends AppCompatActivity {
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

    private Button btn2;
    private RadioGroup valence_group;
    private RadioGroup arousal_group;
    private String valence = "";
    private String arousal = "";
    private String annotation = "";

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
    int videoID;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_annotation);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }
    public void onResume(){
        super.onResume();
        Intent intent = getIntent();
        videoID = intent.getIntExtra("videoID", 0);
        final String user_name = intent.getStringExtra("user_name");
        final String user_age = intent.getStringExtra("user_age");
        final String user_gender = intent.getStringExtra("user_gender");
        annotation = intent.getStringExtra("annotation");
        btn2 = findViewById(R.id.button2);
        valence_group = findViewById(R.id.valence_RadioGroup);
        arousal_group = findViewById(R.id.arousal_RadioGroup);
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                videoID = videoID+1;
                if(valence_group.getCheckedRadioButtonId()!=-1)
                {
                    RadioButton rb = findViewById(valence_group.getCheckedRadioButtonId());
                    valence = rb.getText().toString();
                    annotation = annotation+valence+",";
                }
                else
                    {Toast.makeText(Annotation.this,"Pleas input your valence!", Toast.LENGTH_LONG).show();}
                if(arousal_group.getCheckedRadioButtonId()!=-1)
                {
                    RadioButton rb = findViewById(arousal_group.getCheckedRadioButtonId());
                    arousal = rb.getText().toString();
                    annotation = annotation+arousal+"\n";
                }
                else
                {Toast.makeText(Annotation.this,"Pleas input your arousal!", Toast.LENGTH_LONG).show();}
                if(valence_group.getCheckedRadioButtonId()!=-1&&arousal_group.getCheckedRadioButtonId()!=-1) {
                    if (videoID < 12) {
                        Intent i = new Intent(Annotation.this, VideoPlayer.class);
                        i.putExtra("annotation", annotation);
                        i.putExtra("videoID", videoID);
                        i.putExtra("user_name", user_name);
                        i.putExtra("user_age", user_age);
                        i.putExtra("user_gender", user_gender);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(Annotation.this, Ending.class);
                        i.putExtra("annotation", annotation);
                        i.putExtra("user_name", user_name);
                        i.putExtra("user_age", user_age);
                        i.putExtra("user_gender", user_gender);
                        startActivity(i);
                    }
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
}
