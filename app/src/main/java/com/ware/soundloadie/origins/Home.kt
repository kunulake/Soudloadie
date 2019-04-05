package com.ware.soundloadie.origins


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.request.RequestOptions

import com.ware.soundloadie.R
import com.ware.soundloadie.flappy.Birds
import com.ware.soundloadie.flappy.GlideApp
import com.ware.soundloadie.flappy.SoundAdapter
import kotlinx.android.synthetic.main.home_row.view.*
import kotlinx.android.synthetic.main.origin_home.*
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class Home : Fragment() , CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.origin_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        genreCall()

    }



    fun genreCall(){
        val nameList = ArrayList<String>()
        val followersList = ArrayList<String>()
        val profileList = ArrayList<String>()
        val codeList = ArrayList<String>()
        val birds = Birds()
        recycler.adapter = null

        this.launch {

            withContext(Dispatchers.Default){


                val response = birds.readJsonFile("charts.json",activity!!)
                val json = JSONObject(response)
                val jsonarr = json.getJSONArray("data")

                for (i in 0 until jsonarr.length() - 1) {

                    val jsonobj = jsonarr.getJSONObject(i)

                    val name = jsonobj.getString("name")
                    val code = jsonobj.getString("code")
                    val images = jsonobj.getString("images")

                    nameList.add(name);profileList.add(images);codeList.add(code)

                }


            }

           val adapter = SoundAdapter(itemLayoutRes = R.layout.home_row,
                itemCount = nameList.size,
                binder = {

                    val imageURL = profileList[it.adapterPosition]
                    val names = nameList[it.adapterPosition]
                    val codex = codeList[it.adapterPosition]

                    val requestOptions = RequestOptions()
                    requestOptions.placeholder(R.drawable.ic_place)

                    it.itemView.genre_title.text = names
                    it.itemView.genre_subtitle.text = "Top 50"
                    GlideApp.with(it.itemView.context)
                        .load(imageURL)
                        .apply(requestOptions)
                        .into(it.itemView.genre_cover)

                    it.itemView.setOnClickListener {

                        val bundle = Bundle()
                        bundle.putString("genre",codex)
                        val navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
                        navController.navigate(R.id.action_home_to_genreView, bundle)

                    }


                })


            recycler.layoutManager = GridLayoutManager(activity!!,2)
            recycler.adapter = adapter
            adapter.notifyDataSetChanged()

        }

    }



}
