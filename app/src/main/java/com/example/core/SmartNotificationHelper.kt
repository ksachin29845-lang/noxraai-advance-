package com.example.core

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R

object SmartNotificationHelper {
    private const val CHANNEL_ID = "sachinchat_smart_learning"
    private const val CHANNEL_NAME = "Smart Learning & Streak Reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Supportive language reminders, streak alerts, AI tutor feedback, and milestones."
                // Privacy rule: conceal sensitive details on locked screens
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendLocalNotification(
        context: Context,
        title: String,
        body: String,
        enableSound: Boolean = true,
        enableVibration: Boolean = true
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        ensureChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Noxra AI")
                    .setContentText("Your learning update is ready!")
                    .build()
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (!enableSound) {
            builder.setSilent(true)
        }
        if (!enableVibration) {
            builder.setVibrate(longArrayOf(0L))
        }

        return try {
            NotificationManagerCompat.from(context)
                .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
