package edu.wit.mobileapp.chronos;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class LandscapeSchedule extends AppCompatActivity {

    Map<String,Course> courses;
    List<Lecture> meetingTimes;
    private static final String PREFS_NAME = "edu.wit.mobileapp.chronos.PortraitSchedule";
    /** This application's preferences */
    private static SharedPreferences saved;

    /** This application's settings editor*/
    private static SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landscape_schedule);

        //Initilizes gson, savedPreferences and savedPreferences.Editor
        //Used for storing data to the device for future runs of application
        Gson gson = new Gson();
        if(saved == null){
            saved = this.getApplicationContext().getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE );
        }
        editor = saved.edit();

        //Try loading existing JSON data from device, and unwrapping it back into the respective List/Map
        if(saved.contains("coursesJSON")){
            String existingCourses = saved.getString("coursesJSON","");
            courses = gson.fromJson(existingCourses, courses.getClass());
        }
        if(saved.contains("meetingTimesJSON")){
            String existingMeetingTimes = saved.getString("meetingTimesJSON", "");
            meetingTimes = gson.fromJson(existingMeetingTimes, meetingTimes.getClass());

        }

        fillSchedule();
    }

    private void fillSchedule(){
        //TODO: uses courses and meeting times to fill the schedule interface

        // break point to check data
        System.out.print("break");
    }
}
