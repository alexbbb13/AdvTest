package com.aaburov.adviatortest;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


/*
Design and build android activity for serving full screen html ads.

        When Activity is shown request for ad by sending HTTP POST request with parameter id with value of sim card IMSI (subscriber id) to http://www.505.rs/adviator/index.php

        Example:

        POST /adviator/index.php HTTP/1.1
        Host: www.505.rs
        Cache-Control: no-cache
        Postman-Token: abd93bb8-2857-2fd0-7679-0b25087e1d35
        Content-Type: application/x-www-form-urlencoded

        id=85950205030644900

        {
        "status":"OK",
        "message":"display full screen ad",
        "url":"http://www.505.rs/adviator/ad.html"
        }

        If status is equal "OK" use returned "url" and load ad into Activity webView.

        If status is not equal "OK" show dialog with 'message' text and OK button. Clicking OK button will dismiss both dialog and activity.

        While requesting for ad and loading ad html show spinner in center of screen and transparent background.

        Once html ad is loaded hide spinner and show ad.

        When user clicks ad link close activity and open native android browser with clicked url.

        Activity should work in both portrait and landscape mode.

        Jovan
*/

public class MainActivity extends ActionBarActivity {

    private EventBus bus = EventBus.getDefault();
    private Networking mNetworking;
    private Phone mPhone;
    private JSONObject serverResponse;
    private WebView mWebView;
    private ProgressBar mProgressSpinner;
    final static String AD_URL="http://www.505.rs/adviator/index.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public void onResume(){
        super.onResume();
        runTasks();
    }

    public void onEvent(ExceptionEvent event){
        bus.post(new SpinnerEvent(SpinnerEvent.OFF));
        Toast.makeText(this, event.getData(), Toast.LENGTH_LONG).show();
        Log.d("Exception:", event.getData());
    }

    public void onEvent(HTTPResponseEvent event){
        try {
            serverResponse = new JSONObject(event.getData());
            if(serverResponse.get("status").equals("OK")) {
                openWebView((String) serverResponse.get("url"));
            } else {
                displayFinalDialog((String)serverResponse.get("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onEvent(SpinnerEvent event){
        if(event.getData()==SpinnerEvent.ON) mProgressSpinner.setVisibility(View.VISIBLE);
        if(event.getData()==SpinnerEvent.OFF) mProgressSpinner.setVisibility(View.INVISIBLE);
    }

    void init(){
        mNetworking= new Networking(this);
        mPhone= new Phone (this);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                startBrowser(urlNewString);
                finish();
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                bus.post(new SpinnerEvent(SpinnerEvent.OFF));
            }
        });
        mProgressSpinner= (ProgressBar) findViewById(R.id.spinner);
        bus.register(this);
    }

    void runTasks(){
        String mIMSI=mPhone.getDeviceID();
        //mIMSI="85950205030644900"; //test
        if(mNetworking.isConnected()) {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", mIMSI));
            mNetworking.postAsync(AD_URL, params);
        } else {
            bus.post(new ExceptionEvent("No network"));
        }

    }

    private void displayFinalDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void openWebView(String url) {
        if(null==mWebView)  bus.post(new ExceptionEvent("mWebView is NULL")); else mWebView.loadUrl(url);
    }


    private void startBrowser(String download_link) {
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(download_link));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
            //When user clicks ad link (in WebView) close activity and open native android browser with clicked url.
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (ActivityNotFoundException e) {
            bus.post(new ExceptionEvent("No application can handle this request.Please install a webbrowser"));
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
