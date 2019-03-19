package nl.cwi.dis.aro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import nl.cwi.dis.aro.extras.UserSession;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
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

    private View mContentView;
    private View mControlsView;

    private static final String LOG_TAG = "FullscreenActivity";
    private static final int STORAGE_PERMISSION_REQUEST = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        int writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED || readStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                    STORAGE_PERMISSION_REQUEST
            );
        } else {
            Log.d(LOG_TAG, "Permission already granted");
        }

        final File[] videoFiles = this.readVideoDirectory();

        if (videoFiles.length == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Video directory")
                    .setMessage("Place your video files into the Aro/ directory on your internal storage and restart the app")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finishAffinity();
                            System.exit(0);
                        }
                    })
                    .show();

            return;
        }

        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        final EditText user_age_txt = findViewById(R.id.age_txt);
        final EditText user_name_txt = findViewById(R.id.name_txt);
        final RadioGroup user_gender_group = findViewById(R.id.gender_group);

        Button btn1 = findViewById(R.id.start_button);

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(FullscreenActivity.this , VideoPlayer.class);

                String user_name = user_name_txt.getText().toString().trim();
                String user_age = user_age_txt.getText().toString().trim();

                String user_gender = "";

                if (user_gender_group.getCheckedRadioButtonId() != -1) {
                    RadioButton rb = findViewById(user_gender_group.getCheckedRadioButtonId());
                    user_gender = rb.getText().toString();
                }

                if (user_name.length() == 0) {
                    Toast.makeText(FullscreenActivity.this,"Please input your name!", Toast.LENGTH_LONG).show();
                } else if (user_age.length() == 0) {
                    Toast.makeText(FullscreenActivity.this,"Please input your age!", Toast.LENGTH_LONG).show();
                } else if (user_gender.length() == 0) {
                    Toast.makeText(FullscreenActivity.this,"Please select your gender!", Toast.LENGTH_LONG).show();
                } else {
                    ArrayList<String> filenames = new ArrayList<>();

                    for (File f : videoFiles) {
                        filenames.add(f.getAbsolutePath());
                    }

                    UserSession session = new UserSession(
                            user_name,
                            Integer.parseInt(user_age),
                            user_gender,
                            filenames
                    );

                    intent.putExtra("session", session);
                    startActivity(intent);
                }
            }
        });
    }

    private File[] readVideoDirectory() {
        File storage = Environment.getExternalStorageDirectory();
        File videoDir = new File(storage, getResources().getString(R.string.app_name) + "/");

        if (!videoDir.exists()) {
            Log.d(LOG_TAG, "Video directory does not exist");
            Log.d(LOG_TAG, "Attempting to create directory: " + videoDir.mkdirs());

            return new File[] {};
        }

        File[] videoFiles = videoDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp4");
            }
        });
        Arrays.sort(videoFiles);

        return videoFiles;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

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

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
