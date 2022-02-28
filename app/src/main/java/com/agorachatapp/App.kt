package com.agorachatapp

import android.app.Application

class App: Application() {

    companion object{
        lateinit var instance: App
    }
    lateinit var chatManager: ChatManager

    override fun onCreate() {
        super.onCreate()
        instance = this

        chatManager = ChatManager(this)
        chatManager.init()
    }
}