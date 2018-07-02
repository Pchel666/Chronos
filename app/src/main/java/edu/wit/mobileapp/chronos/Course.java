package edu.wit.mobileapp.chronos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/*
 * This is a Course
 * Courses will have one courseNumber, name, and instructor, but may have mulitple meetingTimes and Homework assignments
 *
 */
public class Course implements Parcelable{
    String courseNumber;
    String courseName;
    String instructor;

    ArrayList<Lecture> meetingTimes;
    ArrayList<Homework> homework;


    public Course(String id){
        courseNumber = id;
        meetingTimes = new ArrayList<Lecture>();
    }

    protected Course(Parcel in) {
        courseNumber = in.readString();
        courseName = in.readString();
        instructor = in.readString();
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    void addLecture(Lecture lec){
        meetingTimes.add(lec);
    }

    void addHomework(Homework hw){
        homework.add(hw);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseNumber);
        dest.writeString(courseName);
        dest.writeString(instructor);
    }
}
