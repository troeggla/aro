package nl.cwi.dis.aro;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.File;

import nl.cwi.dis.aro.extras.UserSession;

public class AnnotationActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AnnotationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation);
    }

    @Override
    public void onResume(){
        super.onResume();

        Intent intent = getIntent();
        final UserSession session = intent.getParcelableExtra("session");

        final SeekBar valenceSlider = findViewById(R.id.seekBar_valence);
        final SeekBar arousalSlider = findViewById(R.id.seekBar_arousal);

        Button btn2 = findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int valence = valenceSlider.getProgress() + 1;
                int arousal = arousalSlider.getProgress() + 1;

                session.addQuestionnaireResponse(
                        arousal,
                        valence
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
        });
    }
}
