package com.gikoko.claudeagentstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF171717)
private val Panel = Color(0xFF222222)
private val Muted = Color(0xFF9B9B9B)
private val Accent = Color(0xFFD6D6D6)
private val Green = Color(0xFFB7D7C0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AgentStudioApp() }
    }
}

private data class ChatMessage(val text: String, val fromUser: Boolean)

@Composable
private fun AgentStudioApp() {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                "မင်္ဂလာပါ။ G.I KO KO CLAUDE AGENT STUDIO မှ ကြိုဆိုပါတယ်။\n\nProject, code, Skills နဲ့ Agent workflow တွေကို ဒီနေရာကနေ စီမံနိုင်ဖို့ ပြင်ဆင်နေပါတယ်။",
                false
            )
        )
    }
    var draft by remember { mutableStateOf("") }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAction(Icons.Default.Folder, "Projects")
                    QuickAction(Icons.Default.Code, "Agent")
                    QuickAction(Icons.Default.Settings, "Settings")
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            "What can I help you build?",
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 22.dp, bottom = 4.dp)
                        )
                        Text(
                            "Plan first. Review changes. Approve actions.",
                            color = Muted,
                            fontSize = 13.sp
                        )
                    }
                    items(messages) { message -> MessageBubble(message) }
                }
                Composer(
                    value = draft,
                    onValueChange = { draft = it },
                    onSend = {
                        val text = draft.trim()
                        if (text.isNotEmpty()) {
                            messages.add(ChatMessage(text, true))
                            draft = ""
                            messages.add(
                                ChatMessage(
                                    "Agent connection မချိတ်ဆက်ရသေးပါ။ Provider နဲ့ execution backend ကို နောက်အဆင့်မှာ ချိတ်ဆက်ပါမယ်။",
                                    false
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) { Icon(Icons.Default.Menu, null, tint = Color.White) }
        Column(modifier = Modifier.weight(1f)) {
            Text("G.I KO KO", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("CLAUDE AGENT STUDIO", color = Muted, fontSize = 10.sp, letterSpacing = 1.4.sp)
        }
        Box(
            modifier = Modifier.size(34.dp).background(Panel, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("GI", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier.background(Panel, RoundedCornerShape(18.dp)).padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(15.dp))
        Text(label, color = Accent, fontSize = 12.sp)
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(if (message.fromUser) 0.88f else 0.96f)
                .background(if (message.fromUser) Color(0xFF303030) else Panel, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Text(
                if (message.fromUser) "You" else "G.I KO KO Agent",
                color = if (message.fromUser) Green else Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(message.text, color = Color(0xFFF0F0F0), fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun Composer(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp)
            .background(Panel, RoundedCornerShape(22.dp)).padding(12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp, lineHeight = 20.sp),
            minLines = 2,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text("Message G.I KO KO Agent...", color = Muted, fontSize = 14.sp)
                    inner()
                }
            }
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { Icon(Icons.Default.AttachFile, "Attach file", tint = Muted) }
            Text("Agent mode · Approval required", color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onSend) {
                Icon(Icons.Default.Send, "Send", tint = Color(0xFF202020), modifier = Modifier.background(Green, CircleShape).padding(9.dp))
            }
        }
    }
}
