package com.ware.soundloadie

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.RequestOptions
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.github.kittinunf.fuel.Fuel
import com.pedro.library.AutoPermissions
import com.pedro.library.AutoPermissionsListener
import com.ware.soundloadie.flappy.Birds
import com.ware.soundloadie.flappy.GlideApp
import kotlinx.android.synthetic.main.calcium.*
import kotlinx.android.synthetic.main.save_plan.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class Calcium : AppCompatActivity(), AutoPermissionsListener, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    var clientID = ""
    var birds:Birds? = null
    var mainURL = ""
    var job:Job? = null
    var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calcium)
        birds = Birds()
        val originURl = fetchIntents()

        save_close.setOnClickListener {

            if (job != null){
                job!!.cancel()
            }
            finishAndRemoveTask()
        }




        val extractume = birds!!.scrapeLinks(originURl)
        mainURL = extractume[0]

        if (Permitted()){

            loadAds()
            songGettr(mainURL)






        }else{

            AutoPermissions.loadActivityPermissions(this, 1)

        }


    }


   fun fetchIntents():String{
       clientID = getString(R.string.client_id)
       val linktent = intent
       val permalink = linktent.getStringExtra(Intent.EXTRA_TEXT)
       return permalink
    }





    override fun onGranted(requestCode: Int, permissions: Array<String>) {

        songGettr(mainURL)
        loadAds()

    }

    override fun onDenied(requestCode: Int, permissions: Array<String>) {

    }




    private fun Permitted(): Boolean {
        val result = ContextCompat.checkSelfPermission(this@Calcium, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AutoPermissions.parsePermissions(this@Calcium, requestCode, permissions, this)
    }

    fun songGettr(queryID:String){
        var downloadURl = ""
        var title = ""
        var fileSize = ""
        var avatarURL = ""
        job = this.launch {

            withContext(Dispatchers.Default){
                val downloadPot = birds!!.fetchSong(clientID,queryID)
                 downloadURl = downloadPot[0]
                 title = downloadPot[1]
                 fileSize = downloadPot[2]
                 avatarURL = downloadPot[3]

                val fileDirect = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/soundloadie/")
                if (!fileDirect.exists()) {
                    val soundDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/soundloadie/")
                    soundDirectory.mkdirs()
                }
            }

            saveMusic(downloadURl,title,avatarURL,fileSize)

        }

    }





    fun saveMusic(downloadString:String,nameString:String,downloadAvatar:String,downloadSize:String){

        lottieAnimationView.visibility = View.GONE
        save_home.visibility = View.VISIBLE
        save_title.text = nameString
        save_status.text = "Downloading..."
        val imageUrl = downloadAvatar.replace("-large.jpg","-t500x500.jpg")
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.ic_place)
        GlideApp.with(this@Calcium).setDefaultRequestOptions(requestOptions).load(downloadAvatar).into(save_preview)
        save_file_size.text = birds!!.readableFileSize(downloadSize.toLong())
        save_preview_btn.visibility = View.GONE
        save_progress.max = 100
        save_progress.isIndeterminate = false
        val name = nameString.replace(" ", "_")
        val fileName = "$name.mp3"
        val savedPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/soundloadie/$fileName"
        val scanURI= arrayOf(savedPath)
        Fuel.download("$downloadString").fileDestination { response, url -> val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()); File (dir, "soundloadie/$fileName")
        }.progress {
                readBytes, totalBytes -> val progress = readBytes.toFloat() / totalBytes.toFloat() * 100

                runOnUiThread {

                    val konvert  = progress.toInt()
                    save_progress.progress = konvert
                    save_status.text = "$konvert%"
                }


        }.response {
                req, res, result ->
            val (data, error) = result
            if (error != null) {

            } else {

                MediaScannerConnection.scanFile(this@Calcium, scanURI, null
                ) { paths, uris ->

                    runOnUiThread {

                        save_status.visibility = View.GONE
                        save_preview_btn.visibility =View.VISIBLE

                        save_preview_btn.setOnClickListener {

                            val intent = Intent()
                            intent.action = android.content.Intent.ACTION_VIEW
                            intent.setDataAndType(uris, "audio/*")
                            startActivity(intent)

                        }
                    }


                }
            }



        }


    }


    fun loadAds(){

        mInterstitialAd = com.facebook.ads.InterstitialAd(this@Calcium, getString(R.string.intersistal))
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
