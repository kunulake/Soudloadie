package com.ware.soundloadie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private  lateinit var navController: NavController
    var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = Navigation.findNavController(this@MainActivity,R.id.nav_host_fragment)
        bottom_nav.setupWithNavController(navController)

        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.navigation_graph)
        navController.graph = graph

        mInterstitialAd = com.facebook.ads.InterstitialAd(this@MainActivity, getString(R.string.intersistal))



        mInterstitialAd!!.setAdListener(object : InterstitialAdListener {
            override fun onLoggingImpression(p0: Ad?) {


            }

            override fun onAdLoaded(p0: Ad?) {

                mInterstitialAd!!.show();

            }

            override fun onError(p0: Ad?, p1: AdError?) {


            }

            override fun onInterstitialDismissed(p0: Ad?) {


            }

            override fun onAdClicked(p0: Ad?) {


            }

            override fun onInterstitialDisplayed(p0: Ad?) {


            }


        })



        // Load ads into Interstitial Ads
        mInterstitialAd!!.loadAd()

    }
}
