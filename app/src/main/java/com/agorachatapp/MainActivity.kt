package com.agorachatapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agorachatapp.tokener.rtm.RtmTokenBuilder
import io.agora.rtm.*

class MainActivity : ComponentActivity() {
    private var mClientListener: MyRtmClientListener? = null
    private var mChatManager: ChatManager? = null
    private var mRtmClient: RtmClient? = null
    enum class PAGE{
        LOGIN,
        FRIEND,
        MESSAGE
    }
    private val currentPage = mutableStateOf(PAGE.LOGIN)
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
    }

    @Composable
    private fun MessagePageContent() {

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
        mClientListener = MyRtmClientListener()
        mClientListener?.let {
            mChatManager?.registerListener(it)
        }
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

    internal class MyRtmClientListener : RtmClientListener {
        override fun onConnectionStateChanged(state: Int, reason: Int) {

        }

        override fun onMessageReceived(message: RtmMessage, peerId: String) {

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