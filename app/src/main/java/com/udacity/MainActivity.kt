package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.utils.NotificationUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var downloadManager: DownloadManager
    private var fileIds = mutableSetOf<Long>()
    private var filesDownloaded = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(
                this,
                NotificationUtils.getChannelInfo(this)
            )
        }

        custom_button.setOnClickListener {
            when (radio_group.checkedRadioButtonId) {
                R.id.load_app -> {
                    download(URL)
                    custom_button.setState(LoadingButton.State.LOADING)
                }
                R.id.glide -> {
                    download(URL_GLIDE)
                    custom_button.setState(LoadingButton.State.LOADING)
                }
                R.id.retrofit -> {
                    download(URL_RETROFIT)
                    custom_button.setState(LoadingButton.State.LOADING)
                }
                else -> Toast.makeText(this, "Please Select a file to download", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
    
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (fileIds.remove(downloadId)) {
                if (--filesDownloaded == 0) {
                    custom_button.setState(LoadingButton.State.COMPLETE)
                }
                
                val extras = intent!!.extras
                val query = DownloadManager.Query()
                        .setFilterById(extras!!.getLong(DownloadManager.EXTRA_DOWNLOAD_ID))
                val cursor = downloadManager.query(query)
                
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    )
                    val fileName = cursor.getString(
                            cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                    )
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            NotificationUtils.sendDownloadNotification(
                                    this@MainActivity,
                                    downloadId!!.toInt(),
                                    DownloadStatus.SUCCESS,
                                    fileName
                            )
                        }
                        DownloadManager.STATUS_FAILED -> {
                            NotificationUtils.sendDownloadNotification(
                                    this@MainActivity,
                                    downloadId!!.toInt(),
                                    DownloadStatus.FAIL,
                                    fileName
                            )
                        }
                    }
                }
            }
        }
    }
  
    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        fileIds.add(downloadManager.enqueue(request))
        filesDownloaded++
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}

enum class DownloadStatus() {
    SUCCESS, FAIL
}
