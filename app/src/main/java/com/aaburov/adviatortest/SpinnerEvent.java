package com.aaburov.adviatortest;

/**
 * Created by Giorgio on 23.05.2015.
 */
public class SpinnerEvent {

    int data;
    static final int OFF=0;
    static final int ON=1;

    public SpinnerEvent(int data){
        if(ON==data) this.data = data; else this.data=OFF;
    }
    public int getData(){
        return data;
    }

}
