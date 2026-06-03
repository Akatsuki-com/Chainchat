package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.ChatViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F172A) // Dark Slate
                ) {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    var recipientAddress by remember { mutableStateOf("") }
    var peerIp by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Text(
            "ChainChat Node",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8) // Light Blue
        )
        Text(
            "My Address: ${viewModel.myAddress.take(16)}...",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // P2P Config (Glassmorphic)
        GlassBox {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = peerIp,
                    onValueChange = { peerIp = it },
                    label = { Text("Peer IP (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(0.3f))
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = recipientAddress,
                    onValueChange = { recipientAddress = it },
                    label = { Text("Recipient Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message List
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true
            ) {
                items(viewModel.messages.reversed()) { msg ->
                    ChatBubble(msg)
                }
            }
            if (viewModel.isMining.value) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFF472B6) // Pink
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message Input
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(recipientAddress, messageText, peerIp.ifBlank { null })
                        messageText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)), // Indigo
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Send")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blockchain Dashboard (Simple)
        GlassBox {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Blocks: ${viewModel.blockchain.size}", color = Color.White, fontSize = 12.sp)
                    Text("Last Hash: ${viewModel.blockchain.lastOrNull()?.hash?.take(8) ?: "N/A"}", color = Color.Gray, fontSize = 10.sp)
                }
                Text("NETWORK ACTIVE", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GlassBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(1.dp) // Border simulation
    ) {
        content()
    }
}

@Composable
fun ChatBubble(message: ChatViewModel.DisplayMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isMe) Color(0xFF312E81) else Color(0xFF1E293B),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(message.sender, fontSize = 10.sp, color = Color(0xFFA5B4FC))
                Text(message.text, color = Color.White)
            }
        }
    }
}
