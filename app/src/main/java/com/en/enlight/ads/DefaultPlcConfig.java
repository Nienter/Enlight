package com.en.enlight.ads;


import com.android.pixel.AdNetwork;
import com.android.pixel.AdPlcConfig;
import com.android.pixel.AdSize;
import com.android.pixel.AdType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultPlcConfig {

    public static String[] sAdmobBanner      ={"ca-app-pub-6890037700139378/6635814512", "ca-app-pub-6890037700139378/1383487838"};
    public static String[] sAdmobInterstitial={"ca-app-pub-6890037700139378/5131161152", "ca-app-pub-6890037700139378/8878834472", "ca-app-pub-6890037700139378/9808772769"};
    public static String[] sAdmobNative      ={};

    public static String[] sFacebookBannerSmall  = {};
    public static String[] sFacebookBannerMedium = {};
    public static String[] sFacebookInterstitial = {"986558384878518_989768801224143", "986558384878518_989769131224110", "986558384878518_989769201224103", "986558384878518_989769297890760"};
    public static String[] sFacebookNativeSmall  = {"986558384878518_989769557890734", "986558384878518_989770314557325"};
    public static String[] sFacebookNativeMedium = {"986558384878518_989767584557598", "986558384878518_989757921225231"};

    public static String[] sMopubBannerSmall  ={};
    public static String[] sMopubBannerMedium ={};
    public static String[] sMopubInterstitial ={"891b834a2fe54658bc6fb8dac953585c"};
    public static String[] sMopubNative       ={"dd1ef26e4bbe408e957067b4f2774d58"};
    public static String[] sMopubRewardedVideo={"69881dc176be489987d346ebc77cf412"};


    public static AdPlcConfig sBannerSmall;
    static {
        sBannerSmall = new AdPlcConfig.Builder()
                .types(AdType.BANNER)
                .size(AdSize.SMALL)
                .build();
    }

    public static AdPlcConfig sBannerMedium;
    static {
        sBannerMedium = new AdPlcConfig.Builder()
                .types(AdType.BANNER)
                .size(AdSize.MEDIUM)
                .build();
    }

    public static AdPlcConfig sNativeSmall;
    static {
        sNativeSmall = new AdPlcConfig.Builder()
                .types(AdType.NATIVE)
                .size(AdSize.SMALL)
                .build();
    }

    public static AdPlcConfig sNativeMedium;
    static {
        sNativeMedium = new AdPlcConfig.Builder()
                .types(AdType.NATIVE)
                .size(AdSize.MEDIUM)
                .build();
    }


    public static AdPlcConfig sBannerNativeSmall;
    static {
        sBannerNativeSmall = new AdPlcConfig.Builder()
                .types(AdType.BANNER, AdType.NATIVE)
                .size(AdSize.SMALL)
                .build();
    }

    public static AdPlcConfig sBannerNativeSmallWithoutFan;
    static {
        sBannerNativeSmallWithoutFan = new AdPlcConfig.Builder()
                .types(AdType.BANNER, AdType.NATIVE)
                .size(AdSize.SMALL)
                .network(AdNetwork.ADMOB, AdNetwork.MOPUB)
                .build();
    }



    public static AdPlcConfig sBannerNativeMedium;
    static {
        sBannerNativeMedium = new AdPlcConfig.Builder()
                .types(AdType.BANNER, AdType.NATIVE)
                .size(AdSize.MEDIUM)
                .build();
    }

    public static AdPlcConfig sBannerNativeMediumWithoutFan;
    static {
        sBannerNativeMediumWithoutFan = new AdPlcConfig.Builder()
                .types(AdType.BANNER, AdType.NATIVE)
                .size(AdSize.MEDIUM)
                .network(AdNetwork.ADMOB, AdNetwork.MOPUB)
                .build();
    }

    public static AdPlcConfig sInterstitial;
    static {
        sInterstitial = new AdPlcConfig.Builder()
                .types(AdType.INTERSTITIAL)
                .size(AdSize.FULLSCREEN)
                .build();
    }

    public static AdPlcConfig sInterstitialWithoutFan;
    static {
        sInterstitialWithoutFan = new AdPlcConfig.Builder()
                .types(AdType.INTERSTITIAL)
                .size(AdSize.FULLSCREEN)
                .network(AdNetwork.ADMOB, AdNetwork.MOPUB)
                .build();
    }




    public static void checkADId() {
        List<String> sAdmobList = new ArrayList<>();
        List<String> sFacebookList = new ArrayList<>();
        List<String> sMopubList = new ArrayList<>();

        sAdmobList.clear();
        sFacebookList.clear();
        sMopubList.clear();

        String[] sAdmobString = sAdmobBanner[0].split("/");
        String sAdmobID = sAdmobString[0];
        sAdmobList.addAll(Arrays.asList(sAdmobBanner));
        sAdmobList.addAll(Arrays.asList(sAdmobInterstitial));
        sAdmobList.addAll(Arrays.asList(sAdmobNative));

        for (int i = 0; i < sAdmobList.size(); i++) {
            if(!sAdmobList.get(i).contains(sAdmobID)) {
                throw new RuntimeException("ADmobID:-->"+sAdmobList.get(i)+"<--Error");
            }
            if(sAdmobList.get(i).length()!=sAdmobList.get(0).length()) {
                throw new RuntimeException("ADmobID:-->"+sAdmobList.get(i)+"<--Size Error");
            }
            for (int j = i + 1; j < sAdmobList.size(); j++) {
                if (sAdmobList.get(i).equals(sAdmobList.get(j))) {
                    throw new RuntimeException("ADmobID:-->"+sAdmobList.get(i)+"<--Duplicate");
                }
            }
        }

        sFacebookList.addAll(Arrays.asList(sFacebookBannerSmall));
        sFacebookList.addAll(Arrays.asList(sFacebookBannerMedium));
        sFacebookList.addAll(Arrays.asList(sFacebookInterstitial));
        sFacebookList.addAll(Arrays.asList(sFacebookNativeSmall));
        sFacebookList.addAll(Arrays.asList(sFacebookNativeMedium));
        String[] sFacebookString = sFacebookList.get(0).split("_");
        String sFacebookID = sFacebookString[0];
        for (int i = 0; i < sFacebookList.size(); i++) {
            if(!sFacebookList.get(i).contains(sFacebookID)) {
                throw new RuntimeException("FacebookID:-->"+sFacebookList.get(i)+"<--Error");
            }
            if(sFacebookList.get(i).length()!=sFacebookList.get(0).length()) {
                throw new RuntimeException("FacebookID:-->"+sFacebookList.get(i)+"<--Size Error");
            }
            for (int j = i + 1; j < sFacebookList.size(); j++) {
                if (sFacebookList.get(i).equals(sFacebookList.get(j))) {
                    throw new RuntimeException("FacebookID:-->"+sFacebookList.get(i)+"<--Duplicate");
                }
            }
        }

        sMopubList.addAll(Arrays.asList(sMopubBannerSmall));
        sMopubList.addAll(Arrays.asList(sMopubBannerMedium));
        sMopubList.addAll(Arrays.asList(sMopubInterstitial));
        sMopubList.addAll(Arrays.asList(sMopubNative));
        for (int i = 0; i < sMopubList.size(); i++) {
            if(sMopubList.get(i).length()!=sMopubList.get(0).length()) {
                throw new RuntimeException("MopubID:-->"+sMopubList.get(i)+"<--Size Error");
            }
            for (int j = i + 1; j < sMopubList.size(); j++) {
                if (sMopubList.get(i).equals(sMopubList.get(j))) {
                    throw new RuntimeException("MopubID:-->"+sMopubList.get(i)+"<--Duplicate");
                }
            }
        }
    }

    public final static String sSplashInnerPlc="splashInner";
    public final static String sSplashIntPlc  ="splashInt";

    public final static String sMainInnerPlc="mainInner";
    public final static String sMainIntPlc  ="mainInt";


    public final static String sScanCodeInnerPlc="scancodeInner";
    public final static String sScanCodeIntPlc="scancodeInt";

    public final static String sScanRetInnerPlc="scanretInner";
    public final static String sScanRetIntPlc="scanretInt";


    public final static String sGencodeInnerPlc="gencodeInner";
    public final static String sGencodeIntPlc="gencodeInt";

    public final static String sPicInnerPlc="picInner";
    public final static String sPicIntPlc="picInt";

    public final static String sExitInnerPlc="exitInner";
    public final static String sExitIntPlc  ="exitInt";
}
