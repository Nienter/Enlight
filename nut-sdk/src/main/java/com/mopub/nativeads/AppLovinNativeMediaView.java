package com.mopub.nativeads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.pixel.AppUtil;
import com.android.pixel.R;
import com.applovin.nativeAds.AppLovinNativeAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkUtils;

public class AppLovinNativeMediaView extends FrameLayout
        implements TextureView.SurfaceTextureListener {
    private static String TAG = "AppLovinNativeMediaView";

    private static boolean sVIDEO_MUTED_BY_DEFAULT = false;

    private AppLovinSdk sdk;
    private AppLovinNativeAd ad;
    private Handler uiHandler;

    private boolean initialized = false;

    private ImageView fallbackImageView;

    private ViewGroup videoOverlay;
    private ViewGroup replayLayout;
    private ViewGroup learnMoreLayout;

    private ImageView muteButtonImageView;
    private AspectRatioTextureView textureView;

    private AppLovinNativeVideoState videoState;

    // Media player tracking
    private boolean videoCreated;
    private boolean autoplayRequested;
    private boolean mediaPlayerPrepared;
    private MediaPlayer mediaPlayer;
    private Surface surface;

    public AppLovinNativeMediaView(Context context) {
        super(context);
    }

    public AppLovinNativeMediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppLovinNativeMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AppLovinNativeMediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setAd(AppLovinNativeAd ad) {
        this.ad = ad;
    }

    public void setVideoState(AppLovinNativeVideoState cardState) {
        this.videoState = cardState;
    }

    public void setSdk(AppLovinSdk sdk) {
        this.sdk = sdk;
    }

    public void setUiHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    public void setUpView() {
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.applovin_native_media_view, this, true);

        bindViews();
        initializeView();
    }

    private void bindViews() {
        fallbackImageView = findViewById(R.id.applovin_native_image);
        videoOverlay = findViewById(R.id.applovin_native_video_overlay);
        replayLayout = findViewById( R.id.applovin_native_video_replay_layout);
        learnMoreLayout = findViewById( R.id.applovin_native_video_learn_more_layout);
    }

    private void initializeView() {
        if (!initialized) {
            initialized = true;

            setBackgroundColor(getResources().getColor(android.R.color.black));

            if (ad.isVideoPrecached() && /*AppLovinCarouselViewSettings.USE_VIDEO_SCREENSHOTS_AS_IMAGES*/true) {
                updateScreenshot();
            }

            AppLovinSdkUtils.safePopulateImageView(fallbackImageView, Uri.parse(ad.getImageUrl()),
                    AppLovinSdkUtils.dpToPx(getContext(), 350));

            // Create mute and replay views programmatically as they're added selectively at runtime.
            muteButtonImageView = new ImageView(getContext());

            final int muteSize = AppLovinSdkUtils.dpToPx(getContext(), 20);
            final int muteMargin = AppLovinSdkUtils.dpToPx(getContext(), 20);

            final LayoutParams muteParams = new LayoutParams(muteSize, muteSize);
            muteParams.gravity = Gravity.LEFT | Gravity.BOTTOM;

            muteButtonImageView.setLayoutParams(muteParams);

            muteButtonImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleMuteState();
                }
            });

            setAppropriateMuteImage( sVIDEO_MUTED_BY_DEFAULT);

            videoOverlay.setVisibility(videoState.isReplayOverlayVisible() ? VISIBLE : GONE);

            replayLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    replayVideo();
                }
            });

            learnMoreLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ad.launchClickTarget(getContext());
                }
            });
        }
    }

    void updateScreenshot() {
        final Bitmap screenshot = getVideoFrame(Math.max(200, videoState.getLastMediaPlayerPosition()));
        if (screenshot != null) {
            fallbackImageView.setImageBitmap(screenshot);
        }
    }

    private Bitmap getVideoFrame(final int position) {
        if (ad.getVideoUrl() == null) {
            return null;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bitmap;

        try {
            retriever.setDataSource(getContext(), Uri.parse(ad.getVideoUrl()));

            final Bitmap rawBitmap = retriever.getFrameAtTime(position);
            final Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap, textureView.getWidth(), textureView.getHeight(), false);

            rawBitmap.recycle();
            bitmap = scaledBitmap;
        } catch (Exception ex) {
            bitmap = null;
            if (AppUtil.sDebug) {
                Log.d(TAG, "Unable to grab video frame for: " + Uri.parse(ad.getVideoUrl()));
            }
        } finally {
            retriever.release();
        }

        return bitmap;
    }

    private void toggleMuteState() {
        setMuteState(videoState.getMuteState().equals(AppLovinNativeVideoState.MuteState.UNMUTED) ? AppLovinNativeVideoState.MuteState.MUTED : AppLovinNativeVideoState.MuteState.UNMUTED, true);
    }

    private void setMuteState(final AppLovinNativeVideoState.MuteState muteState, final boolean fade) {
        videoState.setMuteState(muteState);
        final boolean isBeingMuted = muteState.equals(AppLovinNativeVideoState.MuteState.MUTED);
        setAppropriateMuteImage(isBeingMuted);

        if (fade && /*AppLovinCarouselViewSettings.MUTE_FADES_AUDIO*/true) {
            final float numSteps = 10;
            final int stepDistance = 20;

            // Fade the audio in / out.
            for (int i = 0; i < numSteps; i++) {
                final float volume = isBeingMuted ? (numSteps - i) / numSteps : i / numSteps;
                final int delay = i * stepDistance;

                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            mediaPlayer.setVolume(volume, volume);
                        }
                    }
                }, delay);
            }

            // Finally, post a final adjustment to ensure it's at the target volume.
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        final float volume = isBeingMuted ? 0 : 1;
                        mediaPlayer.setVolume(volume, volume);
                    }
                }
            }, (long) (stepDistance * numSteps));
        } else {
            if (mediaPlayer != null) {
                final float volume = isBeingMuted ? 0 : 1;
                mediaPlayer.setVolume(volume, volume);
            }
        }
    }

    private void setAppropriateMuteImage(final boolean isMuted) {
        final int drawable = isMuted
                ? R.drawable.applovin_native_video_muted
                : R.drawable.applovin_native_video_unmuted;
        AppLovinSdkUtils.safePopulateImageView(getContext(), muteButtonImageView, drawable, /*AppLovinCarouselViewSettings.ICON_IMAGE_MAX_SCALE_SIZE*/50);
    }

    public void autoplayVideo() {
        if (AppLovinSdkUtils.isValidString(ad.getVideoUrl())) {
            if (!videoState.isReplayOverlayVisible() && ad.isVideoPrecached()) {
                if (mediaPlayer != null && mediaPlayerPrepared && !mediaPlayer.isPlaying()) {
                    playVideo(mediaPlayer);
                } else {
                    autoplayRequested = true;
                    createVideo();
                }
            }
        }
    }


    public void playVideo(final MediaPlayer mp) {
        setBackgroundColor(getResources().getColor(android.R.color.black));
        videoOverlay.setVisibility(GONE);
        videoState.setReplayOverlayVisible(false);

        final MediaPlayer mediaPlayer = (mp != null) ? mp : this.mediaPlayer;

        if(AppUtil.sDebug)
            Log.d(TAG, "Video play requested...");
        if (AppLovinSdkUtils.isValidString(ad.getVideoUrl())) {
            if (videoState.getMuteState().equals(AppLovinNativeVideoState.MuteState.UNSPECIFIED)) {
                setMuteState( sVIDEO_MUTED_BY_DEFAULT ? AppLovinNativeVideoState.MuteState.MUTED : AppLovinNativeVideoState.MuteState.UNMUTED, false);
            } else {
                setMuteState(videoState.getMuteState(), false);
            }

            mediaPlayer.start();

            if (!videoState.isVideoStarted()) {
                videoState.setVideoStarted(true);
                sdk.getPostbackService().dispatchPostbackAsync(ad.getVideoStartTrackingUrl(), null);
            }

            final AlphaAnimation muteFade = new AlphaAnimation(0f, 1f);
            muteFade.setDuration(500);
            muteFade.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    muteButtonImageView.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            muteButtonImageView.startAnimation(muteFade);

            // If the fallback view is visible, crossfade it with the video.
            if (fallbackImageView.getVisibility() == VISIBLE) {
                final AlphaAnimation imageFade = new AlphaAnimation(fallbackImageView.getAlpha(), 0f);
                imageFade.setDuration(750);
                imageFade.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fallbackImageView.setVisibility(INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                fallbackImageView.startAnimation(imageFade);

                final AlphaAnimation videoFade = new AlphaAnimation(0f, 1f);
                videoFade.setDuration(500);
                textureView.startAnimation(videoFade);
            }
        }
    }

    public void createVideo() {
        if (AppLovinSdkUtils.isValidString(ad.getVideoUrl())) {
            if (!videoCreated) {
                videoCreated = true;
                textureView = new AspectRatioTextureView(getContext());

                LayoutParams layoutParams =
                        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.gravity = Gravity.CENTER;
                textureView.setLayoutParams(layoutParams);
                textureView.setSurfaceTextureListener(this);

                final FrameLayout layoutRef = this;
                textureView.setOnMeasureCompletionListener(new AspectRatioTextureView.OnMeasureCompletionListener() {
                    @Override
                    public void onMeasureCompleted(int adjustedWidth, int adjustedHeight) {
                        final int xDelta = layoutRef.getWidth() - adjustedWidth; // Difference between layout and adjusted video width.
                        final int yDelta = layoutRef.getHeight() - adjustedHeight;

                        // Move the mute button to overlay the video.
                        final LayoutParams muteParams = (LayoutParams) muteButtonImageView.getLayoutParams();
                        final int padding = AppLovinSdkUtils.dpToPx(getContext(), 5);
                        muteParams.leftMargin = (xDelta / 2) + padding;
                        muteParams.bottomMargin = (yDelta / 2) + padding;
                    }
                });

                addView(textureView);
                bringChildToFront(textureView);

                // Bump the mute button to the front
                addView(muteButtonImageView);
                bringChildToFront(muteButtonImageView);

                invalidate();
                requestLayout();

                if (textureView.isAvailable()) {
                    onSurfaceTextureAvailable(textureView.getSurfaceTexture(), textureView.getWidth(), textureView.getHeight());
                }
            }
        }
    }


    private void prepareForReplay() {
        videoState.setLastMediaPlayerPosition(0);
        videoState.setReplayOverlayVisible(true);

        updateScreenshot();

        final AlphaAnimation replayFade = new AlphaAnimation(0f, 1f);
        replayFade.setDuration(500);
        videoOverlay.setVisibility(VISIBLE);
        videoOverlay.startAnimation(replayFade);

        textureView.setVisibility(INVISIBLE);
    }


    private void replayVideo() {
        videoOverlay.setVisibility(INVISIBLE);
        videoState.setReplayOverlayVisible(false);

        if (textureView != null) {
            textureView.setVisibility(VISIBLE);
            playVideo(null);
        } else {
            autoplayRequested = true;
            createVideo();
        }
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // Once Android has prepared the GL texture, start MediaPlayer setup
        if (mediaPlayer == null) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(getContext(), Uri.parse(ad.getVideoUrl()));
                this.surface = new Surface(surface);
                mediaPlayer.setSurface(this.surface);
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        try {
                            mediaPlayerPrepared = true;

                            final int videoWidth = mp.getVideoWidth();
                            final int videoHeight = mp.getVideoHeight();

                            textureView.setVideoSize(videoWidth, videoHeight);

                            final int lastPosition = videoState.getLastMediaPlayerPosition();
                            if (lastPosition > 0) {
                                mp.seekTo(lastPosition);
                                playVideo(mp);
                            } else if (autoplayRequested && !videoState.isReplayOverlayVisible()) {
                                playVideo(mp);
                            }
                        } catch (Exception ex) {
                            if(AppUtil.sDebug)
                                Log.e(TAG, "Unable to perform post-preparation setup", ex);
                        }
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        int percentViewed = calculatePercentViewed(mp);
                        if(AppUtil.sDebug)
                            Log.d(TAG, "OnCompletion invoked at " + percentViewed);

                        // Some Android devices report 0 on completion. So if we've both started and ended organically, this is a success case.
                        if (percentViewed == 0) {
                            percentViewed = 100;
                        }

                        // If we've reached the end of the video, toggle 'replay' mode.
                        if (percentViewed >= 98) {
                            setBackgroundColor(getResources().getColor(android.R.color.black));
                            videoState.setVideoCompleted(true);
                            prepareForReplay();
                        }

                        // In any case, notify the video end URL.
                        notifyVideoEndUrl(percentViewed);

                        final AlphaAnimation muteFade = new AlphaAnimation(1f, 0f);
                        muteFade.setDuration(500);
                        muteFade.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                muteButtonImageView.setVisibility(INVISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        muteButtonImageView.startAnimation(muteFade);
                    }
                });

                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        if(AppUtil.sDebug)
                            Log.w(TAG, "MediaPlayer error: (" + what + ", " + extra + ")");
                        return true;
                    }
                });

            } catch (Exception ex) {
                if(AppUtil.sDebug)
                    Log.e(TAG, "Unable to build media player.", ex);
            }
        }
    }


    private void notifyVideoEndUrl(int percentViewed) {
        if (videoState.isVideoStarted()) {
            sdk.getPostbackService().dispatchPostbackAsync(ad.getVideoEndTrackingUrl(percentViewed, videoState.isFirstPlay()), null);
            videoState.setFirstPlay(false);
        }
    }


    private int calculatePercentViewed(final MediaPlayer mp){
        final float videoDuration = mp.getDuration();
        final float currentPosition = mp.getCurrentPosition();
        // NOTE: Media player bug: calling getCurrentPosition after the video finished playing gives slightly larger value than the total duration of the video.
        if ( currentPosition >= videoDuration )
        {
            // Video fully watched, return 100%.
            return 100;
        }

        final double percentViewed = ( currentPosition / videoDuration ) * 100f;
        return (int) Math.ceil( percentViewed );
    }

    public static class AppLovinNativeVideoState {
        private boolean videoStarted;
        private boolean videoCompleted;
        private boolean impressionTracked;
        private boolean videoStartTracked;
        private boolean firstPlay;
        private boolean previouslyActivated;
        private boolean currentlyActive;
        private int lastMediaPlayerPosition;
        private boolean replayOverlayVisible;
        private MuteState muteState;

        public AppLovinNativeVideoState() {
            muteState = MuteState.UNSPECIFIED;
            firstPlay = true;
        }

        public boolean isVideoCompleted() {
            return videoCompleted;
        }

        public void setVideoCompleted(boolean videoCompleted) {
            this.videoCompleted = videoCompleted;
        }

        public int getLastMediaPlayerPosition() {
            return lastMediaPlayerPosition;
        }

        public void setLastMediaPlayerPosition(int lastMediaPlayerPosition) {
            this.lastMediaPlayerPosition = lastMediaPlayerPosition;
        }

        public boolean isVideoStarted() {
            return videoStarted;
        }

        public void setVideoStarted(boolean videoStarted) {
            this.videoStarted = videoStarted;
        }

        public boolean isPreviouslyActivated() {
            return previouslyActivated;
        }

        public void setPreviouslyActivated(boolean previouslyActivated) {
            this.previouslyActivated = previouslyActivated;
        }

        public boolean isImpressionTracked() {
            return impressionTracked;
        }

        public void setImpressionTracked(boolean impressionTracked) {
            this.impressionTracked = impressionTracked;
        }

        public boolean isVideoStartTracked() {
            return videoStartTracked;
        }

        public void setVideoStartTracked(boolean videoStartTracked) {
            this.videoStartTracked = videoStartTracked;
        }

        public boolean isCurrentlyActive() {
            return currentlyActive;
        }

        public void setCurrentlyActive(boolean currentlyActive) {
            this.currentlyActive = currentlyActive;
        }

        public boolean isReplayOverlayVisible() {
            return replayOverlayVisible;
        }

        public void setReplayOverlayVisible(boolean replayOverlayVisible) {
            this.replayOverlayVisible = replayOverlayVisible;
        }

        public MuteState getMuteState() {
            return muteState;
        }

        public void setMuteState(MuteState muteState) {
            this.muteState = muteState;
        }

        public boolean isFirstPlay() {
            return firstPlay;
        }

        public void setFirstPlay(boolean paused) {
            this.firstPlay = paused;
        }

        /**
         * Created by mszaro on 4/24/15.
         */
        public static enum MuteState {
            UNSPECIFIED,
            UNMUTED,
            MUTED
        }
    }


    /**
     * Provides a TextureView that maintains the aspect ratio of a video contained within.
     */
    public static class AspectRatioTextureView
            extends TextureView {
        private int mVideoWidth;
        private int mVideoHeight;
        private OnMeasureCompletionListener onMeasureCompletionListener;

        public AspectRatioTextureView(Context context) {
            super(context);

            mVideoWidth = 0;
            mVideoHeight = 0;
        }

        public AspectRatioTextureView(Context context, AttributeSet attrs) {
            super(context, attrs);

            mVideoWidth = 0;
            mVideoHeight = 0;
        }

        public AspectRatioTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            mVideoWidth = 0;
            mVideoHeight = 0;
        }

        public OnMeasureCompletionListener getOnMeasureCompletionListener() {
            return onMeasureCompletionListener;
        }

        public void setOnMeasureCompletionListener(OnMeasureCompletionListener onMeasureCompletionListener) {
            this.onMeasureCompletionListener = onMeasureCompletionListener;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (mVideoWidth <= 0 || mVideoHeight <= 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }

            float heightRatio = (float) mVideoHeight / (float) getHeight();
            float widthRatio = (float) mVideoWidth / (float) getWidth();

            int scaledHeight;
            int scaledWidth;

            if (heightRatio > widthRatio) {
                scaledHeight = (int) Math.ceil((float) mVideoHeight / heightRatio);
                scaledWidth = (int) Math.ceil((float) mVideoWidth / heightRatio);
            } else {
                scaledHeight = (int) Math.ceil((float) mVideoHeight / widthRatio);
                scaledWidth = (int) Math.ceil((float) mVideoWidth / widthRatio);
            }

            setMeasuredDimension(scaledWidth, scaledHeight);

            if (onMeasureCompletionListener != null) {
                onMeasureCompletionListener.onMeasureCompleted(scaledWidth, scaledHeight);
            }
        }

        public void setVideoSize(int videoWidth, int videoHeight) {
            mVideoWidth = videoWidth;
            mVideoHeight = videoHeight;

            try {
                requestLayout();
                invalidate();
            } catch (Exception ignore) {
            }
        }

        public interface OnMeasureCompletionListener {
            void onMeasureCompleted(final int adjustedWidth, final int adjustedHeight);
        }
    }
}
