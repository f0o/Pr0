package com.pr0gramm.app.ui;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pr0gramm.app.BuildConfig;
import com.pr0gramm.app.services.UserService;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Utility methods for ads.
 */
public class Ad {
    private Ad() {
    }

    /**
     * Loads an ad into this view.
     */
    public static void load(AdView view) {
        if (view != null) {
            view.loadAd(new AdRequest.Builder()
                    .setIsDesignedForFamilies(false)
                    .addTestDevice("5436541A8134C1A32DACFD10442A32A1")
                    .build());
        }
    }

    public static Observable<Boolean> shouldShowAds(UserService userService) {
        return userService.loginState()
                .observeOn(AndroidSchedulers.mainThread())
                .map(info -> !info.premium() || BuildConfig.DEBUG)
                .distinctUntilChanged();
    }
}
