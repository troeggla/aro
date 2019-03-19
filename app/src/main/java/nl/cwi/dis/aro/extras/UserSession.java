package nl.cwi.dis.aro.extras;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class UserSession implements Parcelable {
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

    private class Annotation {
        int arousal;
        int valence;
    }

    private String name;
    private int age;
    private String gender;

    private ArrayList<Annotation> annotations;
    private ArrayList<String> videos;
    private int videoIndex;

    public UserSession(Parcel in) {
        this.name = in.readString();
        this.age = in.readInt();
        this.gender = in.readString();
        this.annotations = in.readArrayList(null);
        this.videos = in.readArrayList(null);
        this.videoIndex = in.readInt();
    }

    public UserSession(String name, int age, String gender, ArrayList<String> videos) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.annotations = new ArrayList<>();
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
        dest.writeList(this.annotations);
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

    public ArrayList<Annotation> getAnnotations() {
        return this.annotations;
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
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
}
