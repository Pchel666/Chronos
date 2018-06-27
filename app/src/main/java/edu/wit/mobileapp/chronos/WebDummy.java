package edu.wit.mobileapp.chronos;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
        final WebView webview = (WebView)findViewById(R.id.webView1);
        webview.setWebViewClient(new WebViewClient());
        final Context contextToPass = this;

        // Enable JavaScript
        webview.getSettings().setJavaScriptEnabled(true);

        // Zoom out the page to fit the display
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);

        // Provide pinch zoom operation
        webview.getSettings().setBuiltInZoomControls(true);

        webview.loadUrl("https://cas.wit.edu");


        Button myButton = findViewById(R.id.button);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlOfWebView = webview.getUrl();
                GetData getData = new GetData(contextToPass, "https://cas.wit.edu");
                getData.execute();
            }
        });



    }


    class GetData extends AsyncTask<String, Void, String> {
        private Context mContext;
        private String currentUrl;

        GetData(Context context, String curUrl){
            this.mContext=context;
            this.currentUrl = curUrl;
        }


        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(currentUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String read;
                while ((read = br.readLine())!=null){
                    sb.append(read);
                }
                br.close();
                result = sb.toString();

            } catch (IOException e){
                Log.d(TAG, "Error: " + e.toString());

            }
            return result;
        }

        @Override
        protected void onPostExecute(String data) {
            Log.v(TAG, "data = "+ data);
        }
    }


}
