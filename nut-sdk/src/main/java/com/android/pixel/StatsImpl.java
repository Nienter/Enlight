package com.android.pixel;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.flurry.android.FlurryAgent;

import java.util.Map;

public class StatsImpl {
    private static AppEventsLogger sFbLogger = null;
    public static void init(Application app, String apiKey){
        new FlurryAgent.Builder()
                .withLogEnabled(AppUtil.sDebug)
                .withLogLevel(AppUtil.sDebug ? Log.VERBOSE:Log.ASSERT)
                .withCaptureUncaughtExceptions(true)
                .withContinueSessionMillis(10*DateUtils.SECOND_IN_MILLIS)
                .build(app, apiKey);

        setFlurryUserId();

        FacebookSdk.sdkInitialize(app);
        if(AppUtil.sDebug){
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        }
        sFbLogger = AppEventsLogger.newLogger(app);
    }

    private static volatile boolean sInitUserId = false;
    private static void setFlurryUserId(){
        if(!sInitUserId){
            synchronized (StatsImpl.class){
                if(!sInitUserId){
                    String userId = Identity.getInst().getId();
                    if(!TextUtils.isEmpty(userId)){
                        FlurryAgent.setUserId(userId);
                        sInitUserId = true;
                    }
                }
            }
        }
    }

    public static void recordEvt(String name, Map<String, String> param){
        setFlurryUserId();
        if(TextUtils.isEmpty(name))
            return;

        if(param!=null) {
            FlurryAgent.logEvent(name, param);
        } else {
            FlurryAgent.logEvent(name);
        }

        if(param!=null){
            Bundle bundle = new Bundle();
            for (Map.Entry<String, String> entry:param.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
            sFbLogger.logEvent(name, bundle);

        }else {
            sFbLogger.logEvent(name);
        }
    }



    public static void logAddedToCartEvent (String contentData, String contentId, String contentType, String currency, double price) {
        Bundle params = new Bundle();

        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);

        sFbLogger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, price, params);
    }

    public static void logPurchaseEvent(String contentData, String contentId, String contentType, String currency, double price){
        Bundle params = new Bundle();

        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);


        sFbLogger.logEvent(
                AppEventsConstants.EVENT_NAME_PURCHASED, price, params);
    }
}
