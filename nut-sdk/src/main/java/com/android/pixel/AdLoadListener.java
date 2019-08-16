package com.android.pixel;

public abstract class AdLoadListener {
    public void onAdLoaded(AdNetwork network, AdType type, Object ad, Object...params){}

    public void onAdError(String err){}
}
