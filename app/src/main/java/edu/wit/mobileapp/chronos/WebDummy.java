package edu.wit.mobileapp.chronos;

import android.content.Context;
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

public class WebDummy extends AppCompatActivity {

    private final String TAG = "myApp";
    int click = 0;
    int page = 0;
    WebView webview;
    Button myButton;
    Map<String, Course> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_dummy);
        webview = (WebView)findViewById(R.id.webView1);
        webview.setWebViewClient(new WebViewClient());
        final Context contextToPass = this;

        courses = new HashMap<String, Course>();

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
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Week at a glance page doesn't have course name, and format isn't formatted right to just read from there
                //Currently only gets first link's course details
                //TODO: implement navigation to ALL details pages
                if(page==0) {
                    //follow the link to course details page
                    webview.loadUrl(
                            "javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0]" +
                                    ".getElementsByTagName('table')[7].getElementsByTagName('tr')[1]" +
                                    ".getElementsByTagName('a')[0].click());"
                    );
                    myButton.setText("Click to Scrape Details"); // Adapt the button text for the page user is on
                    page=1; // WebView is now on a Details Page
                    click++; // Set the next details page to go to

                } else if (page == 1) {
                    // get info from courseDetailsPage
                    webview.loadUrl(
                            "javascript:window.INTERFACE.processContent(document.getElementsByClassName('pagebodydiv')[0].innerHTML);"
                    );

                    //return to Week at a Glance
                    webview.loadUrl(
                            "javascript:window.history.back();"
                    );

                    page=0; // WebView is now on week at a glance Page
                    myButton.setText("SCRAPE (ONLY ON CORRECT PAGE)");   // Adapt the button text for the page user is on
                }


            }

        });




    }



    // An instance of this class will be registered as a JavaScript interface
    class MyJavaScriptInterface
    {
        //Patterns to match to the scraped HTML
        Pattern titlePattern = Pattern.compile("<caption class=\"captiontext\">(.*)-(.*)-.*</caption>");
        Pattern detailsPattern = Pattern.compile("<td class=\"dddefault\">(.*)</td>");


        public MyJavaScriptInterface()
        {
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void processContent(String aContent)
        {
            // only gets data if page has data
            if(!aContent.equals("undefined")) {

                String courseName = "";
                String courseId = "";

                // Matches courseTitle and courseNumber
                Matcher titleMatcher = titlePattern.matcher(aContent);
                if(titleMatcher.find() && titleMatcher.groupCount()==2) {
                    courseName = titleMatcher.group(1);
                    courseId = titleMatcher.group(2);
                }

                // Matches Meeting Time Details
                List<String> details = new ArrayList<>();
                Matcher detailsMatcher = detailsPattern.matcher(aContent);
                do{
                    if(detailsMatcher.find() && detailsMatcher.groupCount()==1) {
                        details.add(detailsMatcher.group(1));
                    }
                } while(!detailsMatcher.hitEnd());



                Log.v(TAG, aContent);


                // Creates a course to add to the schedule
                Course courseToAdd;
                if (!courses.isEmpty() && courses.containsKey(courseId)) {
                    courseToAdd = courses.get(courseId);
                } else {
                    courseToAdd = new Course(courseId);
                    courseToAdd.courseName = courseName;
                    courseToAdd.instructor = details.get(13).split("\\(<abbr")[0];
                }

                // Adds meeting times to course
                for (int i = 0; i < details.get(9).length(); i++) {
                    Lecture lectureToAdd = new Lecture();
                    lectureToAdd.day = details.get(9).charAt(i);
                    lectureToAdd.startTime = details.get(8).split("-")[0];
                    lectureToAdd.endTime = details.get(8).split("-")[1];
                    lectureToAdd.place = details.get(10);
                    courseToAdd.addLecture(lectureToAdd);
                }

                // if the course already existed in the courses Map, the course has just been updated
                // otherwise, the course will be added to the courses Map here
                if (!courses.containsKey(courseId)) {
                    courses.put(courseId, courseToAdd);
                }
            }
        }
    }
}


