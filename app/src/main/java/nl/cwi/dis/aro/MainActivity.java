package nl.cwi.dis.aro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import nl.cwi.dis.aro.extras.UserSession;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQUEST = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                    STORAGE_PERMISSION_REQUEST
            );
        } else {
            Log.d(LOG_TAG, "Permission already granted");
            this.setupUI();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.setupUI();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("File system permissions")
                        .setMessage("The app needs access to external storage in order to function properly. Please restart the app and grant the permission.")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            finishAffinity();
                            System.exit(0);
                        })
                        .show();
            }
        }
    }

    private void setupUI() {
        File[] videoFiles = this.readVideoDirectory();

        if (videoFiles.length == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Video directory")
                    .setMessage("Place your video files into the Aro/ directory on your external storage and restart the app")
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        finishAffinity();
                        System.exit(0);
                    })
                    .show();

            return;
        }

        final EditText user_age_txt = findViewById(R.id.age_txt);
        final EditText user_name_txt = findViewById(R.id.name_txt);
        final RadioGroup user_gender_group = findViewById(R.id.gender_group);

        Button btn1 = findViewById(R.id.start_button);

        btn1.setOnClickListener(v -> {
            String user_name = user_name_txt.getText().toString().trim();
            String user_age = user_age_txt.getText().toString().trim();

            String user_gender = "";

            if (user_gender_group.getCheckedRadioButtonId() != -1) {
                RadioButton rb = findViewById(user_gender_group.getCheckedRadioButtonId());
                user_gender = rb.getText().toString();
            }

            if (user_name.length() == 0) {
                Toast.makeText(MainActivity.this,"Please input your name!", Toast.LENGTH_LONG).show();
            } else if (user_age.length() == 0) {
                Toast.makeText(MainActivity.this,"Please input your age!", Toast.LENGTH_LONG).show();
            } else if (user_gender.length() == 0) {
                Toast.makeText(MainActivity.this,"Please select your gender!", Toast.LENGTH_LONG).show();
            } else {
                UserSession session = new UserSession(
                        user_name,
                        Integer.parseInt(user_age),
                        user_gender,
                        Arrays.stream(videoFiles).map(File::getAbsolutePath).collect(Collectors.toCollection(ArrayList::new))
                );

                Intent intent = new Intent(MainActivity.this , VideoPlayerActivity.class);
                intent.putExtra("session", session);

                startActivity(intent);
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

        File[] videoFiles = videoDir.listFiles((dir, name) -> name.endsWith(".mp4"));

        Log.d(LOG_TAG, "Video files: " + videoFiles.length);
        Arrays.sort(videoFiles);

        return videoFiles;
    }
}
