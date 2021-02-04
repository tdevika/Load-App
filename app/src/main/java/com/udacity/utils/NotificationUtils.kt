package com.udacity.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.DownloadStatus
import com.udacity.MainActivity
import com.udacity.R

object NotificationUtils {
    private const val REQUEST_CODE_DOWNLOADS = 100
    fun getChannelInfo(context: Context): ChannelInfo {
        return ChannelInfo(
            "downloadsID",
            "DownLoads",
            "DownLoading Files",
            NotificationManager.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            NotificationCompat.VISIBILITY_PUBLIC
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context, channelInfo: ChannelInfo) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel(
            channelInfo.id,
            channelInfo.name,
            channelInfo.importance
        ).apply {
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
            lightColor = context.getColor(R.color.colorAccent)
            description = channelInfo.description
            lockscreenVisibility = channelInfo.visibility
            notificationManager.createNotificationChannel(this)
        }
    }

    fun sendDownloadNotification(
        context: MainActivity,
        downloadId: Int,
        status: DownloadStatus,
        fileName: String?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifyIntent = Intent(context, DetailActivity::class.java)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        notifyIntent.putExtras(DetailActivity.withExtras(downloadId, status, fileName))
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_DOWNLOADS,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val downloadChannel = getChannelInfo(context)

        val notification = NotificationCompat.Builder(context, downloadChannel.id)
            .setContentTitle(fileName)
            .setContentText(
                if (status == DownloadStatus.SUCCESS) {
                    context.getString(R.string.success)
                } else {
                    context.getString(R.string.fail)
                }
            )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setAutoCancel(true)
            .setColor(context.getColor(R.color.colorAccent))
            .setLights(context.getColor(R.color.colorAccent), 1000, 3000)
            .setVisibility(downloadChannel.visibility)
            .setPriority(downloadChannel.priority)
            .addAction(
                NotificationCompat.Action(0,context.getString(R.string.details),pendingIntent)
            )
            .build()

        notificationManager.notify(downloadId, notification)
    }
    fun clearNotification(context: Context, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(notificationId)
    }

}


data class ChannelInfo(
    val id: String,
    val name: String,
    val description: String,
    val importance: Int,
    val priority: Int,
    val visibility: Int
)