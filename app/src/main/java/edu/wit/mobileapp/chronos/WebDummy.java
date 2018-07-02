package edu.wit.mobileapp.chronos;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PatternMatcher;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebDummy extends AppCompatActivity {

    private final String TAG = "myApp";

    int click = -1;
    int page = 0;

    WebView webview;
    Button myButton;

    List<String> coursesRead;
    Map<String, Course> courses;
    List<Lecture> lectures;
    List<String> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_dummy);
        webview = (WebView)findViewById(R.id.webView1);
        webview.setWebViewClient(new WebViewClient());
        final Context contextToPass = this;

        courses = new HashMap<String, Course>();
        coursesRead = new ArrayList<>();
        lectures = new ArrayList<>();
        links = new ArrayList<>();

        // Enable JavaScript
        webview.getSettings().setJavaScriptEnabled(true);

        // Zoom out the page to fit the display
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);

        // Provide pinch zoom operation
        webview.getSettings().setBuiltInZoomControls(true);

        // JavaScript for html scraping
        webview.addJavascriptInterface(new MyJavaScriptInterface(),"INTERFACE");

        //Load leopardweb
        webview.loadUrl("https://cas.wit.edu");

        // Button that uses JavaScript to scrape html data from the webview's current page
        myButton = findViewById(R.id.button);
        myButton.setText("Count MeetingTimes");
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Week at a glance page doesn't have course name, and not all rows have 7 columns
                // Gets data from course details pages
                if(page==0) {
                    if (click == -1){
                        webview.loadUrl(
                                "javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0]" +
                                        ".getElementsByTagName('table')[7].innerHTML);"
                        );
                        click=0;
                    } else if(getLinksLeft()==0) { // If there are no more course detail links to follow...
                        //Delcare an activity for the schedule
                        Intent dataToSchedule = new Intent();
                        dataToSchedule.setClass(WebDummy.this, PortraitSchedule.class);

                        //Add the courses to the dataToSend
                        int i=0;
                        for(Course course:courses.values()){
                            dataToSchedule.putExtra("course"+i+"",course);
                            i++;

                        }

                        //Add the meeting times to the dataToSend
                        i=0;
                        for(Lecture time:lectures){
                            dataToSchedule.putExtra("lecture"+i+"",time);
                            i++;

                        }

                        dataToSchedule.putExtra("numCourses", courses.size());
                        dataToSchedule.putExtra("numLectures", lectures.size());


                        //Send the data to the schedule activity
                        startActivity(dataToSchedule);
                    } else {

                        //follow the link to course details page
                        webview.loadUrl("https://prodweb2.wit.edu"+links.get(click).replace("amp;",""));
                        myButton.setText("Click to Scrape Details Page"); // Adapt the button text for the page user is on
                        page=1; // WebView is now on a Details Page
                        click++; // Set the next details page to go to

                    }


                } else if (page == 1) {
                    // get info from courseDetailsPage
                    webview.loadUrl(
                            "javascript:window.INTERFACE.processContent(document.getElementsByClassName('pagebodydiv')[0].innerHTML);"
                    );

                    //return to Week at a Glance
                    webview.loadUrl(
                            "javascript:window.history.back();"
                    );

                    // Adapt the button text for the page user is on, according to how many links are left to scrape
                    if (getLinksLeft()>0){
                        myButton.setText(String.format("SCRAPE NEXT LINK (%d Remaining)", getLinksLeft()));
                    } else {
                        myButton.setText("Click to Import Courses To Chronos");
                    }
                }


            }

            int getLinksLeft() {
                return links.size()-click;
            }

        });




    }



    // An instance of this class will be registered as a JavaScript interface
    class MyJavaScriptInterface
    {

        //Pattern to find the links to various Details Pages
        Pattern linkPattern = Pattern.compile("<a href=\"(.*)\">");

        //Patterns to match to the scraped HTML
        Pattern titlePattern = Pattern.compile("<caption class=\"captiontext\">(.*)-(.*)-(.*)</caption>");
        Pattern detailsPattern = Pattern.compile("<td class=\"dddefault\">(.*)</td>");


        public MyJavaScriptInterface()
        {
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void processContent(String aContent)
        {
            // only gets data if page has data to get
            if(!aContent.equals("undefined")) {


                if(page == 0){  //process the request to count the links

                    // Add links to the links list
                    Matcher linkMatcher = linkPattern.matcher(aContent);
                    do {
                        if (linkMatcher.find() && linkMatcher.groupCount() == 1) {
                            links.add(linkMatcher.group(1));
                        }
                    } while (!linkMatcher.hitEnd());

                    myButton.setText(String.format("SCRAPE NEXT LINK (%d Remaining)", links.size()));
                } else { //process the request to get course details

                    String courseName = "";
                    String courseNumber = "";
                    String courseId = "";

                    // Matches courseTitle and courseNumber
                    Matcher titleMatcher = titlePattern.matcher(aContent);
                    if (titleMatcher.find() && titleMatcher.groupCount() == 3) {
                        courseName = titleMatcher.group(1);
                        courseNumber = titleMatcher.group(2);
                        courseId = titleMatcher.group(2)+titleMatcher.group(3);
                    }

                    // Matches Meeting Time Details
                    List<String> details = new ArrayList<>();
                    Matcher detailsMatcher = detailsPattern.matcher(aContent);
                    do {
                        if (detailsMatcher.find() && detailsMatcher.groupCount() == 1) {
                            details.add(detailsMatcher.group(1));
                        }
                    } while (!detailsMatcher.hitEnd());


                    Log.v(TAG, aContent);


                    // Creates a course to add to the schedule
                    Course courseToAdd;
                    if (!courses.isEmpty() && courses.containsKey(courseNumber) && !coursesRead.contains(courseId)) {
                        courseToAdd = courses.get(courseNumber);
                    } else {
                        courseToAdd = new Course(courseNumber);
                        courseToAdd.courseName = courseName;
                        courseToAdd.instructor = details.get(13).split("\\(<abbr")[0];
                    }

                    // Adds meeting times to course
                    for (int i = 0; i < details.get(9).length(); i++) {
                        //Build Lecture Data
                        Lecture lectureToAdd = new Lecture();
                        lectureToAdd.courseNumber = courseNumber;
                        lectureToAdd.day = details.get(9).charAt(i);
                        lectureToAdd.startTime = details.get(8).split("-")[0];
                        lectureToAdd.endTime = details.get(8).split("-")[1];
                        lectureToAdd.place = details.get(10);
                        courseToAdd.addLecture(lectureToAdd);

                        //Add the lecture to the list of meeting times if it hasn't yet been added
                        if (!coursesRead.contains(courseId)) {
                            lectures.add(lectureToAdd);
                        }
                    }

                    // if the course already existed in the courses Map, the course has just been updated
                    // otherwise, the course will be added to the courses Map here
                    if (!courses.containsKey(courseNumber)) {
                        courses.put(courseNumber, courseToAdd);
                    }

                    //Ensure the same details page isn't read twice
                    coursesRead.add(courseId);
                    page=0;
                }
            }
        }
    }
}


