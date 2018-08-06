package edu.wit.mobileapp.chronos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LandscapeSchedule extends AppCompatActivity {

    //fields to be used by the details fragment
    String clickedClassName;
    String clickedClassNumber;
    String clickedClassInstructor;
    String clickedClassLocation;
    String clickedClassTime;

    //
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
        if(saved.contains("numCourses")){
            for(int i = 0; i<saved.getInt("numCourses", 0); i++ ){
                String existingCourse = saved.getString("course"+i+"", "");
                Course courseToAdd = gson.fromJson(existingCourse, Course.class);
                courses.put(courseToAdd.courseNumber, courseToAdd);
            }
        }
        if(saved.contains("numLectures")){
            for(int i = 0; i<saved.getInt("numLectures", 0); i++ ){
                String existingCourse = saved.getString("lecture"+i+"", "");
                Lecture timeToAdd = gson.fromJson(existingCourse, Lecture.class);
                meetingTimes.add(timeToAdd);
            }
        }

        fillSchedule();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //If the orientation has just been changed to portrait, send the user to the portrait schedule activity
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Intent turnPortrait = new Intent();
            turnPortrait.setClass(LandscapeSchedule.this, PortraitSchedule.class);
            startActivity(turnPortrait);
        }
    }

    public int convertDpToPix(int dp){
        //converts pixels to density-independent pixels (dp)
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float pix = dp * (metrics.densityDpi / 160f);
        return Math.round(pix);
    }

    private void fillSchedule(){
        //uses courses and meeting times to fill the schedule interface
        RelativeLayout currentLayout = new RelativeLayout(this);
        String startTime;
        String endTime;
        int startMargin;
        int heightOfBtn;
        //goes through the meetingTimes list to create a button for each class
        for(int i = 0; i < meetingTimes.size(); i++){
            //button representing the current class in the list
            Button cbtn = new Button(this);
            //id = the index of the class in the list
            cbtn.setId(i);
            cbtn.setText(courses.get(meetingTimes.get(i).courseNumber).courseName);
            cbtn.setBackgroundColor(getResources().getColor(R.color.ivory));
            //original times are in the format of 1-2 numbers for hour, then a colon, then 2 numbers for minutes, a space, then am/pm
            startTime = meetingTimes.get(i).startTime;
            endTime = meetingTimes.get(i).endTime;
            //converting start and end times to top margin and height respectively
            if (startTime.split(" ")[1].equals("am")){
                startMargin = (Integer.parseInt(startTime.split(":")[0])-7)*60 + Integer.parseInt(startTime.split(":")[1].split(" ")[0]);
                startMargin = convertDpToPix(startMargin);
            }else{
                if(startTime.split(":")[0].equals("12")){
                    startMargin = 300;
                    startMargin = convertDpToPix(startMargin);
                }else{
                    startMargin = (Integer.parseInt(startTime.split(":")[0]) - 1) * 60 + Integer.parseInt(startTime.split(":")[1].split(" ")[0]) + 360;
                    startMargin = convertDpToPix(startMargin);
                }
            }
            if (endTime.split(" ")[1].equals("am")){
                heightOfBtn = (Integer.parseInt(endTime.split(":")[0])-7)*60 + Integer.parseInt(endTime.split(":")[1].split(" ")[0]);
                heightOfBtn = convertDpToPix(heightOfBtn) - startMargin;
            }else{
                if(endTime.split(":")[0].equals("12")){
                    heightOfBtn = 300;
                    heightOfBtn = convertDpToPix(heightOfBtn) - startMargin;
                }else{
                    heightOfBtn = (Integer.parseInt(endTime.split(":")[0]) - 1) * 60 + Integer.parseInt(endTime.split(":")[1].split(" ")[0]) + 360;
                    heightOfBtn = convertDpToPix(heightOfBtn) - startMargin;
                }
            }
            RelativeLayout.LayoutParams layParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, heightOfBtn);
            layParams.setMargins(0,startMargin,1,0);
            cbtn.setLayoutParams(layParams);
            char currentDay = meetingTimes.get(i).day;
            switch (currentDay){
                case 'M':
                    currentLayout = (RelativeLayout)findViewById(R.id.mondayRelativeLayout);
                    break;
                case 'T':
                    currentLayout = (RelativeLayout)findViewById(R.id.tuesdayRelativeLayout);
                    break;
                case 'W':
                    currentLayout = (RelativeLayout)findViewById(R.id.wednesdayRelativeLayout);
                    break;
                case 'R':
                    currentLayout = (RelativeLayout)findViewById(R.id.thursdayRelativeLayout);
                    break;
                case 'F':
                    currentLayout = (RelativeLayout)findViewById(R.id.fridayRelativeLayout);
                    break;
            }
            final int currentCount = i;
            cbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    //setting fields to be used by the fragment
                    clickedClassName = courses.get(meetingTimes.get(currentCount).courseNumber).courseName;
                    clickedClassNumber = courses.get(meetingTimes.get(currentCount).courseNumber).courseNumber;
                    clickedClassInstructor = courses.get(meetingTimes.get(currentCount).courseNumber).instructor;
                    clickedClassLocation = meetingTimes.get(currentCount).place;
                    clickedClassTime = String.format("%s - %s", meetingTimes.get(currentCount).startTime, meetingTimes.get(currentCount).endTime);

                    Bundle bundle = new Bundle();
                    bundle.putString("courseName", clickedClassName);
                    bundle.putString("courseNumber", clickedClassNumber);
                    bundle.putString("instructor", clickedClassInstructor);
                    bundle.putString("place", clickedClassLocation);
                    bundle.putString("time", clickedClassTime);
                    //opening the fragment
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    Fragment fragment1 = new CourseDetails();
                    fragment1.setArguments(bundle);
                    transaction.replace(R.id.detailsContainerLandscape, fragment1);
                    transaction.commit();
                    transaction.addToBackStack(null);
                }
            });
            currentLayout.addView(cbtn);
        }
    }
}
