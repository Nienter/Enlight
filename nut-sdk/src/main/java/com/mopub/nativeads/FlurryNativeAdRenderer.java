package com.mopub.nativeads;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mopub.nativeads.FlurryCustomEventNative.FlurryMediationNativeAd;
import java.util.WeakHashMap;

public class FlurryNativeAdRenderer implements MoPubAdRenderer<FlurryMediationNativeAd> {
    @NonNull
    private final FlurryViewBinder mViewBinder;
    @NonNull
    private final WeakHashMap<View, FlurryNativeViewHolder> mViewHolderMap;

    public FlurryNativeAdRenderer(@NonNull FlurryViewBinder viewBinder) {
        this.mViewBinder = viewBinder;
        this.mViewHolderMap = new WeakHashMap();
    }

    @NonNull
    public View createAdView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater.from(context).inflate(this.mViewBinder.staticViewBinder.layoutId, parent, false);
    }

    public void renderAdView(@NonNull View view, @NonNull FlurryMediationNativeAd ad) {
        FlurryNativeViewHolder flurryNativeViewHolder = (FlurryNativeViewHolder)this.mViewHolderMap.get(view);
        if (flurryNativeViewHolder == null) {
            flurryNativeViewHolder = FlurryNativeViewHolder.fromViewBinder(view, this.mViewBinder);
            this.mViewHolderMap.put(view, flurryNativeViewHolder);
        }

        this.update(flurryNativeViewHolder, ad);
        NativeRendererHelper.updateExtras(flurryNativeViewHolder.staticNativeViewHolder.mainView, this.mViewBinder.staticViewBinder.extras, ad.getExtras());
        this.setViewVisibility(flurryNativeViewHolder, View.VISIBLE);
    }

    public boolean supports(@NonNull BaseNativeAd nativeAd) {
        return nativeAd instanceof FlurryMediationNativeAd;
    }

    private void update(FlurryNativeViewHolder viewHolder, FlurryMediationNativeAd ad) {
        NativeImageHelper.loadImageView(ad.getIconImageUrl(), viewHolder.staticNativeViewHolder.iconImageView);
        NativeRendererHelper.addTextView(viewHolder.staticNativeViewHolder.callToActionView, ad.getCallToAction());

        NativeRendererHelper.addTextView(viewHolder.staticNativeViewHolder.titleView, ad.getTitle());
        NativeImageHelper.loadImageView(ad.getSponsoredMarkerImageUrl(),
                viewHolder.staticNativeViewHolder.privacyInformationIconImageView);
        NativeRendererHelper.addTextView(viewHolder.advertiserView, ad.getAdvertiserName());

        if (ad.isVideoAd()) {
            if (viewHolder.videoView != null) {
                viewHolder.videoView.setVisibility(View.VISIBLE);
                ad.loadVideoIntoView(viewHolder.videoView);
            }

            if (viewHolder.staticNativeViewHolder.mainImageView != null) {
                viewHolder.staticNativeViewHolder.mainImageView.setVisibility(View.GONE);
            }
        } else {
            if (viewHolder.videoView != null) {
                viewHolder.videoView.setVisibility(View.GONE);
            }

            if (viewHolder.staticNativeViewHolder.mainImageView != null) {
                viewHolder.staticNativeViewHolder.mainImageView.setVisibility(View.VISIBLE);
                NativeImageHelper.loadImageView(ad.getMainImageUrl(), viewHolder.staticNativeViewHolder.mainImageView);
            }
        }

    }

    private void setViewVisibility(@NonNull FlurryNativeViewHolder viewHolder, int visibility) {
        if (viewHolder.staticNativeViewHolder.mainView != null) {
            viewHolder.staticNativeViewHolder.mainView.setVisibility(visibility);
        }

    }




    public static class FlurryViewBinder{
        ViewBinder staticViewBinder;
        int advertiserNameViewId;
        int videoViewId;

        private FlurryViewBinder(@NonNull Builder builder) {
            this.staticViewBinder = builder.staticViewBinder;
            this.videoViewId = builder.videoViewId;
            this.advertiserNameViewId = builder.advertiserNameViewId;
        }

        public static final class Builder {
            ViewBinder staticViewBinder;
            int videoViewId;
            int advertiserNameViewId;

            public Builder(ViewBinder staticViewBinder) {
                this.staticViewBinder = staticViewBinder;
            }

            @NonNull
            public final Builder videoViewId(int videoViewId) {
                this.videoViewId = videoViewId;
                return this;
            }

            @NonNull
            public final Builder advertiserNameViewId(int id) {
                this.advertiserNameViewId = id;
                return this;
            }

            @NonNull
            public final FlurryViewBinder build() {
                return new FlurryViewBinder(this);
            }
        }
    }



    private static class FlurryNativeViewHolder {
        private final StaticNativeViewHolder staticNativeViewHolder;
        private final ViewGroup videoView;
        private final TextView  advertiserView;

        private FlurryNativeViewHolder(StaticNativeViewHolder staticNativeViewHolder, ViewGroup videoView, TextView advertiserView) {
            this.staticNativeViewHolder = staticNativeViewHolder;
            this.videoView = videoView;
            this.advertiserView = advertiserView;
        }

        static FlurryNativeViewHolder fromViewBinder(View view, FlurryViewBinder viewBinder) {
            StaticNativeViewHolder staticNativeViewHolder = StaticNativeViewHolder.fromViewBinder(view, viewBinder.staticViewBinder);
            ViewGroup videoView = view.findViewById(viewBinder.videoViewId);
            TextView  advertiserView = view.findViewById(viewBinder.advertiserNameViewId);

            return new FlurryNativeViewHolder(staticNativeViewHolder, videoView, advertiserView);
        }
    }
}
