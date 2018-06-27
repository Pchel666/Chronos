package edu.wit.mobileapp.chronos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebDummy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_dummy);
        WebView webview = (WebView)findViewById(R.id.webView1);
        webview.setWebViewClient(new WebViewClient());

        // Enable JavaScript
        webview.getSettings().setJavaScriptEnabled(true);

        // Zoom out the page to fit the display
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);

        // Provide pinch zoom operation
        webview.getSettings().setBuiltInZoomControls(true);

        webview.loadUrl("https://cas.wit.edu");
    }
}
