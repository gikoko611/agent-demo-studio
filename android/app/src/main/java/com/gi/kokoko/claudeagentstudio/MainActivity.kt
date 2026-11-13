package com.gi.kokko.claudeagentstudio

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

private val Ink = Color(0xFF171717)
private val Panel = Color(0xFF242424)
private val Muted = Color(0xFF9B9B9B)
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
    var page by remember { mutableStateOf("chat") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("မင်္ဂလာပါ။ G.I KO KO CLAUDE AGENT STUDIO မှ ကြိုဆိုပါတယ်။\n\nProvider ကို Settings မှာ ချိတ်ဆက်ပြီးနောက် AI Chat ကို အသုံးပြုနိုင်အောင် တည်ဆောက်နေပါတယ်။", false)
        )
    }
    var draft by remember { mutableStateOf("") }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            if (page == "settings") {
                ProviderSettingsScreen(onBack = { page = "chat" })
            } else {
                Column(Modifier.fillMaxSize()) {
                    TopBar(onSettings = { page = "settings" })
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAction(Icons.Default.Folder, "Projects") { page = "projects" }
                        QuickAction(Icons.Default.Code, "Agent") { page = "agent" }
                        QuickAction(Icons.Default.Settings, "Settings") { page = "settings" }
                    }
                    if (page == "projects" || page == "agent") {
                        Column(Modifier.weight(1f).fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (page == "projects") "Projects" else "Agent Workspace", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                            Text(if (page == "projects") "Project workspace foundation is ready. Repository browsing and file operations will be added in the next phase." else "Plan-first agent workflow: inspect → plan → review → approve → execute. Execution backend is not connected yet.", color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
                            Button(onClick = { page = "chat" }) { Text("Back to Chat") }
                        }
                    } else {
                        LazyColumn(
                            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Text("What can I help you build?", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 22.dp, bottom = 4.dp))
                                Text("Plan first. Review changes. Approve actions.", color = Muted, fontSize = 13.sp)
                            }
                            items(messages) { MessageBubble(it) }
                        }
                        Composer(
                            value = draft,
                            onValueChange = { draft = it },
                            onSend = {
                                val text = draft.trim()
                                if (text.isNotEmpty()) {
                                    messages.add(ChatMessage(text, true))
                                    draft = ""
                                    messages.add(ChatMessage("Provider ချိတ်ဆက်မှုနဲ့ real AI response ကို နောက်အဆင့်မှာ ဖွင့်ပါမယ်။ Settings မှာ API provider ကို အရင်သတ်မှတ်နိုင်ပါပြီ။", false))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("G.I KO KO", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("CLAUDE AGENT STUDIO", color = Muted, fontSize = 10.sp, letterSpacing = 1.4.sp)
        }
        IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings", tint = Color.White) }
        Box(Modifier.size(34.dp).background(Panel, CircleShape), contentAlignment = Alignment.Center) {
            Text("GI", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Panel, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = Muted, modifier = Modifier.size(15.dp))
            Text(label, color = Color(0xFFE2E2E2), fontSize = 12.sp)
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.fillMaxWidth(if (message.fromUser) 0.88f else 0.96f)
                .background(if (message.fromUser) Color(0xFF303030) else Panel, RoundedCornerShape(18.dp)).padding(14.dp)
        ) {
            Text(if (message.fromUser) "You" else "G.I KO KO Agent", color = if (message.fromUser) Green else Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(message.text, color = Color(0xFFF0F0F0), fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun Composer(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(12.dp).background(Panel, RoundedCornerShape(22.dp)).padding(12.dp)) {
        BasicTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().padding(4.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp, lineHeight = 20.sp),
            minLines = 2,
            decorationBox = { inner -> Box { if (value.isEmpty()) Text("Message G.I KO KO Agent...", color = Muted, fontSize = 14.sp); inner() } }
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

@Composable
private fun ProviderSettingsScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("agent_provider", 0) }
    var provider by remember { mutableStateOf(prefs.getString("provider", "OpenAI-compatible") ?: "OpenAI-compatible") }
    var baseUrl by remember { mutableStateOf(prefs.getString("base_url", "https://api.openai.com/v1") ?: "") }
    var model by remember { mutableStateOf(prefs.getString("model", "gpt-4o-mini") ?: "") }
    var apiKey by remember { mutableStateOf(SecretStore.read(context) ?: "") }
    var status by remember { mutableStateOf("API provider ကို သတ်မှတ်ပါ။ Key ကို Android Keystore ဖြင့် encrypt လုပ်သိမ်းပါမည်။") }
    var busy by remember { mutableStateOf(false) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
            Text("Provider Settings", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
        Text("OpenAI-compatible API", color = Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = provider, onValueChange = { provider = it }, label = { Text("Provider name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("Base URL") }, placeholder = { Text("https://api.example.com/v1") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key") }, singleLine = true, modifier = Modifier.fillMaxWidth(), visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
        Text(status, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                enabled = !busy && baseUrl.isNotBlank() && model.isNotBlank(),
                onClick = {
                    prefs.edit().putString("provider", provider).putString("base_url", baseUrl.trim()).putString("model", model.trim()).apply()
                    try {
                        SecretStore.write(context, apiKey)
                        status = "Provider settings ကို ဖုန်းထဲမှာ သိမ်းပြီးပါပြီ။"
                    } catch (e: Exception) {
                        status = "Secure storage error: ${e.message ?: "unknown"}"
                    }
                }
            ) { Text("Save") }
            OutlinedButton(
                enabled = !busy && baseUrl.isNotBlank() && apiKey.isNotBlank(),
                onClick = {
                    busy = true
                    status = "Connection စမ်းသပ်နေပါတယ်…"
                    Thread {
                        val result = testProvider(baseUrl, apiKey)
                        mainHandler.post {
                            busy = false
                            status = result
                        }
                    }.start()
                }
            ) { Text(if (busy) "Testing…" else "Test Connection") }
        }
        Text("မှတ်ချက်: Connection Test သည် /models endpoint ကိုသာ စမ်းသပ်သည်။ Chat completion နှင့် Agent execution ကို မချိတ်ဆက်ရသေးပါ။", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
    }
}

private fun testProvider(base: String, key: String): String {
    var connection: HttpURLConnection? = null
    return try {
        var root = base.trim().trimEnd('/')
        if (!root.endsWith("/v1")) root += "/v1"
        connection = (URL("$root/models").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12000
            readTimeout = 12000
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Accept", "application/json")
        }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code in 200..299) "✓ Connected (HTTP $code). Models endpoint responded." else "Connection failed (HTTP $code): ${body.take(180)}"
    } catch (e: Exception) {
        "Connection error: ${e.message ?: "network error"}"
    } finally {
        connection?.disconnect()
    }
}

private object SecretStore {
    private const val ALIAS = "gikoko_provider_key"
    private const val PREFS = "agent_provider_secret"
    private const val CIPHER = "AES/GCM/NoPadding"

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        return generator.generateKey()
    }

    fun write(context: android.content.Context, value: String) {
        if (value.isBlank()) {
            context.getSharedPreferences(PREFS, 0).edit().clear().apply()
            return
        }
        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val prefs = context.getSharedPreferences(PREFS, 0)
        prefs.edit().putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString("data", Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
    }

    fun read(context: android.content.Context): String? {
        val prefs = context.getSharedPreferences(PREFS, 0)
        val iv = prefs.getString("iv", null) ?: return null
        val data = prefs.getString("data", null) ?: return null
        return try {
            val cipher = Cipher.getInstance(CIPHER)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
            String(cipher.doFinal(Base64.decode(data, Base64.NO_WRAP)), Charsets.UTF_8)
        } catch (_: Exception) { null }
    }
}
