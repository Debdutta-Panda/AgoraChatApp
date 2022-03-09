package com.agorachatapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.agorachatapp.charc.ChatClient
import com.agorachatapp.charc.model.ChatPacket
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.agora.rtm.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var senderId = ""
    private val messages = mutableStateListOf<ChatPacket>()
    private var myId: String = ""
    private val messageCardCornerRadius = 6
    private val messageCardCornerElevation = 10
    private val messageCardMargin = 12
    private val messageCardPadding = 12
    private val messageCardMinSizeFactor = 0.125
    private val messageCardMaxSizeFactor = 0.75
    data class Message(
        val rtmMessage: RtmMessage,
        val peerId: String? = null,
    )
    private var peerId: String = ""
    enum class PAGE{
        LOGIN,
        FRIEND,
        MESSAGE
    }
    private val currentPage = mutableStateOf(PAGE.LOGIN)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState==null){
            (application as App).initializeChatClient(senderId)
            ChatClient.instance?.frontendNotificationListener = {
                mergeMessages(it)
            }
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("gfdgfdgfdg52", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.w("gfdgfdgfdg52", token?:"not found")
        })
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                when(currentPage.value){
                    PAGE.LOGIN -> FromPageContent()
                    PAGE.FRIEND -> FriendPageContent()
                    PAGE.MESSAGE -> MessagePageContent()
                }
            }
        }
    }

    private fun mergeMessages(it: List<ChatPacket>) {
        it.forEach { incoming ->
            val id = incoming.chatId
            val index = messages.indexOfFirst { existed->
                existed.chatId == id
            }
            if(index==-1){
                messages.add(incoming)
            }
            else{
                messages[index] = incoming
            }
        }
    }

    @Composable
    private fun MessagePageContent() {
        val state = rememberLazyListState()
        val toSend = remember { mutableStateOf("") }
        Box(modifier = Modifier.fillMaxSize()){
            Column(modifier = Modifier.fillMaxSize()){
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color.Blue)){
                    Text(
                        peerId,
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                LazyColumn(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xfff5f5f5))
                        .weight(1f),
                    state
                ){
                    items(messages){
                        MessageItem(it)
                    }
                    if(messages.size>0){
                        CoroutineScope(Dispatchers.Main).launch {
                            state.scrollToItem(messages.size-1)
                        }
                    }
                }
                Row(modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .height(64.dp)){
                    OutlinedTextField(
                        value = toSend.value,
                        onValueChange = {
                            toSend.value = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    IconButton(onClick = {
                        val text = toSend.value
                        if(text.isEmpty()){
                            toast("Message needed")
                            return@IconButton
                        }
                        toSend.value = ""
                        sendMessage(text)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        lifecycleScope.launch {
            
            ChatClient.instance?.newChat(text,peerId)
            
        }
    }

    private @Composable
    fun LazyItemScope.MessageItem(message: ChatPacket) {
        val configuration = LocalConfiguration.current
        Box(modifier = Modifier.fillMaxWidth()){
            Box(modifier = Modifier
                .width((configuration.screenWidthDp * messageCardMaxSizeFactor).dp)
                .padding(messageCardMargin.dp)
                .align(
                    if (message.sender == myId) Alignment.CenterEnd else Alignment.CenterStart
                )
            ){
                Card(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(if (message.sender == myId) Alignment.CenterEnd else Alignment.CenterStart),
                    elevation = messageCardCornerElevation.dp,
                    backgroundColor = if(message.sender == myId) Color.White else Color(0xff3838ff),
                    shape = RoundedCornerShape(
                        topStart = messageCardCornerRadius.dp,
                        topEnd = messageCardCornerRadius.dp,
                        bottomEnd = if(message.sender == myId) messageCardCornerRadius.dp else 0.dp,
                        bottomStart = if(message.sender == myId) 0.dp else messageCardCornerRadius.dp
                    ),
                ) {
                    Box(modifier = Modifier
                        .widthIn((configuration.screenWidthDp*messageCardMinSizeFactor).dp,(configuration.screenWidthDp*messageCardMaxSizeFactor).dp)){
                        Column(modifier = Modifier
                            .align(if (message.sender == myId) Alignment.CenterEnd else Alignment.CenterStart)
                            .wrapContentSize()
                            .padding(messageCardPadding.dp),
                            horizontalAlignment = if(message.sender == myId) Alignment.End else Alignment.Start
                        ){
                            if(message.sender != myId){
                                Text(
                                    message.sender,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                /*Divider(
                                    color = Color(0xff5757ff),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )*/
                            }
                            Text(
                                message.data?.text?:"",
                                color = if(message.sender == myId) Color.Blue else Color.White
                            )
                            //Text(message.rtmMessage.serverReceivedTs.toString())
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FriendPageContent() {
        val fieldValueState = remember { mutableStateOf("") }
        Box(modifier = Modifier.fillMaxSize()){
            Column(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = fieldValueState.value,
                    onValueChange = {
                        fieldValueState.value = it
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Gray,
                        focusedIndicatorColor = Color.Blue,
                        cursorColor = Color.Blue,
                        textColor = Color.Blue,
                    ),
                    placeholder = {
                        Text("Friend ID")
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    onClick = {
                        onJoinClick(fieldValueState.value)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Blue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Join")
                }
            }
        }
    }

    private fun onJoinClick(value: String) {
        peerId = value
        currentPage.value = PAGE.MESSAGE
    }

    @Composable
    private fun FromPageContent() {
        val fieldValueState = remember { mutableStateOf("") }
        Box(modifier = Modifier.fillMaxSize()){
            Column(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = fieldValueState.value,
                    onValueChange = {
                        fieldValueState.value = it
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Gray,
                        focusedIndicatorColor = Color.Blue,
                        cursorColor = Color.Blue,
                        textColor = Color.Blue,
                    ),
                    placeholder = {
                        Text("User ID")
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    onClick = {
                                onLoginClick(fieldValueState.value)
                              },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Blue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Login")
                }
            }
        }
    }

    private fun onLoginClick(value: String) {
        myId = value
        if(value.isEmpty()){
            toast("User ID can not be empty")
            return
        }
        doLogin(value)
    }

    private fun doLogin(userId: String) {
        senderId = userId
        onLoginDone()
    }

    private fun onLoginDone() {
        currentPage.value = PAGE.FRIEND
    }

    fun toast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}