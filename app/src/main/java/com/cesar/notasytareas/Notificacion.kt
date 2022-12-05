package com.cesar.notasytareas

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class Notificacion (base: Context) : ContextWrapper(base) {

    val MYCHANNEL_ID = "App Alert Notification ID"
    val MYCHANNEL_NAME = "App Alert Notification"

    private var manager: NotificationManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }

    // Create channel for Android version 26+
    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannels() {
        val channel = NotificationChannel(MYCHANNEL_ID, MYCHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.enableVibration(true)
        getManager().createNotificationChannel(channel)
    }

    // Get Manager
    fun getManager() : NotificationManager {
        if (manager == null) manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager as NotificationManager
    }

    fun getNotificationBuilder(titulo:String): NotificationCompat.Builder {
        val intent = Intent(this, CreateNote::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        //val pendingIntent = PendingIntent.getActivity(this, notificationID, intent, 0)

        var pendingIntent: PendingIntent? = null
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, notificationID, intent,PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this,  notificationID, intent,PendingIntent.FLAG_ONE_SHOT)
        }
        return NotificationCompat.Builder(applicationContext, MYCHANNEL_ID)
            .setContentTitle(titulo)
            .setContentText("Tienes una tarea pendiente")
            .setSmallIcon(R.drawable.remind)
            .setColor(Color.YELLOW)
            .setContentIntent(pendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
    }
}