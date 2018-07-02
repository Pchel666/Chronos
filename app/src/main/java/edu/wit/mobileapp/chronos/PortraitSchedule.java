package edu.wit.mobileapp.chronos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class PortraitSchedule extends AppCompatActivity {
    List<Course> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portrait_schedule);
        courseList = new ArrayList<>();

        int numCourses = getIntent().getIntExtra("numCourses",0);

        for(int i = 0; i< numCourses; i++){
            Course courseToAdd = (Course) getIntent().getParcelableExtra("course"+i);
            courseList.add(courseToAdd);
        }

        System.out.print("break"); //To set a break point to check data


    }
}
