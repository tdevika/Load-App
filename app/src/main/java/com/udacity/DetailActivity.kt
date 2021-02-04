package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.utils.NotificationUtils
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {
    private var downloadId = -1
    private lateinit var downloadStatus: DownloadStatus
    private lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        loadExtras()
        NotificationUtils.clearNotification(this, downloadId)
        file_name.text = fileName
        download_status.text = (if (downloadStatus == DownloadStatus.SUCCESS) {
            "Success"
        } else {
            "Fail"
        })
        motion_layout.transitionToEnd()

        back_button.setOnClickListener {
            finish()
        }
    }

    private fun loadExtras() {
        val extras = intent.extras
        extras?.let {
            downloadId = it.getInt("download_id")
            downloadStatus = DownloadStatus.values()[it.getInt("download_status")]
            fileName = it.getString("fileName")!!
        }
    }

    companion object {
        fun withExtras(downloadId: Int, status: DownloadStatus, fileName: String?): Bundle {
            return Bundle().apply {
                putInt("download_id", downloadId)
                putInt("download_status", status.ordinal)
                putString("fileName", fileName)
            }

        }
    }

}
