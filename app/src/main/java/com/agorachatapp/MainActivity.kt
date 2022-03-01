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
import com.agorachatapp.tokener.rtm.RtmTokenBuilder
import io.agora.rtm.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
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
    val messages = mutableStateListOf<Message>()
    private var peerId: String = ""
    private var mClientListener: MyRtmClientListener? = null
    private var mChatManager: ChatManager? = null
    private var mRtmClient: RtmClient? = null
    enum class PAGE{
        LOGIN,
        FRIEND,
        MESSAGE
    }
    private val currentPage = mutableStateOf(PAGE.LOGIN)

    private val service = PostsService.create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        lifecycleScope.launch {
            var r = PostsService.create().createPost(PostRequest("hello1"))
            Log.d("ffsfsdfsfsdf",r?.input?:"empty")
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
        val message = mRtmClient?.createMessage()
        message?.text = text

        if(message==null){
            return
        }
        mRtmClient?.sendMessageToPeer(peerId,message, SendMessageOptions(), object: ResultCallback<Void>{
            override fun onSuccess(p0: Void?) {
                messages.add(Message(message))
            }

            override fun onFailure(p0: ErrorInfo?) {
                toast("Failed to send message")
            }

        })
    }

    private @Composable
    fun LazyItemScope.MessageItem(message: Message) {
        val configuration = LocalConfiguration.current
        Box(modifier = Modifier.fillMaxWidth()){
            Box(modifier = Modifier
                .width((configuration.screenWidthDp*messageCardMaxSizeFactor).dp)
                .padding(messageCardMargin.dp)
                .align(
                    if(message.peerId==null) Alignment.CenterEnd else Alignment.CenterStart
                )
            ){
                Card(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(if(message.peerId==null) Alignment.CenterEnd else Alignment.CenterStart),
                    elevation = messageCardCornerElevation.dp,
                    backgroundColor = if(message.peerId==null) Color.White else Color(0xff3838ff),
                    shape = RoundedCornerShape(
                        topStart = messageCardCornerRadius.dp,
                        topEnd = messageCardCornerRadius.dp,
                        bottomEnd = if(message.peerId==null) messageCardCornerRadius.dp else 0.dp,
                        bottomStart = if(message.peerId==null) 0.dp else messageCardCornerRadius.dp
                    ),
                ) {
                    Box(modifier = Modifier
                        .widthIn((configuration.screenWidthDp*messageCardMinSizeFactor).dp,(configuration.screenWidthDp*messageCardMaxSizeFactor).dp)){
                        Column(modifier = Modifier
                            .align(if(message.peerId==null) Alignment.CenterEnd else Alignment.CenterStart)
                            .wrapContentSize()
                            .padding(messageCardPadding.dp),
                            horizontalAlignment = if(message.peerId==null) Alignment.End else Alignment.Start
                        ){
                            if(message.peerId!=null){
                                Text(
                                    message.peerId,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                /*Divider(
                                    color = Color(0xff5757ff),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )*/
                            }
                            Text(
                                message.rtmMessage.text,
                                color = if(message.peerId==null) Color.Blue else Color.White
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
        mClientListener = MyRtmClientListener()
        mClientListener?.let {
            mChatManager?.registerListener(it)
        }
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
        if(value.isEmpty()){
            toast("User ID can not be empty")
            return
        }
        doLogin(value)
    }

    private fun doLogin(userId: String) {
        mChatManager = App.instance.chatManager
        mRtmClient = mChatManager?.rtmClient

        val tokener = RtmTokenBuilder()
        val token: String = tokener.buildToken(
            "a47b50dab04949b7b9ee95b05902b014",
            "4b2f8c4f885c40a7b4f25793231c35da",
            userId,
            RtmTokenBuilder.Role.Rtm_User,
            0
        )

        mRtmClient!!.login(token, userId, object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                onLoginDone()
            }

            override fun onFailure(errorInfo: ErrorInfo) {

            }
        })
    }

    private fun onLoginDone() {
        currentPage.value = PAGE.FRIEND
    }

    fun toast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    inner class MyRtmClientListener : RtmClientListener {
        override fun onConnectionStateChanged(state: Int, reason: Int) {

        }

        override fun onMessageReceived(message: RtmMessage, peerId: String) {
            messages.add(Message(message, peerId))
        }

        override fun onImageMessageReceivedFromPeer(
            rtmImageMessage: RtmImageMessage,
            peerId: String
        ) {

        }

        override fun onFileMessageReceivedFromPeer(rtmFileMessage: RtmFileMessage, s: String) {}
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
        override fun onPeersOnlineStatusChanged(map: Map<String, Int>) {}
    }
}