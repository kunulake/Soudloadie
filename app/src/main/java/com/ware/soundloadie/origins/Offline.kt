package com.ware.soundloadie.origins


import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.pedro.library.AutoPermissions
import com.pedro.library.AutoPermissionsListener

import com.ware.soundloadie.R
import com.ware.soundloadie.flappy.Birds
import com.ware.soundloadie.flappy.SoundAdapter
import kotlinx.android.synthetic.main.offline_row.view.*
import kotlinx.android.synthetic.main.origin_offline.*
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class Offline : Fragment(), AutoPermissionsListener, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    val birds = Birds()
    var job:Job? = null
    val safePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath  + "/soundloadie/"
    var adapter:SoundAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.origin_offline, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerview.layoutManager = linearLayoutManager
        swiperefreesh.isRefreshing = true

        if (permissioinPolice())
        {
            fileFetcher()

        } else {
            AutoPermissions.loadActivityPermissions(activity!!, 1)
        }

        toolar.title = getString(R.string.download_label)


    }






    private fun permissioinPolice(): Boolean {
        val result = ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AutoPermissions.parsePermissions(activity!!, requestCode, permissions, this)
    }

    override fun onGranted(requestCode: Int, permissions: Array<String>) {

        fileFetcher()

    }

    override fun onDenied(requestCode: Int, permissions: Array<String>) {

        AutoPermissions.loadActivityPermissions(activity!!, 1)


    }



    //method to fetch file from SDK card
    fun fileFetcher(){
        val uriList = ArrayList<String>()
        var imageURlList = ArrayList<String>()
        val nameList = ArrayList<String>()
        val dateList = ArrayList<Long>()

        job =  this.launch {

            withContext(Dispatchers.Default){

                val fileDirect = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/soundloadie/")
                if (!fileDirect.exists()) {
                    val soundDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/soundloadie/")
                    soundDirectory.mkdirs()
                }
                // code to retrieve from media library
                val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.DATE_ADDED, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.SIZE, MediaStore.Video.Thumbnails.DATA)
                val queryUri = MediaStore.Files.getContentUri("external")

                val cursor = activity!!.contentResolver.query(queryUri, projection, MediaStore.Files.FileColumns.DATA + " LIKE ? AND " + MediaStore.Files.FileColumns.DATA + " NOT LIKE ?", arrayOf(safePath + "%", safePath + "%/%"), MediaStore.Files.FileColumns.DATE_ADDED + " desc")

                var url = ""


                if (cursor != null) {

                    if (cursor.moveToFirst()) {

                        val Column_data = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                        val Column_name = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val Column_mime = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                        val Column_id = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                        val Column_time = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                        val Column_type = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                        val Column_size = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)


                        do {

                            val mData = cursor.getString(Column_data)
                            val mName = cursor.getString(Column_name)
                            val mMime = cursor.getString(Column_mime)
                            val mId = cursor.getString(Column_id)
                            val mTime = cursor.getString(Column_time)
                            val mType = cursor.getString(Column_type)
                            val mDate = Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000)


                            if (mMime != null && mMime.contains("video")) {

                                val uri = Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString() + "/" + mId)
                                url = uri.toString()


                            }

                            if (mMime!= null && mMime.contains("audio")) {

                                val uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + mId)
                                url = uri.toString()


                            }

                            if (mMime!= null && mMime.contains("image")) {

                                url = mData

                            }

                            uriList.add(mData)
                            imageURlList.add(url)
                            nameList.add(mName)
                            val milliSeconds = mDate.time
                            dateList.add(milliSeconds)

                        } while (cursor.moveToNext())


                    } else {

                    }
                }

                cursor.close()


            }


            if (nameList.size != 0){
                swiperefreesh.isRefreshing = false

                adapter = SoundAdapter(itemLayoutRes = R.layout.offline_row,
                    itemCount = uriList.size,
                    binder = {

                        val name = nameList[it.adapterPosition]
                        val timeInMillis = dateList[it.adapterPosition]
                        val point = uriList[it.adapterPosition]

                        it.itemView.artist.text = TimeAgo.using(timeInMillis)
                        it.itemView.song_title.text = name.replace("_", " ").replace(".mp3", "")

                        val requestOptions = RequestOptions()
                        requestOptions.placeholder(R.drawable.ic_place)
                        it.itemView.setOnClickListener {


                            MediaScannerConnection.scanFile(activity!!, arrayOf(point), null) { path, uri ->

                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setDataAndType(uri, "audio/*")
                                startActivity(intent)


                            }

                        }

                    })


                recyclerview.adapter = adapter
                adapter!!.notifyDataSetChanged()

            }else{

                swiperefreesh.isRefreshing = false

            }


        }
    }



}
