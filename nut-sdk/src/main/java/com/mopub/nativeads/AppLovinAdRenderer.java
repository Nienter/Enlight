package com.mopub.nativeads;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.pixel.AppUtil;
import com.applovin.sdk.AppLovinSdkUtils;
import com.mopub.common.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static android.view.View.VISIBLE;

public class AppLovinAdRenderer implements MoPubAdRenderer<AppLovinMediationNative.AppLovinMediationNativeAd> {
    private AppLovinViewBinder mViewBinder = null;
    @NonNull
    final WeakHashMap<View, ApplovinNativeViewHolder> mViewHolderMap;

    /**
     * Constructs a native ad renderer with a view binder.
     *
     * @param viewBinder The view binder to use when inflating and rendering an ad.
     */
    public AppLovinAdRenderer(@NonNull final AppLovinViewBinder viewBinder) {
        mViewBinder = viewBinder;
        mViewHolderMap = new WeakHashMap<>();
    }

    @NonNull
    @Override
    public View createAdView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(mViewBinder.getLayoutResourceId(), parent, false);
    }

    @Override
    public void renderAdView(@NonNull View view, @NonNull AppLovinMediationNative.AppLovinMediationNativeAd ad) {
        ApplovinNativeViewHolder viewHolder = mViewHolderMap.get(view) ;
        if(viewHolder == null){
            viewHolder = ApplovinNativeViewHolder.fromViewBinder(view, mViewBinder);
            mViewHolderMap.put(view, viewHolder);
        }

        update(viewHolder, ad);
        NativeRendererHelper.updateExtras(viewHolder.mainView,
                mViewBinder.getExtras(),
                ad.getExtras());
        setViewVisibility(viewHolder, VISIBLE);
    }

    @Override
    public boolean supports(@NonNull BaseNativeAd nativeAd) {
        Preconditions.checkNotNull(nativeAd);
        return nativeAd instanceof AppLovinMediationNative.AppLovinMediationNativeAd;
    }


    private void update(@NonNull final ApplovinNativeViewHolder viewHolder,
                        @NonNull final AppLovinMediationNative.AppLovinMediationNativeAd nativeAd){
        Context appContext = viewHolder.mainView.getContext().getApplicationContext();

        viewHolder.mediaPlaceHolder.removeAllViews();
        viewHolder.mediaPlaceHolder.addView(nativeAd.getMediaView());

        AppLovinSdkUtils.safePopulateImageView(viewHolder.iconImageView,
                Uri.parse(nativeAd.getIconImageUrl()),
                AppLovinSdkUtils.dpToPx(appContext, 50));
        viewHolder.ratingImageView.setImageDrawable(nativeAd.getRatingImage());
        NativeRendererHelper.addTextView(viewHolder.titleView, nativeAd.getTitle());
        NativeRendererHelper.addTextView(viewHolder.textView, nativeAd.getText());
        NativeRendererHelper.addTextView(viewHolder.CTAView, nativeAd.getCta());

        List<View> clickableView = new ArrayList<>();
        clickableView.add(viewHolder.iconImageView);
        clickableView.add(viewHolder.titleView);
        clickableView.add(viewHolder.CTAView);
        clickableView.add(viewHolder.mediaPlaceHolder);

        for (View view:clickableView) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nativeAd.adIsClicked(v);
                }
            });
        }

    }

    private void setViewVisibility(@NonNull final ApplovinNativeViewHolder viewHolder,
                                   final int visibility) {
        if (viewHolder.mainView != null) {
            viewHolder.mainView.setVisibility(visibility);
        }
    }



    public static class AppLovinViewBinder {
        final Builder mData;

        public int getLayoutResourceId(){
            return mData.layoutId;
        }

        public int getIconImgId(){
            return mData.iconImageId;
        }

        public int getRatingImgId(){
            return mData.ratingImageId;
        }

        public int getTitleViewId(){
            return mData.titleId;
        }

        public int getTextViewId(){
            return mData.textId;
        }

        public int getCallToActionViewId(){
            return mData.callToActionId;
        }

        public int getMediaPlaceHolderId(){
            return mData.mediaPlaceHolderId;
        }

        public Map<String, Integer> getExtras(){
            return mData.extras;
        }

        private AppLovinViewBinder(Builder builder){
            mData = builder;
        }

        public static class Builder{
            private final int layoutId;
            private int iconImageId;
            private int ratingImageId;
            private int titleId;
            private int textId;
            private int callToActionId;
            private int mediaPlaceHolderId;

            @NonNull private Map<String, Integer> extras = Collections.emptyMap();

            public Builder(final int layoutId){
                this.layoutId = layoutId;
                this.extras = new HashMap<String, Integer>();
            }

            @NonNull
            public AppLovinViewBinder build() {
                return new AppLovinViewBinder(this);
            }

            @NonNull
            public final Builder iconImageId(final int iconImageId) {
                this.iconImageId = iconImageId;
                return this;
            }

            @NonNull
            public final Builder ratingImageId(final int iconImageId) {
                this.ratingImageId = iconImageId;
                return this;
            }

            @NonNull
            public final Builder titleId(final int titleId) {
                this.titleId = titleId;
                return this;
            }

            @NonNull
            public final Builder textId(final int textId) {
                this.textId = textId;
                return this;
            }

            @NonNull
            public final Builder callToActionId(final int callToActionId) {
                this.callToActionId = callToActionId;
                return this;
            }

            @NonNull
            public final Builder mediaPlaceHolderId(final int id) {
                this.mediaPlaceHolderId = id;
                return this;
            }

            @NonNull
            public final Builder addExtras(final Map<String, Integer> resourceIds) {
                this.extras = new HashMap<String, Integer>(resourceIds);
                return this;
            }

            @NonNull
            public final Builder addExtra(final String key, final int resourceId) {
                this.extras.put(key, resourceId);
                return this;
            }
        }

    }

    private static class ApplovinNativeViewHolder{
        @Nullable View mainView;
        @Nullable ImageView iconImageView;
        @Nullable ImageView ratingImageView;
        @Nullable TextView titleView;
        @Nullable TextView textView;
        @Nullable TextView CTAView;
        @Nullable FrameLayout mediaPlaceHolder;


        static ApplovinNativeViewHolder fromViewBinder(@NonNull  final View view,
                                                       @NonNull  final AppLovinViewBinder viewBinder) {
            final ApplovinNativeViewHolder viewHolder = new ApplovinNativeViewHolder();
            viewHolder.mainView = view;

            try{
                viewHolder.iconImageView = view.findViewById(viewBinder.getIconImgId());
                viewHolder.ratingImageView = view.findViewById(viewBinder.getRatingImgId());
                viewHolder.titleView = view.findViewById(viewBinder.getTitleViewId());
                viewHolder.textView = view.findViewById(viewBinder.getTextViewId());
                viewHolder.CTAView = view.findViewById(viewBinder.getCallToActionViewId());
                viewHolder.mediaPlaceHolder = view.findViewById(viewBinder.getMediaPlaceHolderId());

            } catch (Throwable err){
                if(AppUtil.sDebug) err.printStackTrace();
            }

            return viewHolder;
        }
    }
}
