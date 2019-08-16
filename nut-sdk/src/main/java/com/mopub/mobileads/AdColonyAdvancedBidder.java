package com.mopub.mobileads;

import android.content.Context;

import com.mopub.common.MoPubAdvancedBidder;

public class AdColonyAdvancedBidder implements MoPubAdvancedBidder {
    public AdColonyAdvancedBidder() {
    }

    public String getToken(Context context) {
        return "1";
    }

    public String getCreativeNetworkName() {
        return "adcolony";
    }
}
