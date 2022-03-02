package com.agorachatapp

import android.app.Application
import com.Sqlide

class App: Application() {

    companion object{
        lateinit var instance: App
    }
    lateinit var chatManager: ChatManager

    override fun onCreate() {
        super.onCreate()
        Sqlide.initialize(this,"chat")
        instance = this

        chatManager = ChatManager(this)
        chatManager.init()
    }
}