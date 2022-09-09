package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.datatransport.cct.internal.LogResponse.fromJson
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // TODO: replace this in homework

//        если recipientId = тому, что в AppAuth, то всё ok, показываете Notification;
//        если recipientId = 0 (и не равен вашему), значит сервер считает, что у вас анонимная аутентификация и вам нужно переотправить свой push token;
//        если recipientId != 0 (и не равен вашему), значит сервер считает, что на вашем устройстве другая аутентификация и вам нужно переотправить свой push token;
//        если recipientId = null, то это массовая рассылка, показываете Notification.
        //if (recip)


        println(message.data["content"])
        val messageData = Gson().fromJson(message.data["content"], ServerMessage::class.java)
        when (messageData.recipientId) {
            AppAuth.getInstance().authStateFlow.value.id, null -> handleMessage(messageData.content)
            else -> AppAuth.getInstance().sendPushToken()
        }

    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    private fun handleMessage(content: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

}

class ServerMessage(val recipientId: Long?, val content: String) {


}
