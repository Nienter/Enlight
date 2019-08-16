package com.android.pixel;

public abstract class AdDisplayListener {
    public void onAdDisplay(AdNetwork network, AdType type, String adId, Object... params) {}

    public void onAdClicked(AdNetwork network, AdType type, String adId, Object... params) {}

    public void onAdClosed(AdNetwork network, AdType type, String adId, Object... params) {}
}
