package nl.cwi.dis.aro.extras;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

public class UserSession implements Parcelable {
    private static final String LOG_TAG = "UserSession";

    public static final Parcelable.Creator<UserSession> CREATOR = new Parcelable.Creator<UserSession>() {
        @Override
        public UserSession createFromParcel(Parcel in) {
            return new UserSession(in);
        }

        @Override
        public UserSession[] newArray(int size) {
            return new UserSession[size];
        }
    };

    private String name;
    private int age;
    private String gender;
    private ArrayList<UserAnnotation> annotations;
    private ArrayList<UserAnnotation> questionnaireResponses;
    private ArrayList<String> videos;
    private int videoIndex;

    public UserSession(Parcel in) {
        this.name = in.readString();
        this.age = in.readInt();
        this.gender = in.readString();

        this.annotations = new ArrayList<>();
        in.readTypedList(this.annotations, UserAnnotation.CREATOR);

        this.questionnaireResponses = new ArrayList<>();
        in.readTypedList(this.questionnaireResponses, UserAnnotation.CREATOR);

        this.videos = in.readArrayList(null);
        this.videoIndex = in.readInt();
    }

    public UserSession(String name, int age, String gender, ArrayList<String> videos) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.annotations = new ArrayList<>();
        this.questionnaireResponses = new ArrayList<>();
        this.videos = videos;
        this.videoIndex = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.age);
        dest.writeString(this.gender);
        dest.writeTypedList(this.annotations);
        dest.writeTypedList(this.questionnaireResponses);
        dest.writeList(this.videos);
        dest.writeInt(this.videoIndex);
    }

    public String getName() {
        return name;
    }

    public void addAnnotation(double arousal, double valence) {
        this.annotations.add(new UserAnnotation(
                System.currentTimeMillis() / 1000.0,
                this.videos.get(this.getVideoIndex()),
                arousal,
                valence
        ));
    }

    public void addQuestionnaireResponse(double arousal, double valence) {
        this.questionnaireResponses.add(new UserAnnotation(
                this.videos.get(this.getVideoIndex()),
                arousal,
                valence
        ));
    }

    public String getCurrentVideoPath() {
        if (this.videoIndex >= this.videos.size()) {
            return null;
        }

        return this.videos.get(this.videoIndex);
    }

    public int getVideoIndex() {
        return videoIndex;
    }

    public void incrementVideoIndex() {
        this.videoIndex++;
    }

    public void writeResponsesToFile(File targetDir) {
        this.writeRockerValuesToFile(targetDir);
        this.writeQuestionnaireResponsesToFile(targetDir);
    }

    private void writeQuestionnaireResponsesToFile(File targetDir) {
        ArrayList<String> lines = this.questionnaireResponses.stream().map(a -> String.format(
                Locale.ENGLISH,
                "\"%s\",%.3f,%.3f\n",
                a.getVideoName(), a.getArousal(), a.getValence()
        )).collect(Collectors.toCollection(ArrayList::new));

        this.writeDataToFile(
                String.format(Locale.ENGLISH, "%s_%d_%s_questionnaire.csv", this.name, this.age, this.gender),
                targetDir,
                "videoName,arousal,valence\n",
                lines
        );
    }

    private void writeRockerValuesToFile(File targetDir) {
        ArrayList<String> lines = this.annotations.stream().map(a -> String.format(
                Locale.ENGLISH,
                "%.1f,\"%s\",%.3f,%.3f\n",
                a.getTimestamp(), a.getVideoName(), a.getArousal(), a.getValence()
        )).collect(Collectors.toCollection(ArrayList::new));

        this.writeDataToFile(
                String.format(Locale.ENGLISH, "%s_%d_%s_values.csv", this.name, this.age, this.gender),
                targetDir,
                "timestamp,videoName,arousal,valence\n",
                lines
        );
    }

    private void writeDataToFile(String filename, File targetDir, String header, ArrayList<String> lines) {
        if (lines.size() == 0) {
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(targetDir, filename))) {
            fileOutputStream.write(header.getBytes());

            for (String line : lines) {
                fileOutputStream.write(line.getBytes());
            }
        } catch (FileNotFoundException fnf) {
            Log.e(LOG_TAG, "File " + filename + " not found: " + fnf);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "File " + filename + " IO Exception: " + ioe);
        }
    }
}
