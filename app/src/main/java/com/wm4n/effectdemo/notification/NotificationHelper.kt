package com.wm4n.effectdemo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.wm4n.effectdemo.MainActivity
import com.wm4n.effectdemo.MainActivity.Companion.SNACK_BAR_MESSAGE
import com.wm4n.effectdemo.R


object NotificationHelper {

  const val TAG_NOTIFICATION_ID = "Notification ID"
  private const val HEADS_UP_CHANNEL_ID = "HEADS_UP_CHANNEL_ID"
  private const val HEADS_UP_NOTIFICATION_ID = 5000
  private const val HEADS_UP_CHANNEL_NAME = "HeadsUp Notification"

  fun cancelNotification(context: Context, notificationId: Int) {
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(notificationId)
  }

  fun showCustomHeadsUpNotification(context: Context, message: String) {
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 1. Prepare notification channel for Android 8+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Create notification channel for SDK >= 26 (Android O)
      val channel = NotificationChannel(
        HEADS_UP_CHANNEL_ID,
        HEADS_UP_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      )

      // Configure the notification channel.
      channel.enableLights(true)
      channel.enableVibration(true)
      notificationManager.createNotificationChannel(channel)
    }

    // 2. Build up a RemoteViews for other process to should the notification
    val headsUpRemoteView = RemoteViews(context.packageName, R.layout.heads_up_notification).apply RemoteViews@ {

      // 3. Bind RemoteViews values
      setImageViewResource(R.id.icon, R.drawable.ic_round_call_24)
      setImageViewResource(R.id.avatar, R.drawable.mom_avatar)
      setTextViewText(R.id.title, context.getString(R.string.app_name))
      setTextViewText(R.id.text, message)

      // 3.1 PendingIntent for user whom click the [Answer] button
      Intent().apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        // For demo purpose, MainActivity is used here because it demonstrates how to handle PendingIntent
        setClass(context, MainActivity::class.java)
        putExtra(SNACK_BAR_MESSAGE, "Answer call clicked")
        putExtra(TAG_NOTIFICATION_ID, HEADS_UP_NOTIFICATION_ID)
        // Bind [Answer] intent to the RemoteViews
        this@RemoteViews.setOnClickPendingIntent(
          R.id.button_accept,
          PendingIntent.getActivity(context, 5001, this, PendingIntent.FLAG_UPDATE_CURRENT)
        )
      }

      // 3.2 PendingIntent for user whom click the [Decline] button
      Intent().apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        // For demo purpose, MainActivity is used here because it demonstrates how to handle PendingIntent
        setClass(context, MainActivity::class.java)
        putExtra(SNACK_BAR_MESSAGE, "Decline call clicked")
        putExtra(TAG_NOTIFICATION_ID, HEADS_UP_NOTIFICATION_ID)
        // Bind [Decline] intent to the RemoteViews
        this@RemoteViews.setOnClickPendingIntent(
          R.id.button_decline,
          PendingIntent.getActivity(context, 5002, this, PendingIntent.FLAG_UPDATE_CURRENT)
        )
      }
    }

    // 3.3 PendingIntent for user whom click the notification itself
    val pendingIntent = Intent().let {
      it.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      it.setClass(context, MainActivity::class.java)
      it.putExtra(SNACK_BAR_MESSAGE, "Notification clicked")
      it.putExtra(TAG_NOTIFICATION_ID, HEADS_UP_NOTIFICATION_ID)
      PendingIntent.getActivity(context, 5003, it, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // 4. Setup the notification details
    val notification = NotificationCompat.Builder(context, HEADS_UP_CHANNEL_ID).let {
      it.setStyle(NotificationCompat.BigTextStyle())
      it.priority = NotificationCompat.PRIORITY_HIGH
      it.color = ContextCompat.getColor(context, R.color.custom_notification_text)
      it.setSmallIcon(R.drawable.ic_round_call_24)
      it.setContentTitle(message)
      it.setContentText("Tap to enter the dialing screen...")
      it.setCategory(NotificationCompat.CATEGORY_CALL)
      it.setDefaults(NotificationCompat.DEFAULT_ALL)
      it.setVibrate(longArrayOf(1000, 1000))
      it.setOngoing(true)
      it.setAutoCancel(false)
      it.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      it.setCustomHeadsUpContentView(headsUpRemoteView)
      it.setFullScreenIntent(pendingIntent, true)
      it.setContentIntent(pendingIntent)
      it.build()
    }

    // 5. Fire the notification
    notificationManager.notify(
      HEADS_UP_NOTIFICATION_ID,
      notification
    )
  }
}