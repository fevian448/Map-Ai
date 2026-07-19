package com.example.mapai.util

import android.content.Context
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdLoader

object AdMobManager {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var nativeAd: NativeAd? = null

    fun initialize(context: Context) {
        MobileAds.initialize(context) {}
    }

    fun loadBanner(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    fun loadInterstitial(context: Context, adUnitId: String, onLoaded: (InterstitialAd?) -> Unit) {
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    onLoaded(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    onLoaded(null)
                }
            }
        )
    }

    fun showInterstitial(
        context: Context,
        onAdDismissed: () -> Unit
    ) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            val activity = context as? androidx.activity.ComponentActivity
            activity?.let { ad.show(it) } ?: onAdDismissed()
        } ?: run {
            onAdDismissed()
        }
    }

    fun loadRewarded(context: Context, adUnitId: String, onLoaded: (RewardedAd?) -> Unit) {
        RewardedAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    onLoaded(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    onLoaded(null)
                }
            }
        )
    }

    fun loadNativeAd(
        context: Context,
        adUnitId: String,
        onLoaded: (NativeAd?) -> Unit
    ) {
        val loader = NativeAdLoader(context, adUnitId, NativeAdOptions.Builder().build())
        loader.loadAd(AdRequest.Builder().build(), object : NativeAdLoadCallback() {
            override fun onAdLoaded(ad: NativeAd) {
                nativeAd = ad
                onLoaded(ad)
            }

            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                nativeAd = null
                onLoaded(null)
            }
        })
    }

    fun destroyNativeAd() {
        nativeAd?.destroy()
        nativeAd = null
    }
}
