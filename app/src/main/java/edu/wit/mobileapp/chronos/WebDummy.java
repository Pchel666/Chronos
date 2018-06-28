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

import java.util.HashMap;
import java.util.Map;

public class WebDummy extends AppCompatActivity {

    private final String TAG = "myApp";
    int click = 0;
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

        // Scrape button stuff
        myButton = findViewById(R.id.button);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uses JavaScript to get html data from the current page

                //TODO: Week at a glance page doesn't have course name, and format isn't formatted right to just read from there, need to implement navigation to details pages
                if(click == 0) { //follow the link to course details
                    webview.loadUrl(
                            "javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0]" +
                                    ".getElementsByTagName('table')[7].getElementsByTagName('tr')[1]" +
                                    ".getElementsByTagName('a')[0].click());"
                    );
                    click=1;
                } else if (click == 1) { //get info from courseDetailsPage and return to week at glance

                    //TODO: currently only gets details for one page
                    webview.loadUrl(
                            "javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].getElementsByClassName('datadisplaytable')[1].getElementsByTagName('tr')[1].innerText);"
                    );

                    webview.loadUrl(
                            "javascript:window.history.back();"
                    );
                    click=0;
                }


            }

        });




    }



    // An instance of this class will be registered as a JavaScript interface
    class MyJavaScriptInterface
    {
        public MyJavaScriptInterface()
        {
        }
        @SuppressWarnings("unused")
        @JavascriptInterface
        public void processContent(String aContent)
        {
            if(!aContent.equals("undefined")) {


                Log.v(TAG, aContent);

                //TODO: currently just a test to create a Course Object with adding Lectures
                String[] detail = aContent.split("\t|-");


                //Creates a course to add to the schedule
                Course courseToAdd;
                if (!courses.isEmpty()&&courses.containsKey(detail[0])) {
                    courseToAdd = courses.get(detail[0]);
                } else {
                    courseToAdd = new Course(detail[0]);
                    courseToAdd.instructor = detail[8];
                }

                //Adds meeting times to course
                for (int i = 0; i < detail[3].length(); i++) {
                    Lecture lectureToAdd = new Lecture();
                    lectureToAdd.day = detail[3].charAt(i);
                    lectureToAdd.startTime = detail[1];
                    lectureToAdd.endTime = detail[2];
                    lectureToAdd.place = detail[4];
                    courseToAdd.addLecture(lectureToAdd);
                }

                // if the course already existed in the courses Map, the course has just been updated
                // otherwise, the course will be added to the courses Map here
                if (!courses.containsKey(detail[0])) {
                    courses.put(detail[0], courseToAdd);
                }
            }
        }
    }
}


