package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ChatMessage
import com.example.state.UltronAssistantManager
import com.example.ui.theme.UltronCardBorder
import com.example.ui.theme.UltronCrimson
import com.example.ui.theme.UltronNeonCyan
import com.example.ui.theme.UltronSuccessGreen
import com.example.ui.theme.UltronSurface
import com.example.ui.theme.UltronSurfaceVariant
import com.example.ui.theme.UltronTextMuted
import com.example.ui.theme.UltronTitanium

/**
 * Direct AI Chat Interface for ULTRON
 * Allows typing queries and executing commands (Weather, YouTube, Calling, Network, etc.)
 */
@Composable
fun UltronChatDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val chatMessages by UltronAssistantManager.chatMessages.collectAsStateWithLifecycle()
    val pendingConfirmation by UltronAssistantManager.pendingConfirmation.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D14)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, UltronNeonCyan.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("dialog_ai_chat")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = UltronNeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ULTRON NEURAL CHAT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTitanium,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "REAL-TIME AI & COMMAND LINK",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronNeonCyan,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(UltronSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = UltronTitanium,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Messages list
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(chatMessages) { msg ->
                        ChatBubble(msg)
                    }
                }

                // Confirmation Prompt Banner if active
                if (pendingConfirmation != null) {
                    val conf = pendingConfirmation!!
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1010))
                            .border(1.dp, UltronCrimson.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = conf.prompt,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTitanium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { UltronAssistantManager.confirmPendingAction() },
                                    colors = ButtonDefaults.buttonColors(containerColor = UltronCrimson),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Confirm Call", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                Button(
                                    onClick = { UltronAssistantManager.cancelPendingAction() },
                                    colors = ButtonDefaults.buttonColors(containerColor = UltronSurface),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = UltronTitanium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Ask Ultron (e.g. 'Open YouTube', 'Weather')...",
                                color = UltronTextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UltronNeonCyan,
                            unfocusedBorderColor = UltronCardBorder,
                            focusedTextColor = UltronTitanium,
                            unfocusedTextColor = UltronTitanium
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val text = inputText.trim()
                            if (text.isNotEmpty()) {
                                UltronAssistantManager.sendUserQuery(text)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(UltronNeonCyan)
                            .testTag("btn_send_chat")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "USER"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bg = if (isUser) Color(0xFF132A38) else Color(0xFF0F1722)
    val border = if (isUser) UltronNeonCyan.copy(alpha = 0.5f) else UltronCardBorder

    Column(
        horizontalAlignment = align,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isUser) "USER / TONY" else "ULTRON CORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isUser) UltronNeonCyan else UltronSuccessGreen
                    )
                    Text(
                        text = message.timestamp,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = UltronTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.message,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = UltronTitanium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
