package com.ware.soundloadie.evolution


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
import com.ware.soundloadie.Calcium

import com.ware.soundloadie.R
import com.ware.soundloadie.flappy.Birds
import com.ware.soundloadie.flappy.GlideApp
import com.ware.soundloadie.flappy.Songs
import com.ware.soundloadie.flappy.SoundAdapter
import kotlinx.android.synthetic.main.evo_genre_view.*
import kotlinx.android.synthetic.main.response_view.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*
import kotlin.coroutines.CoroutineContext

class GenreView : Fragment() , CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    var jobberman :Job? = null
    var builder: Dialog? = null
    var songs :ArrayList<Songs> ? = null
    var clientID = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.evo_genre_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clientID = getString(R.string.client_id)
        val genre = arguments!!.getString("genre","all-music")
         genre_toolbar.title = genre
        genre_toolbar.setNavigationIcon(R.drawable.ic_arrow)
        genre_toolbar.setNavigationOnClickListener { activity!!.onBackPressed() }

        fetchGenres(genre)


    }



    fun fetchGenres(genreID:String){

        jobberman = this.launch {
            var titleList  =  ArrayList<String>()
            var artistList  =  ArrayList<String>()
            var artList  =  ArrayList<String>()
            var urlList  =  ArrayList<String>()
            songs =  ArrayList<Songs>()
            var errorMessage = ""
            genre_refresher.isRefreshing = true
            genre_recyclerview.adapter = null
            val birds = Birds()
            withContext(Dispatchers.Default){

               val clientID = getString(R.string.client_id)
                val url = "https://api-v2.soundcloud.com/charts?kind=top&genre=soundcloud%3Agenres%3A$genreID&client_id=$clientID&limit=50&offset=0"
                val response = birds.jsonMan(url)
                val json = JSONObject(response)
                val jsonarr = json.getJSONArray("collection")

                for (i in 0 until jsonarr.length() - 1) {

                    var jsonobj = jsonarr.getJSONObject(i)


                    val titles = jsonobj.getJSONObject("track").getString("title")
                    val artists = jsonobj.getJSONObject("track").getJSONObject("user").getString("username")
                    val artCover = jsonobj.getJSONObject("track").getString("artwork_url")
                    val duration = jsonobj.getJSONObject("track").getInt("duration")
                    val soundID  = jsonobj.getJSONObject("track").getInt("id")
                    //var genre:Double = 0.0
                    //if (!jsonobj.getJSONObject("track").isNull("playback_count")) {
                    //  genre = jsonobj.getJSONObject("track").getInt("playback_count").toDouble()
                    //}

                    val soundLink = jsonobj.getJSONObject("track").getString("permalink_url")
                    //val soundID = jsonobj.getJSONObject("track").getString("id")
                    //val player = "https://api.soundcloud.com/tracks/$soundID/stream?client_id=$clientID"

                    titleList.add(titles)
                    artistList.add(artists)
                    artList.add(artCover)
                    urlList.add(soundLink)
                    songs!!.add(Songs(titles,artists,artCover,soundID,duration,soundLink))


                }
            }

            //new beginnings
           val adapter = SoundAdapter(itemLayoutRes = R.layout.response_view,
                itemCount = titleList.size,
                binder = {

                    val imageURL = artList[it.adapterPosition]
                    val soundLinq = urlList[it.adapterPosition]
                    val position = it.adapterPosition

                    val requestOptions = RequestOptions()
                    requestOptions.placeholder(R.drawable.ic_place)

                    it.itemView.song_title.text = titleList[it.adapterPosition]
                    it.itemView.artist.text = artistList[it.adapterPosition]
                    GlideApp.with(it.itemView.context)
                        .load(imageURL)
                        .apply(requestOptions)
                        .into(it.itemView.album_cover)

                    it.itemView.setOnClickListener {

                        soundPop(position)


                    }


                })


            genre_recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL,false)
            genre_recyclerview.adapter = adapter
            adapter.notifyDataSetChanged()
            genre_refresher.isRefreshing = false

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
        val play_btn = view.findViewById<ImageView>(R.id.play_btn)
        val play_progress  = view.findViewById<ProgressBar>(R.id.play_progress)
        val spinner  =  view.findViewById<ProgressBar>(R.id.spinner)
        val popsaveBtn = view.findViewById<ImageView>(R.id.pop_save_btn)



        popsaveBtn.setOnClickListener {

            val inty = Intent(activity!!, Calcium::class.java)
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


        media.setOnPreparedListener(object : MediaPlayer.OnPreparedListener{
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

        media.setOnCompletionListener(object : MediaPlayer.OnCompletionListener{
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



}
