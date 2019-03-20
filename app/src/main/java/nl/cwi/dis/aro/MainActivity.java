package nl.cwi.dis.aro;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQUEST = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        int writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED || readStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                    STORAGE_PERMISSION_REQUEST
            );

            return;
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

        final EditText user_age_txt = findViewById(R.id.age_txt);
        final EditText user_name_txt = findViewById(R.id.name_txt);
        final RadioGroup user_gender_group = findViewById(R.id.gender_group);

        Button btn1 = findViewById(R.id.start_button);

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this , VideoPlayerActivity.class);

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

        Log.d(LOG_TAG, "Video files: " + videoFiles.length);
        Arrays.sort(videoFiles);

        return videoFiles;
    }
}
