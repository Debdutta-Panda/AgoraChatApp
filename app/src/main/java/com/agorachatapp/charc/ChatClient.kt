package com.agorachatapp.charc

import android.app.Application
import android.util.Log
import com.agorachatapp.charc.model.ChatPacket
import com.agorachatapp.charc.model.ChatPackets
import com.agorachatapp.newUid
import com.agorachatapp.utcTimestamp
import io.ktor.utils.io.*
import kotlinx.coroutines.delay

class ChatClient private constructor(
    private val application: Application,
    private val sender: String,
    private val appId: String,
    private val appCertificate: String
){
    private var chatDb: ChatDb = ChatDb(sender)
    private var chatAgora: ChatAgora = ChatAgora(application, appId, appCertificate )
    private val chatServer: ChatServer = ChatServer("https://hellomydoc.com/common/chat")
    private val chatCircuit = GateCircuit<ChatPackets>()
    init {
        setupCircuit()
    }

    var frontendNotificationListener: (List<ChatPacket>)->Unit = {}
    private fun setupCircuit() {
        val dbGate = chatCircuit.newGate("db"){ it ->
            val filtered = it.filter {
                it.success!=GateCircuit.Success.FALSE
            }
            var iterationCount = -1
            filtered.forEach {
                it.data.items.forEach {
                    if(++iterationCount>0){
                        delay(10)
                    }
                    chatDb.put(it)
                }
            }
            filtered
        }
        val agoraGate = chatCircuit.newGate("agora"){ it ->
            val allPackets = mutableListOf<ChatPacket>()
            it.forEach {
                allPackets.addAll(it.data.items)
            }
            allPackets.removeAll {
                it.travelled and ChatPacket.TravelPoints.agora == ChatPacket.TravelPoints.agora
            }
            if(allPackets.isEmpty()){
                return@newGate emptyList()
            }
            val asPrev = ChatPackets(allPackets)
            val toSend = asPrev.clone()
            toSend.items.forEach {
                it.upgraded(
                    ChatPacket.ChatPacketMeta(
                    status = Status.RECEIVED_BY_RECEIVER.encoded
                ))
                it.travelled = it.travelled or ChatPacket.TravelPoints.agora
            }
            val toSendGroupBy = toSend.items.groupBy {
                it.receiver
            }
            var iterationCount = -1
            val results = mutableListOf<GateCircuit.Result<ChatPackets>>()
            toSendGroupBy.forEach {
                val receiver = it.key
                val packets = it.value
                if(++iterationCount>0){
                    delay(1200)
                }
                val chatPackets = ChatPackets(packets)
                val success = chatAgora.sendToPeerWithLoginAssurance(sender,receiver,chatPackets)
                if(success){
                    results.add(GateCircuit.Result(
                        GateCircuit.Success.TRUE,
                        chatPackets
                    ))
                }
                else{
                    chatPackets.items.forEach {
                        it.meta?.status = Status.decode(it.meta?.status?:"").apply {
                            set(Status.progress,Status.created)
                        }.encoded
                    }
                    results.add(GateCircuit.Result(
                        GateCircuit.Success.FALSE,
                        chatPackets
                    ))
                }
            }
            results
        }
        val serverGate = chatCircuit.newGate("server"){ it ->
            val allPackets = mutableListOf<ChatPacket>()
            it.forEach {
                allPackets.addAll(it.data.items)
            }
            Log.d("que_bug4",allPackets.size.toString())
            allPackets.removeAll {
                it.travelled and ChatPacket.TravelPoints.server == ChatPacket.TravelPoints.server
            }
            Log.d("que_bug5",allPackets.size.toString())
            if(allPackets.isEmpty()){
                return@newGate emptyList()
            }
            val asPrev = ChatPackets(allPackets)
            val toSendPackets = asPrev.clone()
            toSendPackets.items.forEach {
                it.travelled = it.travelled or ChatPacket.TravelPoints.server
            }
            val results = mutableListOf<GateCircuit.Result<ChatPackets>>()
            val success = chatServer.puts(toSendPackets)
            if(success.data?.success==true){
                toSendPackets.items.forEach {
                    it.meta?.status = Status.decode(it.meta?.status?:"").upgrade(Status.RECEIVED_BY_SERVER).encoded
                }
                results.add(GateCircuit.Result(GateCircuit.Success.TRUE,toSendPackets))
            }
            else{
                results.add(GateCircuit.Result(GateCircuit.Success.FALSE, asPrev))
            }
            results
        }
        val frontendGate = chatCircuit.newGate("frontend"){ it ->
            val allPackets = mutableListOf<ChatPacket>()
            it.forEach {
                allPackets.addAll(it.data.items)
            }
            frontendNotificationListener(allPackets)
            emptyList()
        }
        dbGate.nextStages.apply {
            add(frontendGate)
            add(agoraGate)
        }
        agoraGate.nextStages.apply {
            add(dbGate)
            add(serverGate)
        }
        serverGate.nextStages.apply {
            add(dbGate)
        }
    }

    companion object{
        var instance: ChatClient? = null
            private set

        fun initialize(
            application: Application,
            sender: String,
            appId: String,
            appCertificate: String
        ){
            if(instance==null){
                instance = ChatClient(
                    application,
                    sender,
                    appId,
                    appCertificate
                )
            }
        }
    }
    fun handleChatPushNotification(data: Map<String, String>) {
        if(data.containsKey("data")){
            data["data"]?.let {
                val chatPackets = ChatPackets.fromString(it)
                onChatFromServer(chatPackets)
            }
        }
    }

    private fun onChatFromServer(chatPackets: ChatPackets?) {

    }

    suspend fun newChat(text: String, receiver: String){
        val cp = createChat(text, sender,receiver)
        chatCircuit.gates["db"]?.put("start",listOf(
            GateCircuit.Result(
                GateCircuit.Success.NONE,
                ChatPackets(
                    listOf(cp)
                )
            )
        ))
    }

    private fun notifyFront(chatPacket: ChatPacket) {

    }

    private fun createChat(text: String, sender: String, receiver: String): ChatPacket {
        return ChatPacket(
            chatId = newUid,
            sender = sender,
            receiver = receiver,
            timestamp = utcTimestamp,
            data = ChatPacket.ChatPacketData(
                text = text
            ),
            meta = ChatPacket.ChatPacketMeta(
                status = Status.CREATED.encoded
            )
        )
    }
}

fun charLog(msg: String){
    Log.d("chat_system_log",msg)
}