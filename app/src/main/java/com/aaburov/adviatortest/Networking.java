package com.aaburov.adviatortest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Giorgio on 23.05.2015.
 */
public class Networking {
    Context mContext;
    ConnectivityManager connMgr=null;
    NetworkInfo networkInfo;
    private EventBus bus = EventBus.getDefault();

    Networking (Context c) {
        mContext = c;
        connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
    }

    public boolean isConnected(){
        networkInfo = connMgr.getActiveNetworkInfo();
        return  (networkInfo != null && networkInfo.isConnected());
    }

    public void download(String url){
        new downloadAsync().execute(url);
    }

    public void postAsync(String url,ArrayList<NameValuePair> nameValuePair){
        postAsync mpostAsync = new postAsync();
        mpostAsync.setNameValue(nameValuePair);
        mpostAsync.execute(url);
    }

    private class postAsync extends AsyncTask<String, Void, String> {
        ArrayList<NameValuePair> nameValuePairs;
        String result;

        public void setNameValue(ArrayList<NameValuePair> nvp){
            nameValuePairs=nvp;
        }

        @Override
        protected void onPreExecute() {
            bus.post(new SpinnerEvent(SpinnerEvent.ON));
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return postToUrl(params[0]);

            } catch (Exception ex) {

                ex.printStackTrace();

            }
            return result;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            bus.post(new HTTPResponseEvent(result));
            //bus.post(new SpinnerEvent(SpinnerEvent.OFF));
        }


        private String postToUrl(String myurl) throws IOException {
            int responseCode;
            String output="";
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 20000);
            HttpClient httpClient= new DefaultHttpClient(params);
            HttpResponse httpResponse;
            InputStream inputStream=null;
            try{
                HttpPost httpPost=new HttpPost(myurl);
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httpResponse = httpClient.execute(httpPost);
                responseCode = httpResponse.getStatusLine().getStatusCode();
                long responsesize = httpResponse.getEntity().getContentLength();
                HttpEntity entity=httpResponse.getEntity();
                try{
                    inputStream=entity.getContent();
                }
                catch(IllegalStateException ise){
                    bus.post(new ExceptionEvent("Illegal State Exception"));
                    ise.printStackTrace();
                }
                catch(IOException ioe){
                    bus.post(new ExceptionEvent("IO Exception"));
                    ioe.printStackTrace();
                }
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                } catch (UnsupportedEncodingException e) {
                    bus.post(new ExceptionEvent("UnsupportedEncodingException"));
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();

                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String s=sb.toString();

                if(responseCode==200) {
                    output = s;
                    //bus.post(new HTTPResponseEvent(output));
                }
                else{
                    bus.post(new ExceptionEvent("Load page fail:Errorcode" + responseCode + ",Data size:" + responsesize + ",Data content:" + s));
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
             finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
            return output;
    }





}



    private class downloadAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                 bus.post(new ExceptionEvent("Unable to retrieve web page. URL may be invalid."));
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            bus.post(new HTTPResponseEvent(result));
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            //Log.i("downloadUrl", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }



    private String readIt(InputStream is){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            return  sb.toString();
        } catch (Exception e) {
            bus.post(new ExceptionEvent("Error converting result " + e.toString()));
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

    return "";
}



}
