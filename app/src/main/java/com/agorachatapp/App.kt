package com.agorachatapp

import android.app.Application
import com.Sqlide
import com.agorachatapp.charc.ChatClient

class App: Application() {

    companion object{
        lateinit var instance: App
    }
    lateinit var chatManager: ChatManager

    override fun onCreate() {
        super.onCreate()
        Sqlide.initialize(this,"chat")
        instance = this

        /*chatManager = ChatManager(this)
        chatManager.init()*/


    }

    fun initializeChatClient(sender: String){
        ChatClient.initialize(
            this,
            sender,
            "a47b50dab04949b7b9ee95b05902b014",
            "4b2f8c4f885c40a7b4f25793231c35da"
        )
    }
}