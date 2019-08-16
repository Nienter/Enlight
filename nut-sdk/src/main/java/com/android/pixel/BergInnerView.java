package com.android.pixel;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;

import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.Gravity.TOP;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
import static android.view.WindowManager.LayoutParams.TYPE_TOAST;


public class BergInnerView extends LinearLayout {
    private WindowManager mWndManager ;//g
    private DisplayMetrics mDisplayMetrics;//

    private ImageView mCloseView;

    private int mUnlockStep;
    private boolean mIsCenter;

    private boolean mDestroyed = false;


    private Runnable mTskShowCloseBtn = new Runnable() {
        @Override
        public void run() {
            mCloseView.setVisibility(VISIBLE);
        }
    };

    public BergInnerView(Context context, int step, boolean isCenter){
        super(context);
        mWndManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        mUnlockStep = step;
        mIsCenter = isCenter;

        mDisplayMetrics = getResources().getDisplayMetrics();
        setOrientation(VERTICAL);
        setBackgroundColor(Color.parseColor("#ffffff"));

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = TYPE_TOAST;
        layoutParams.format= PixelFormat.RGBA_8888;
        layoutParams.flags = FLAG_WATCH_OUTSIDE_TOUCH | FLAG_NOT_FOCUSABLE;
        layoutParams.width = mDisplayMetrics.widthPixels;
        layoutParams.height= FunctionUtil.dp2px(250+25);
        layoutParams.gravity = !mIsCenter ? CENTER_HORIZONTAL|TOP : CENTER;
        mWndManager.addView(this, layoutParams);
    }

    private void createHeader(AdNetwork network){
        //create header
        FrameLayout headerLayout = new FrameLayout(getContext());
        addView(headerLayout, new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        LinearLayout headerLayoutLeft = new LinearLayout(getContext());
        headerLayoutLeft.setOrientation(HORIZONTAL);
        headerLayoutLeft.setGravity(CENTER_VERTICAL);
        headerLayout.addView(headerLayoutLeft, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

//        headerLayoutLeft.addView(new AdChoicesView(getContext(), mNativeAd, true));
//
//        CharSequence sponsored = mNativeAd.getSponsoredTranslation();
//        TextView textView = new TextView(getContext());
//        textView.setText(sponsored);
//        textView.setTextColor(Color.parseColor("#333333"));
//        headerLayoutLeft.addView(textView);

        createCloseBtn(network);
        headerLayout.addView(mCloseView, generateLayoutParamsForCloseView(network));

    }


    public ViewGroup getAdContainer(AdNetwork network){
        createHeader(network);

        LinearLayout adContainer = new LinearLayout(getContext());
        adContainer.setOrientation(VERTICAL);
        adContainer.setGravity(CENTER);
        addView(adContainer, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        return adContainer;
    }


    public void showCloseBtn(){
        if(mCloseView!=null) {
            mCloseView.setVisibility(VISIBLE);
        }
    }


    public boolean isDestroyed(){
        return mDestroyed;
    }
    public final void destroy(){
        if(mDestroyed)
            return;

        mDestroyed = true;
        removeCallbacks(mTskShowCloseBtn);

        mWndManager.removeViewImmediate(this);

        getContext().sendBroadcast(new Intent(mUnlockStep==1 ? BergScheduler.sUnlockAdClose : BergScheduler.sUnlockDelayAdClose));
    }



    private void createCloseBtn(AdNetwork network){
        int size3dp  = FunctionUtil.dp2px(3);
        int size50dp = FunctionUtil.dp2px(50);

        mCloseView = new ImageView(getContext());
        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                destroy();
            }
        });

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#99000000"));

        gradientDrawable.setCornerRadius(size50dp);
        mCloseView.setBackground(gradientDrawable);
        mCloseView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

        mCloseView.setPadding(size3dp, size3dp, size3dp, size3dp);
        mCloseView.setVisibility(INVISIBLE);

        //kDayNCount>0 ? 0 : btnDelay
        long delayToShowClose;
        if(BergStats.getInst().getTotalInnerClickTimesNetwork(network) > 0){
            delayToShowClose = 0;
        } else {//the value got from lanzong is 60, however, the postDelayed() accepts the second param in millisecond rather than second.
            delayToShowClose = BergConfig.getInst().getInnerCloseBtnDelay();
        }
        postDelayed(mTskShowCloseBtn, delayToShowClose*DateUtils.SECOND_IN_MILLIS);
    }


    FrameLayout.LayoutParams generateLayoutParamsForCloseView(AdNetwork network){
        int i=25;

        if(BergStats.getInst().getTotalInnerClickTimesNetwork(network)<=0){
            int btnStyle = 2;
            switch (btnStyle) {
                case 1:
                    i = 15;
                    break;
                case 2:
                    i = 20;
                    break;
                case 3:
                    i = 25;
                    break;
            }
        }

        int length =  FunctionUtil.dp2px(i);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(length, length);
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        return layoutParams;
    }
}
