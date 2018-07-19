package edu.wit.mobileapp.chronos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortraitSchedule extends AppCompatActivity {
    Map<String,Course> courses;
    List<Lecture> meetingTimes;
    boolean fromLogin;

    private static final String PREFS_NAME = "edu.wit.mobileapp.chronos.PortraitSchedule";
    /** This application's preferences */
    private static SharedPreferences saved;


    /** This application's settings editor*/
    private static SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portrait_schedule);

        //Initilizes gson, savedPreferences and savedPreferences.Editor
        //Used for storing data to the device for future runs of application
        Gson gson = new Gson();
        if(saved == null){
            saved = this.getApplicationContext().getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE );
        }
        editor = saved.edit();

        //Used for resetting the app (clearing courses, meetingTimes, and fromLogin from the device)
//        editor.clear();
//        editor.commit();

        //Loads a boolean from device data, that tells us whether or not we arrived at this schedule from the login page
        fromLogin = saved.getBoolean("fromLogin", false);

        //Initializes the structures for courses and meetingTimes
        courses = new HashMap<>();
        meetingTimes = new ArrayList<>();

        //***********
        //If we arrived at this activity from the login screen, parse the scraped data,
        //otherwise (if we got here from the other activities or on launch) load existing course data
        //if data doesn't exist, do to the login activity to scrape the data
        //***********
        if(fromLogin){

            //Gets the bundle of data sent by the login activity
            Bundle bundle = getIntent().getExtras();

            //Parses the scraped data into the structures declared above: courses and meetingTimes
            int numCourses = bundle.getInt("numCourses", 0);
            int numLectures = bundle.getInt("numLectures", 0);
            for (int i = 0; i < numCourses; i++) {
                Course courseToAdd = bundle.getParcelable("course" + i + "");
                courses.put(courseToAdd.courseNumber, courseToAdd);
            }
            for (int i = 0; i < numLectures; i++) {
                Lecture timeToAdd = bundle.getParcelable("lecture" + i + "");
                meetingTimes.add(timeToAdd);
            }

            //Turns courses and meeting times into JSON data, in order to store to device
            String cid = "coursesJSON";
            String mid = "meetingTimesJSON";
            String coursesJSON = gson.toJson(courses);
            String meetingTimesJSON = gson.toJson(meetingTimes);

            //Stores the data to the device, sets boolean to inform app we will not return to this activity from login
            editor.putString(cid, coursesJSON);
            editor.putString(mid, meetingTimesJSON);
            editor.putBoolean("fromLogin", false);
            editor.commit();

//            //break point to check data
//            System.out.print("break");

            //Fill the schedule with this parsed data
            fillSchedule();

        } else {

            //Try loading existing JSON data from device, and unwrapping it back into the respective List/Map
            if(saved.contains("coursesJSON")){
                String existingCourses = saved.getString("coursesJSON","");
                courses = gson.fromJson(existingCourses, courses.getClass());
            }
            if(saved.contains("meetingTimesJSON")){
                String existingMeetingTimes = saved.getString("meetingTimesJSON", "");
                meetingTimes = gson.fromJson(existingMeetingTimes, meetingTimes.getClass());

            }

            //If the data is unsuccessfully loaded/doesn't exist, go to the login screen to scrape data
            if (courses.isEmpty() || meetingTimes.isEmpty()) {

                //intent for login
                Intent goToLogin = new Intent();
                goToLogin.setClass(PortraitSchedule.this, WebDummy.class);

                //inform app that the next time we arrive at Portrait Schedule, we will be coming from the login activity
                editor.putBoolean("fromLogin",true);
                editor.commit();
                startActivity(goToLogin);

                //we are not on this activity anymore, we need to get out of the onCreate() method
                return;
            } else {

                //If the data exists, use it to fill the schedule
                fillSchedule();
            }
        }
    }


    private void fillSchedule(){
        //TODO: uses courses and meeting times to fill the schedule interface

        // break point to check data
        System.out.print("break");
    }
}
