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

public class UserSession implements Parcelable {
    private static final String LOG_TAG = "UserSession";

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
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

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public ArrayList<UserAnnotation> getAnnotations() {
        return this.annotations;
    }

    public ArrayList<UserAnnotation> getQuestionnaireResponses() {
        return this.questionnaireResponses;
    }

    public void addAnnotation(double arousal, double valence) {
        this.annotations.add(new UserAnnotation(
                System.currentTimeMillis() / 1000.0,
                this.getVideoIndex(),
                arousal,
                valence
        ));
    }

    public void addQuestionnaireResponse(double arousal, double valence) {
        this.questionnaireResponses.add(new UserAnnotation(
                this.getVideoIndex(),
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

    public void writeToFile(File targetDir) {
        String fileName = this.name + "_" + this.age + "_" + this.gender + ".csv";
        File dataFile = new File(targetDir, fileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(dataFile)) {
            fileOutputStream.write("timestamp,videoIndex,arousal,valence\n".getBytes());

            for (UserAnnotation a : this.annotations) {
                String line = String.format(
                        Locale.ENGLISH,
                        "%.1f,%d,%.3f,%.3f\n",
                        a.getTimestamp(), a.getVideoIndex(), a.getArousal(), a.getValence()
                );

                fileOutputStream.write(line.getBytes());
            }
        } catch (FileNotFoundException fnf) {
            Log.e(LOG_TAG, "File not found: " + fnf);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "IO Exception: " + ioe);
        }
    }
}
