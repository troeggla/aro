package nl.cwi.dis.aro;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;

import nl.cwi.dis.aro.extras.UserSession;

public class AnnotationActivity extends AppCompatActivity {
    private final static String LOG_TAG = "AnnotationActivity";

    private RadioGroup valence_group;
    private RadioGroup arousal_group;

    private String valence = "";
    private String arousal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_annotation);

        UserSession session = getIntent().getParcelableExtra("session");
        Log.d(LOG_TAG, "Launched AnnotationActivity activity: " + session.getCurrentVideoPath());
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
                    Toast.makeText(AnnotationActivity.this,"Please input a value for valence!", Toast.LENGTH_LONG).show();
                }

                if (arousal_group.getCheckedRadioButtonId() != -1) {
                    RadioButton rb = findViewById(arousal_group.getCheckedRadioButtonId());
                    arousal = rb.getText().toString();
                } else {
                    Toast.makeText(AnnotationActivity.this,"Please input a value for arousal!", Toast.LENGTH_LONG).show();
                }

                if(valence_group.getCheckedRadioButtonId() != -1 && arousal_group.getCheckedRadioButtonId() != -1) {
                    session.addQuestionnaireResponse(
                            Double.parseDouble(arousal),
                            Double.parseDouble(valence)
                    );

                    File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    session.writeResponsesToFile(downloadDir);

                    session.incrementVideoIndex();

                    Log.d(LOG_TAG, "Incremented index: " + session.getVideoIndex());
                    Log.d(LOG_TAG, "New video path: " + session.getCurrentVideoPath());

                    Intent videoIntent;

                    if (session.getCurrentVideoPath() != null) {
                        videoIntent = new Intent(AnnotationActivity.this, VideoPlayerActivity.class);
                    } else {
                        videoIntent = new Intent(AnnotationActivity.this, EndingActivity.class);
                    }

                    videoIntent.putExtra("session", session);
                    startActivity(videoIntent);
                }
            }
        });

    }
}
