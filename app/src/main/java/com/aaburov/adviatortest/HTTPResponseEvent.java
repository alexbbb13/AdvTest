package com.aaburov.adviatortest;

import android.util.Log;

/**
 * Created by Giorgio on 23.05.2015.
 */
public class HTTPResponseEvent {
        private String data;
        private int hash;
    static final String TAG="ASS_HTTP";

        public HTTPResponseEvent(String url,String data){
            this.data = data;
            hash=url.hashCode();
            Log.d(TAG,"url="+url+"resp="+data);
        }

        public HTTPResponseEvent(String data){
            this.data = data;
        }

        public String getData(String url){
            if( hash==url.hashCode()) return data; else return null;
        }



        public String getData(){
            return data;
        }
}

