package nl.cwi.dis.aro.extras;

import android.os.Parcel;
import android.os.Parcelable;

public class UserAnnotation implements Parcelable {
    private double timestamp;
    private int videoIndex;
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

    public UserAnnotation(double timestamp, int videoIndex, double arousal, double valence) {
        this.timestamp = timestamp;
        this.videoIndex = videoIndex;
        this.arousal = arousal;
        this.valence = valence;
    }

    public UserAnnotation(int videoIndex, double arousal, double valence) {
        this.timestamp = 0;
        this.videoIndex = videoIndex;
        this.arousal = arousal;
        this.valence = valence;
    }

    public UserAnnotation(Parcel in) {
        this.timestamp = in.readDouble();
        this.videoIndex = in.readInt();
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
        dest.writeInt(this.videoIndex);
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

    public int getVideoIndex() {
        return videoIndex;
    }
}
