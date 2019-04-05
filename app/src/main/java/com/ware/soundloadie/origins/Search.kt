package com.ware.soundloadie.origins


import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.lapism.searchview.Search
import com.ware.soundloadie.Calcium

import com.ware.soundloadie.R
import com.ware.soundloadie.flappy.Birds
import com.ware.soundloadie.flappy.GlideApp
import com.ware.soundloadie.flappy.Songs
import com.ware.soundloadie.flappy.SoundAdapter
import kotlinx.android.synthetic.main.origin_search.*
import kotlinx.android.synthetic.main.response_view.view.*
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.coroutines.*
import org.json.JSONArray
import java.util.*
import kotlin.coroutines.CoroutineContext


class Search : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    var jobberman : Job? = null
    var queryString = ""
    var clientID = ""
    var builder: Dialog? = null
    var songs :ArrayList<Songs> ? = null
    enum class SEEKBAR_STATE{

        STICK, UNSTICK
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.origin_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clientID = getString(R.string.client_id)
        loadBar()
    }


    fun loadBar(){

        val hintMessage = getString(R.string.hint)
        searchBar.setLogoIcon(R.drawable.ic_soundcloud)
        searchBar.setHint(hintMessage)

        searchBar.setOnQueryTextListener(object: Search.OnQueryTextListener{
            override fun onQueryTextChange(newText: CharSequence?) {


            }

            override fun onQueryTextSubmit(query: CharSequence?): Boolean {

                if (query.toString()!! != ""){
                    search_recycler.adapter = null
                    queryString = query.toString()
                    queryCloud(queryString )
                    searchBar.clearFocus()
                }else{
                    searchBar.clearFocus()
                }

                return true
            }


        })

    }


    fun queryCloud(query:String){

        val titleListx  =  ArrayList<String>()
        val artistListx  =  ArrayList<String>()
        val artListx =  ArrayList<String>()
        val urlListx  =  ArrayList<String>()
        songs =  ArrayList<Songs>()
        var errorMessage = ""
        search_refresh.isRefreshing = true
        search_recycler.adapter = null
        val birds = Birds()


        jobberman = this.launch {

            withContext(Dispatchers.Default){
                val finalString = query.replace(" ","%20")
                val queryURl = "https://api.soundcloud.com/tracks.json?client_id=$clientID&q=$finalString&limit=200"
                val responsed =  birds.jsonMan(queryURl)
                val jsonarray = JSONArray(responsed)

                for (i in 0 until jsonarray.length()) {

                    var jsonobj = jsonarray.getJSONObject(i)


                    val titles = jsonobj.getString("title")
                    val artists = jsonobj.getJSONObject("user").getString("username")
                    val artCover = jsonobj.getString("artwork_url")
                    val soundLink = jsonobj.getString("permalink_url")
                    val duration = jsonobj.getInt("duration")
                    val soundID = jsonobj.getInt("id")
                    titleListx.add(titles)
                    artistListx.add(artists)
                    artListx.add(artCover)
                    urlListx.add(soundLink)
                    songs!!.add(Songs(titles,artists,artCover,soundID,duration,soundLink))


                }


            }

            // primetime

            val adapter = SoundAdapter(itemLayoutRes = R.layout.response_view,
                itemCount = titleListx.size,
                binder = {

                    val imageURL = artListx[it.adapterPosition]
                    val soundLinq = urlListx[it.adapterPosition]
                    val position = it.adapterPosition

                    val requestOptions = RequestOptions()
                    requestOptions.placeholder(R.drawable.ic_place)

                    it.itemView.song_title.text = titleListx[it.adapterPosition]
                    it.itemView.artist.text = artistListx[it.adapterPosition]
                    GlideApp.with(it.itemView.context)
                        .load(imageURL)
                        .apply(requestOptions)
                        .into(it.itemView.album_cover)

                    it.itemView.setOnClickListener {

                        soundPop(position)


                    }


                })


            search_recycler.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            search_recycler.adapter = adapter
            adapter!!.notifyDataSetChanged()
            search_refresh.isRefreshing = false



        }


    }


    override fun onDestroy() {
        super.onDestroy()
        if (jobberman != null){
            jobberman!!.cancel()
        }
    }

    override fun onPause() {
        super.onPause()
        if (jobberman != null){
            jobberman!!.cancel()
        }
    }



    //show save dialog
    fun soundPop(postion:Int){

         val timer =  Timer()

        val albumart = songs!![postion].albumCover
        val songname = songs!![postion].songName
        val songmaker = songs!![postion].artisteTitle
        val songID = songs!![postion].songID
        val duration = songs!![postion].duration
        val permanent = songs!![postion].soundLink
        val albumcover = albumart.replace("-large.jpg","-t500x500.jpg")
        var isReady = false
        val view = layoutInflater.inflate(R.layout.soundpop, null)
        val popProfile = view.findViewById<ImageView>(R.id.pop_profile)
        val popsong = view.findViewById<TextView>(R.id.pop_song_name)
        val popartiste = view.findViewById<TextView>(R.id.pop_artiste_name)
        val cancelBtn = view.findViewById<ImageView>(R.id.cancel_btn)
        val popsaveBtn = view.findViewById<ImageView>(R.id.pop_save_btn)
        val play_btn = view.findViewById<ImageView>(R.id.play_btn)
        val play_progress  = view.findViewById<ProgressBar>(R.id.play_progress)
        val spinner  =  view.findViewById<ProgressBar>(R.id.spinner)

        popsaveBtn.setOnClickListener {

            val inty = Intent(activity!!,Calcium::class.java)
            inty.putExtra(Intent.EXTRA_TEXT,permanent)
            startActivity(inty)
        }

        val handler = Handler()





        val media = MediaPlayer()
        media.setDataSource("https://api.soundcloud.com/tracks/$songID/stream?client_id=$clientID")
        val runnableCode = object: Runnable {
            override fun run() {
                if (media.isPlaying){
                    play_progress.progress = media.currentPosition
                }
                handler.postDelayed(this, 1000)
            }
        }

        runnableCode.run()


        media.setOnPreparedListener(object :MediaPlayer.OnPreparedListener{
            override fun onPrepared(mp: MediaPlayer?) {
                activity!!.runOnUiThread {
                    isReady = true
                    mp!!.start()


                    play_btn.visibility = View.VISIBLE
                    play_btn.setImageResource(R.drawable.ic_pause)
                    spinner.visibility = View.GONE
                    play_progress.isIndeterminate = false
                    play_progress.max = mp!!.duration




                }

            }


        })

        media.setOnCompletionListener(object :MediaPlayer.OnCompletionListener{
            override fun onCompletion(mp: MediaPlayer?) {

                mp!!.stop()
                timer.cancel()
                play_btn.setImageResource(R.drawable.ic_play)

            }


        })

        play_btn.setOnClickListener {

            if (isReady){

                if (media.isPlaying){

                    play_btn.setImageResource(R.drawable.ic_play)
                    media.pause()

                }else{

                    play_btn.setImageResource(R.drawable.ic_pause)
                    media.start()


                }

            }else{


                this.launch {
                    play_progress.isIndeterminate = true
                    play_progress.visibility = View.VISIBLE
                    spinner.visibility = View.VISIBLE
                    play_btn.visibility=  View.GONE


                    withContext(Dispatchers.Default){

                        media.prepare()

                    }

                }



            }


        }




        popsong.text = songname
        popartiste.text = songmaker

        cancelBtn.setOnClickListener {
            handler.removeCallbacks(runnableCode)
            if (media != null){

                if (media.isPlaying){
                    media.stop()
                }
                media.release()
            }
            builder!!.dismiss()
        }
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.ic_place)
        GlideApp.with(activity!!).setDefaultRequestOptions(requestOptions).load(albumcover).into(popProfile)
        builder =  Dialog(activity!!);
        builder!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder!!.setCancelable(false)
        builder!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        builder!!.setContentView(view)
        builder!!.show()
    }




    fun milliSecondsToTimer(milliseconds:Long):String{

         var finalTimerString = ""
        var secondsString = ""
        val hours = (milliseconds / (1000 * 60 * 60))
        val minutes =  (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds =  ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000)
        if (hours == 0L) {
            finalTimerString = "$hours:"
        }
        if (seconds == 10L) {
            secondsString = "0  $seconds"
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = "$finalTimerString $minutes : $secondsString"

        return finalTimerString;

    }

}
