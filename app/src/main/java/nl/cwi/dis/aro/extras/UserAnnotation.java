package nl.cwi.dis.aro.extras;

import android.os.Parcel;
import android.os.Parcelable;

public class UserAnnotation implements Parcelable {
    private double timestamp;
    private String videoName;
    private double arousal;
    private double valence;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public UserAnnotation createFromParcel(Parcel in) {
            return new UserAnnotation(in);
        }

        @Override
        public UserAnnotation[] newArray(int size) {
            return new UserAnnotation[size];
        }
    };

    public UserAnnotation(double timestamp, String videoName, double arousal, double valence) {
        this.timestamp = timestamp;
        this.videoName = videoName;
        this.arousal = arousal;
        this.valence = valence;
    }

    public UserAnnotation(String videoName, double arousal, double valence) {
        this.timestamp = 0;
        this.videoName = videoName;
        this.arousal = arousal;
        this.valence = valence;
    }

    public UserAnnotation(Parcel in) {
        this.timestamp = in.readDouble();
        this.videoName = in.readString();
        this.arousal = in.readDouble();
        this.valence = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.timestamp);
        dest.writeString(this.videoName);
        dest.writeDouble(this.arousal);
        dest.writeDouble(this.valence);
    }

    public double getTimestamp() {
        return timestamp;
    }

    public double getArousal() {
        return arousal;
    }

    public double getValence() {
        return valence;
    }

    public String getVideoName() {
        String[] path = this.videoName.split("/");
        return path[path.length - 1];
    }
}
