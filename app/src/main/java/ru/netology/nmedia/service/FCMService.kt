package ru.netology.nmedia.service


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.di.DependencyContainer
import kotlin.random.Random


class FCMService : FirebaseMessagingService() {
    private val action = "action"
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
//        message.data[action]?.let {
//            Action.entries.find { entry -> entry.name == it }?.let {
//                when (it) {
//                    Action.LIKE -> handleLike(gson.fromJson(message.data[content],Like::class.java))
//
//                    Action.ADD -> handleAdd(gson.fromJson(message.data[content], Add::class.java))
//                }
//            }
//        }

        val parsedMessage = gson.fromJson(message.data[content], Message::class.java)

        if (parsedMessage.recipientId == (DependencyContainer.getInstance().appAuth.state.value?.id ?: 0) || parsedMessage.recipientId == null ) {
            showNotification(parsedMessage)
        } else {
            DependencyContainer.getInstance().appAuth.sendPushToken()
        }

        //println(message.data["content"])
    }

    override fun onNewToken(token: String) {
        DependencyContainer.getInstance().appAuth.sendPushToken(token)
    }

    private fun showNotification(message: Message) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .run { message.recipientId?.let { this } ?: setContentTitle(getString(R.string.broadcast_message)) }
            .setContentText(message.content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }


    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun handleAdd(content: Add) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_post_added,
                    content.postAuthor,
                )
            )
            .setContentText(
                getString(
                    R.string.notification_post_added,
                    content.postAuthor,
                )
            )

            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.content))
            .build()

        notify(notification)
    }


    private fun notify(notification: Notification) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }
}

enum class Action {
    LIKE,
    ADD
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

data class Add(
    val postId: Long,
    val postAuthor: String,
    val content: String,
)

data class Message (
    val recipientId: Long?,
    val content: String,
)