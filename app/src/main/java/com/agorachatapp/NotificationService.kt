package com.agorachatapp

import android.util.Log
import com.agorachatapp.charc.ChatClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("agora_push_notification",p0)
    }


    override fun onMessageReceived(p0: RemoteMessage) {
        val data = p0.data
        if(data.containsKey("type")){
            val type = data["type"]
            when(type){
                "chat"->{
                    ChatClient.instance?.handleChatPushNotification(data)
                }
            }
        }
    }
}