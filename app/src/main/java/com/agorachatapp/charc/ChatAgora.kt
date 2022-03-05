package com.agorachatapp.charc

import android.app.Application
import com.agorachatapp.R
import io.agora.rtm.*

class ChatAgora(private val application: Application) {
    private var rtmClient: RtmClient? = null
    private val mListenerList: MutableList<RtmClientListener> = ArrayList()
    fun init() {
        val appID = application.getString(R.string.agora_app_id)
        try {
            rtmClient = RtmClient.createInstance(application, appID, object : RtmClientListener {
                override fun onConnectionStateChanged(state: Int, reason: Int) {

                }

                override fun onMessageReceived(rtmMessage: RtmMessage, peerId: String) {
                    mListenerList.forEach {
                        it.onMessageReceived(rtmMessage,peerId)
                    }
                }

                override fun onImageMessageReceivedFromPeer(
                    rtmImageMessage: RtmImageMessage,
                    peerId: String
                ) {

                }

                override fun onFileMessageReceivedFromPeer(
                    rtmFileMessage: RtmFileMessage,
                    s: String
                ) {
                }

                override fun onMediaUploadingProgress(
                    rtmMediaOperationProgress: RtmMediaOperationProgress,
                    l: Long
                ) {

                }

                override fun onMediaDownloadingProgress(
                    rtmMediaOperationProgress: RtmMediaOperationProgress,
                    l: Long
                ) {
                }

                override fun onTokenExpired() {}
                override fun onPeersOnlineStatusChanged(status: Map<String, Int>) {

                }
            })
        } catch (e: Exception) {
        }
    }

    fun registerListener(listener: RtmClientListener) {
        mListenerList.add(listener)
    }

    fun unregisterListener(listener: RtmClientListener) {
        mListenerList.remove(listener)
    }
}