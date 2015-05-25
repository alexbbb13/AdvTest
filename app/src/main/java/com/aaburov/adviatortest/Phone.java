package com.aaburov.adviatortest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

/**
 * Created by Giorgio on 23.05.2015.
 */
public class Phone {
    Context mContext;

    Phone (Context c) {
        mContext = c;
    }

    public String getDeviceID() {
        String deviceID = null;
        String serviceName = Context.TELEPHONY_SERVICE;
        TelephonyManager m_telephonyManager = (TelephonyManager) mContext.getSystemService(serviceName);
        deviceID = m_telephonyManager.getDeviceId();
        return deviceID;
    }
}
