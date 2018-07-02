package edu.wit.mobileapp.chronos;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * This is a meetingTime
 * Course will often meet at least 3 days a week. Lectures are added to a list of meetingTimes in a Course Object
 *
 */
public class Lecture implements Parcelable {

    String courseNumber;
    String startTime;
    String endTime;
    char day;
    String place;

    public Lecture(){

    }

    protected Lecture(Parcel in) {
        courseNumber = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        day = (char) in.readInt();
        place = in.readString();
    }

    public static final Creator<Lecture> CREATOR = new Creator<Lecture>() {
        @Override
        public Lecture createFromParcel(Parcel in) {
            return new Lecture(in);
        }

        @Override
        public Lecture[] newArray(int size) {
            return new Lecture[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseNumber);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeInt((int) day);
        dest.writeString(place);
    }
}
