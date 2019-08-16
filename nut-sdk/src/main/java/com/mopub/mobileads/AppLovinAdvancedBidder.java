package com.mopub.mobileads;

import android.content.Context;
import com.applovin.sdk.AppLovinSdk;
import com.mopub.common.MoPubAdvancedBidder;

public class AppLovinAdvancedBidder implements MoPubAdvancedBidder {
    public AppLovinAdvancedBidder() {
    }

    public String getCreativeNetworkName() {
        return "applovin_sdk";
    }

    public String getToken(Context context) {
        return AppLovinSdk.getInstance(context).getAdService().getBidToken();
    }
}
