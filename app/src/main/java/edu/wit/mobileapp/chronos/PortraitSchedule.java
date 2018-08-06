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
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortraitSchedule extends AppCompatActivity {

    //fields to be used by the details fragment
    String clickedClassName;
    String clickedClassNumber;
    String clickedClassInstructor;
    String clickedClassLocation;
    String clickedClassTime;
    int clickedClassOffset;

    //
    Map<String,Course> courses;
    List<Lecture> meetingTimes;
    int numCourses;
    int numLectures;
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

        //TODO: This button is fully functinoal, just need to make it look good in UI
        ImageButton resetBTN = findViewById(R.id.resetBTN);
        resetBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make user log in again to reload data when reset
                importData();
            }
        });

        //Initilizes gson, savedPreferences and savedPreferences.Editor
        //Used for storing data to the device for future runs of application
        Gson gson = new Gson();
        if(saved == null){
            saved = this.getApplicationContext().getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE );
        }
        editor = saved.edit();

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
            assert bundle != null;
            numCourses = bundle.getInt("numCourses", 0);
            numLectures = bundle.getInt("numLectures", 0);
            editor.putInt("numCourses", numCourses);
            editor.putInt("numLectures", numLectures);

            //Turns courses and meeting times into JSON data, in order to store to device
            for (int i = 0; i < numCourses; i++) {
                Course courseToAdd = bundle.getParcelable("course" + i + "");
                courses.put(courseToAdd.courseNumber, courseToAdd);
                String courseJSON = gson.toJson(courseToAdd);
                editor.putString("course" + i + "", courseJSON);

            }
            for (int i = 0; i < numLectures; i++) {
                Lecture timeToAdd = bundle.getParcelable("lecture" + i + "");
                meetingTimes.add(timeToAdd);
                String lectureJSON = gson.toJson(timeToAdd);
                editor.putString("lecture"+i+"", lectureJSON);

            }




            //sets boolean to inform app we will not return to this activity from login
            editor.putBoolean("fromLogin", false);
            editor.commit();

            //Fill the schedule with this parsed data
            fillSchedule();

        } else {

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


            if (courses.isEmpty() || meetingTimes.isEmpty()) {
                //If the data is unsuccessfully loaded/doesn't exist, go to the login screen to scrape data
                importData();
            } else {
                //If the data exists, use it to fill the schedule
                fillSchedule();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // If the orientation has just changed to landscape, send the user to the Landscape schedule activity
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Intent turnLandscape = new Intent();
            turnLandscape.setClass(PortraitSchedule.this, LandscapeSchedule.class);
            startActivity(turnLandscape);
        }
    }

    public void importData(){
        //intent for login
        editor.clear();
        editor.commit();
        Intent goToLogin = new Intent();
        goToLogin.setClass(PortraitSchedule.this, WebDummy.class);
        startActivity(goToLogin);
    }

    public static int convertDpToPix(int dp){
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
        Calendar calendar = Calendar.getInstance();
        int dayI = calendar.get(Calendar.DAY_OF_WEEK);
        char dayC = 'M';
        TextView curDayTextView = (TextView)findViewById(R.id.TextViewForDay);
        curDayTextView.setText(getResources().getString(R.string.monday));
        switch(dayI){
            case Calendar.MONDAY:
                dayC = 'M';
                curDayTextView.setText(getResources().getString(R.string.monday));
                break;
            case Calendar.TUESDAY:
                dayC = 'T';
                curDayTextView.setText(getResources().getString(R.string.tuesday));
                break;
            case Calendar.WEDNESDAY:
                dayC = 'W';
                curDayTextView.setText(getResources().getString(R.string.wednesday));
                break;
            case Calendar.THURSDAY:
                dayC = 'R';
                curDayTextView.setText(getResources().getString(R.string.thursday));
                break;
            case Calendar.FRIDAY:
                dayC = 'F';
                curDayTextView.setText(getResources().getString(R.string.friday));
                break;
            default:
                dayC = 'M';
                curDayTextView.setText(getResources().getString(R.string.monday));
                break;
        }
        //goes through the meetingTimes list to create a button for each class
        for(int i = 0; i < meetingTimes.size(); i++){
            //Only Add classes that occur "today", adding duplicate classes causes crash
            if(meetingTimes.get(i).day == dayC) {
                //button representing the current class in the list
                Button cbtn = new Button(this);
                //id = the index of the class in the list
                cbtn.setId(i);
                cbtn.setTag(meetingTimes.get(i));
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
                currentLayout = (RelativeLayout) findViewById(R.id.singleDayRelativeLayout);
                final int currentCount = i;
                clickedClassOffset = startMargin;
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
                        bundle.putInt("offset", clickedClassOffset);
                        //opening the fragment
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction transaction = fm.beginTransaction();
                        Fragment fragment1 = new CourseDetails();
                        fragment1.setArguments(bundle);
                        transaction.replace(R.id.detailsContainerPortrait, fragment1);
                        transaction.commit();
                        transaction.addToBackStack(null);
                    }
                });
                currentLayout.addView(cbtn);
            }
        }
        // break point to check data
        System.out.print("break");
    }
}
