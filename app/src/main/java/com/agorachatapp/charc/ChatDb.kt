package com.agorachatapp.charc

import android.content.ContentValues
import com.Sqlide
import com.agorachatapp.charc.model.ChatPacket
import org.json.JSONObject

class ChatDb(private val sender: String) {
    companion object {
        private const val me = "me"
        private const val tableName = "chatEntries"
        private const val chatIdColumn = "chatId"
        private const val senderColumn = "sender"
        private const val receiverColumn = "receiver"
        private const val timestampColumn = "timestamp"
        private const val dataColumn = "data"
        private const val statusColumn = "status"
        private const val allColumns = "*"
        private val tableDefinition = """
            CREATE TABLE IF NOT EXISTS $tableName (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        $chatIdColumn varchar(255) NOT NULL,
                        $senderColumn varchar(255) NOT NULL,
                        $receiverColumn varchar(255) NOT NULL,
                        $timestampColumn int,                        
                        $dataColumn TEXT,
                        $statusColumn TEXT
                    );
            """.trimIndent()
    }

    suspend fun get(chatId: String): ChatPacket?{
        val r = Sqlide{
            table(tableDefinition).run {
                val m = select(allColumns).where("$chatIdColumn='$chatId'").get()[0].map
                JSONObject(m).toString()
            }
        }
        return if(r is String){
            ChatPacket.fromString(r)
        } else{
            null
        }
    }

    suspend fun put(packet: ChatPacket) {
        Sqlide {
            table(tableDefinition).apply {
                if(!chatExists(packet.chatId)){
                    addChat(packet)
                }
                else{
                    updateChat(packet)
                }
            }
        }
    }

    private fun Sqlide.Table.updateChat(packet: ChatPacket) {
        if(packet.meta?.status != null){
            val prevStat = getStatus(packet.chatId)
            val newStat = packet.meta.status
            val composedStat = composeStatus(prevStat,newStat)
            if(prevStat!=composedStat){
                update(
                    ContentValues().apply {
                        put(statusColumn,composedStat)
                    },
                    "$chatIdColumn='${packet.chatId}'"
                )
            }
        }
    }

    private fun composeStatus(prevStat: String?, newStat: String): String? {
        return if(prevStat==null){
            newStat
        } else{
            val prev = Status.decode(prevStat)
            val new = Status.decode(newStat)
            prev.upgrade(new).encoded()
        }
    }

    private fun Sqlide.Table.getStatus(chatId: String): String?{
        return select(statusColumn).where("$chatIdColumn='$chatId'").get()[0][0]?.toString()
    }

    private fun Sqlide.Table.chatExists(chatId: String): Boolean{
        return select(allColumns)
            .where("$chatIdColumn='$chatId'")
            .get().rowCount==1
    }

    private fun Sqlide.Table.addChat(packet: ChatPacket){
        var data = packet.data
        if(data!=null){
            insert(
                ContentValues().apply {
                    put(chatIdColumn,packet.chatId)
                    put(senderColumn,packet.sender)
                    put(receiverColumn,packet.receiver)
                    put(timestampColumn,packet.timestamp)
                    put(dataColumn,packet.data.toString())
                    put(statusColumn,packet.meta?.status)
                }
            )
        }

    }
}