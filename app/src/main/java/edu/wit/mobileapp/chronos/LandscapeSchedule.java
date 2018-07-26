package edu.wit.mobileapp.chronos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
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


        //Initializes the structures for courses and meetingTimes
        courses = new HashMap<>();
        meetingTimes = new ArrayList<>();

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int orientation=newConfig.orientation;

        //If the orientation has just been changed to portrait, send the user to the portrait schedule activity
        if(orientation == 1) {
            Intent turnPortrait = new Intent();
            turnPortrait.setClass(LandscapeSchedule.this, PortraitSchedule.class);
            startActivity(turnPortrait);
        }
    }

    private void fillSchedule(){
        //uses courses and meeting times to fill the schedule interface
        RelativeLayout currentLayout = new RelativeLayout(this);
        //goes through the meetingTimes list to create a button for each class
        for(int i = 0; i < meetingTimes.size(); i++){
            //button representing the current class in the list
            Button cbtn = new Button(this);
            //id = the index of the class in the list
            cbtn.setId(i);
            cbtn.setTag(meetingTimes.get(i).courseNumber);
            cbtn.setText(meetingTimes.get(i).courseNumber);
            cbtn.setBackgroundColor(getResources().getColor(R.color.ivory));
            RelativeLayout.LayoutParams layParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            //TODO: make a method to convert time to margins

            layParams.setMargins(0,0,0,0);
            cbtn.setLayoutParams(layParams);
            char currentDay = meetingTimes.get(i).day;
            switch (currentDay){
                case 'M': currentLayout = (RelativeLayout)findViewById(R.id.mondayRelativeLayout);
                case 'T': currentLayout = (RelativeLayout)findViewById(R.id.tuesdayRelativeLayout);
                case 'W': currentLayout = (RelativeLayout)findViewById(R.id.wednesdayRelativeLayout);
                case 'R': currentLayout = (RelativeLayout)findViewById(R.id.thursdayRelativeLayout);
                case 'F': currentLayout = (RelativeLayout)findViewById(R.id.fridayRelativeLayout);
            }
            currentLayout.addView(cbtn);
            //TODO: make onClick listener
            //String key = meetingTimes.get(i).courseNumber;
            //String courseName = courses.get(key).name;
            //rest of courses data and bundle to send to fragment...
        }
        // break point to check data
        System.out.print("break");
    }
}
