package edu.wit.mobileapp.chronos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortraitSchedule extends AppCompatActivity {
    Map<String,Course> courses;
    List<Lecture> meetingTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portrait_schedule);
        courses = new HashMap<>();
        meetingTimes = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();

        int numCourses = bundle.getInt("numCourses",0);
        int numLectures = bundle.getInt("numLectures",0);

        for(int i = 0; i< numCourses; i++){
            Course courseToAdd = bundle.getParcelable("course"+i+"");
            courses.put(courseToAdd.courseNumber, courseToAdd);
        }
        for(int i = 0; i< numLectures; i++){
            Lecture timeToAdd = bundle.getParcelable("lecture"+i+"");
            meetingTimes.add(timeToAdd);
        }

        System.out.print("break"); //To set a break point to check data


    }
}
