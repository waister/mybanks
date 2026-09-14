package com.duduapps.mybanks.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.webkit.URLUtil
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.MainActivity
import com.duduapps.mybanks.R
import com.duduapps.mybanks.data.repository.AppConfigRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.net.ConnectException
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val preferencesRepository: PreferencesRepository by inject()
    private val appConfigRepository: AppConfigRepository by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val TYPE = "type"
        const val TITLE = "title"
        const val BODY = "body"
        const val LINK = "link"
        const val IMAGE = "image"
        const val VERSION = "version"
        const val ITEM_ID = "item_id"
        const val VIBRATE = "vibrate"

        private const val API_WAKEUP = "wakeup"
        private const val NOTIFICATION_DEFAULT_ID = 1001
        private const val PARAM_TYPE = "ParamType"
        private const val PARAM_ITEM_ID = "ParamItemId"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        preferencesRepository.fcmToken = token
        serviceScope.launch {
            appConfigRepository.identify(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val type = data[TYPE].orEmpty()
        val title = data[TITLE].orEmpty()
        val body = data[BODY].orEmpty()
        var link = data[LINK].orEmpty()
        val image = data[IMAGE].orEmpty()
        val version = data[VERSION].orEmpty()
        val itemId = data[ITEM_ID].orEmpty()
        val vibrate = data[VIBRATE].orEmpty()

        if (title.isEmpty() || type == API_WAKEUP) return

        if (version.isNotEmpty()) {
            val versionCode = version.filter { it.isDigit() }.toIntOrNull() ?: 0
            if (versionCode > 0) {
                if (BuildConfig.VERSION_CODE < versionCode) {
                    link = "https://play.google.com/store/apps/details?id=${applicationContext.packageName}"
                } else {
                    return
                }
            }
        }

        val notifyIntent: Intent = if (link.isNotEmpty() && URLUtil.isValidUrl(link)) {
            Intent(Intent.ACTION_VIEW, Uri.parse(link))
        } else {
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra(PARAM_TYPE, type)
                putExtra(PARAM_ITEM_ID, itemId)
            }
        }

        val channelId = "${type}_channel"
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(notifyIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        if (body.length > 40) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        if (image.isNotEmpty()) {
            val thumbUrl = if (!image.contains("http") && image.contains("/uploads/")) {
                "https://maggapps.com/thumb?src=$image&w=100&h=100&q=85"
            } else {
                image
            }

            try {
                val url = URL(thumbUrl)
                val icon = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                if (icon != null) {
                    builder.setLargeIcon(icon)
                }
            } catch (_: IOException) {
            } catch (_: ConnectException) {
            }
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.channel_updates),
                NotificationManager.IMPORTANCE_HIGH,
            )
            manager.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        manager.notify(NOTIFICATION_DEFAULT_ID, builder.build())

        if (vibrate.isNotEmpty()) {
            val pattern = longArrayOf(0, 100, 0, 100)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, -1),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}
