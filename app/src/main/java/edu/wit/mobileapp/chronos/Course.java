package edu.wit.mobileapp.chronos;

import java.util.ArrayList;

/*
 * This is a Course
 * Courses will have one courseNumber, name, and instructor, but may have mulitple meetingTimes and Homework assignments
 *
 */
public class Course {
    String courseNumber;
    String courseName;
    String instructor;

    ArrayList<Lecture> meetingTimes;
    ArrayList<Homework> homework;


    public Course(String id){
        courseNumber = id;
        meetingTimes = new ArrayList<Lecture>();
    }

    void addLecture(Lecture lec){
        meetingTimes.add(lec);
    }

    void addHomework(Homework hw){
        homework.add(hw);
    }
}
