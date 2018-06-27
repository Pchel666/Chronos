package edu.wit.mobileapp.chronos;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebDummy extends AppCompatActivity {

    private final String TAG = "myApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_dummy);
        final WebView webview = findViewById(R.id.webView1);
        webview.setWebViewClient(new WebViewClient());
        final Context contextToPass = this;


        // Enable JavaScript
        webview.getSettings().setJavaScriptEnabled(true);

        // Zoom out the page to fit the display
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);

        // Provide pinch zoom operation
        webview.getSettings().setBuiltInZoomControls(true);

        // JavaScript for html scraping
        webview.addJavascriptInterface(new MyJavaScriptInterface(),"INTERFACE");

        webview.loadUrl("https://cas.wit.edu");

        // Scrape button stuff
        Button myButton = findViewById(R.id.button);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uses JavaScript to get html data from the current page
                webview.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
            }
        });
    }

    // An instance of this class will be registered as a JavaScript interface
    class MyJavaScriptInterface
    {
        private MyJavaScriptInterface()
        {
        }
        @SuppressWarnings("unused")
        @JavascriptInterface
        public void processContent(String aContent)
        {
            Log.v(TAG, aContent);
        }
    }
}


