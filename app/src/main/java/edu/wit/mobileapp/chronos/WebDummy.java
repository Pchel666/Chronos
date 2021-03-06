package edu.wit.mobileapp.chronos;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebDummy extends AppCompatActivity {

    private final String TAG = "myApp";

    int click = -1;
    int page = 0;

    WebView webview;
    TextView loadingTV;

    Map<String, Course> courses;
    List<Lecture> lectures;
    List<String> links;

    private static final String PREFS_NAME = "edu.wit.mobileapp.chronos.PortraitSchedule";
    /** This application's preferences */
    private static SharedPreferences saved;


    /** This application's settings editor*/
    private static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_dummy);

        webview = (WebView)findViewById(R.id.webView1);
        webview.setWebViewClient(new WebViewClient());

        loadingTV = findViewById(R.id.loadingTextView);


        courses = new HashMap<String, Course>();
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

        // Ensures user has to enter credentials when arriving at this activity
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        if(saved == null){
            saved = this.getApplicationContext().getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE );
        }
        editor = saved.edit();


        //Load leopardweb
        webview.loadUrl("https://cas.wit.edu/");

        //Web View Client to headlessly browse the web page
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if(url.equals("https://prodweb2.wit.edu/SSBPROD/twbkwbis.P_GenMenu?name=bmenu.P_MainMnu")) {
                    //If the main menu has loaded, follow the "Student and Financial Aid" Link
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) webview.getLayoutParams();
                    params.height = 0;
                    webview.setLayoutParams(params);
                    webview.loadUrl("https://prodweb2.wit.edu/SSBPROD/twbkwbis.P_GenMenu?name=bmenu.P_StuMainMnu");

                    //Let user know their data is being gathered
                    loadingTV.setText("Please Wait\nGathering Data from LeopardWeb");

                } else if(url.equals("https://prodweb2.wit.edu/SSBPROD/twbkwbis.P_GenMenu?name=bmenu.P_StuMainMnu")) {
                    //If the student menu has loaded, follow the "Registration" Link
                    webview.loadUrl("https://prodweb2.wit.edu/SSBPROD/twbkwbis.P_GenMenu?name=bmenu.P_RegMnu");

                } else if(url.equals("https://prodweb2.wit.edu/SSBPROD/twbkwbis.P_GenMenu?name=bmenu.P_RegMnu")) {
                    //if the registration menu has loaded, follow the "Week at a Glance" link
                    webview.loadUrl("https://prodweb2.wit.edu/SSBPROD/bwskfshd.P_CrseSchd");

                } else if(url.contains("https://prodweb2.wit.edu/SSBPROD/bwskfshd.P_CrseSchd")) {

                    //If Week at a Glance OR Course details page loads...

                    //if the links haven't been gathered yet, call the click method for the first time to get the links
                    if(links.isEmpty()){
                        clickLink();
                    }
                    while(links.isEmpty()){
                        //wait for list of links to populate
                    }

                    //Update user on import progress
                    loadingTV.setText(String.format(getString(R.string.import_progress), ((double)click / (double) links.size())*100));


                    //Click the next link involved with to scraping pages
                    clickLink();
                } else if(url.equals("https://cas.wit.edu/cas/login?service=https%3A%2F%2Fprodweb2.wit.edu%3A443%2Fssomanager%2Fc%2FSSB")){
                    //do nothing, let user interact with this page

                } else {

                    //if any other page is loaded, bring user back to login screen to prevent user from getting stuck
                    webview.loadUrl("https://cas.wit.edu/cas/login?service=https%3A%2F%2Fprodweb2.wit.edu%3A443%2Fssomanager%2Fc%2FSSB");
                }
            }
        });

    }


    void clickLink(){

        // Week at a glance page doesn't have course name, and not all rows have 7 columns...
        // Gets data from course details pages
        if(page==0) { // If the current page is Week at Glance
            if (click == -1){ // If button hasn't yet been clicked

                // Create a list of 'course details' links
                webview.loadUrl(
                        "javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0]" +
                                ".getElementsByTagName('table')[7].innerHTML);"
                );

                // Inform 'click' variable that the button has been clicked
                click=0;


            } else if(getLinksLeft()==0) { // If there are no more course detail links to follow...

                // Delcare an Intent for the Schedule activity
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

                //Add the counts of courses and meeting times to dataToSend
                dataToSchedule.putExtra("numCourses", courses.size());
                dataToSchedule.putExtra("numLectures", lectures.size());


                //Send the data to the schedule activity
                editor.putBoolean("fromLogin", true);
                editor.commit();
                startActivity(dataToSchedule);
            } else { // When there are still links yet to be clicked

                // Follow the link to course details page
                webview.loadUrl("https://prodweb2.wit.edu"+links.get(click).replace("amp;",""));
                page=1; // WebView is now on a Details Page
                click++; // Set the next details page to go to

            }


        } else if (page == 1) { //If page is a courseDetailsPage

            // get info from courseDetailsPage
            webview.loadUrl(
                    "javascript:window.INTERFACE.processContent(document.getElementsByClassName('pagebodydiv')[0].innerHTML);"
            );

            // Go Back to Week at a Glance
            webview.loadUrl(
                    "javascript:window.history.back();"
            );

        }
    }



    // Method that returns the number of links that haven't been clicked
    int getLinksLeft() {
        return links.size()-click;
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


                if(page == 0){
                    // If processContent reaches here, that means this is the first time the button is clicked
                    // Therefore, this button click should create the list of links to all the course details pages

                    // Add links to the links list
                    Matcher linkMatcher = linkPattern.matcher(aContent);
                    do {
                        if (linkMatcher.find() && linkMatcher.groupCount() == 1) {
                            if(!links.contains(linkMatcher.group(1))){
                                //Only add a link to the list if it has not already been added
                                links.add(linkMatcher.group(1));
                            }
                        }
                    } while (!linkMatcher.hitEnd());

                } else {
                    //process the request to get course details

                    String courseName = "";
                    String courseNumber = "";
                    String courseId = "";

                    // Matches courseTitle and courseNumber
                    Matcher titleMatcher = titlePattern.matcher(aContent);
                    if (titleMatcher.find() && titleMatcher.groupCount() == 3) {
                        courseName = titleMatcher.group(1).trim();
                        courseNumber = titleMatcher.group(2).trim();
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
                    if (!courses.isEmpty() && courses.containsKey(courseNumber)) {
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
                        //time is in the format of 1-2 numbers for hour, then a colon, then 2 numbers for minutes, a space, then am/pm
                        lectureToAdd.startTime = details.get(8).split("-")[0].trim();
                        lectureToAdd.endTime = details.get(8).split("-")[1].trim();
                        lectureToAdd.place = details.get(10);
                        courseToAdd.addLecture(lectureToAdd);

                        //Add the lecture to the list of meeting times
                        lectures.add(lectureToAdd);

                    }

                    // if the course already existed in the courses Map, the course has just been updated
                    // otherwise, the course will be added to the courses Map here
                    if (!courses.containsKey(courseNumber)) {
                        courses.put(courseNumber, courseToAdd);
                    }
                    page=0; // The page is now Week at a glance
                }
            }
        }
    }
}


