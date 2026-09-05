package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.state.UltronLogItem
import com.example.ui.theme.UltronCardBorder
import com.example.ui.theme.UltronCrimson
import com.example.ui.theme.UltronNeonCyan
import com.example.ui.theme.UltronSuccessGreen
import com.example.ui.theme.UltronSurface
import com.example.ui.theme.UltronTextMuted
import com.example.ui.theme.UltronTitanium

@Composable
fun UltronTerminalLog(
    logs: List<UltronLogItem>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = UltronSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
        modifier = modifier.fillMaxWidth().testTag("telemetry_terminal_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = UltronNeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EVENT TELEMETRY LOG",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = UltronTitanium,
                        letterSpacing = 1.sp
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(28.dp).testTag("clear_logs_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear logs",
                            tint = UltronTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF06090E))
                    .border(1.dp, Color(0xFF131B26), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.matchParentSize()
                    ) {
                        Text(
                            text = "Awaiting events. Say 'Ultron' to initiate trigger sequence.",
                            color = UltronTextMuted.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            LogItemRow(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItemRow(log: UltronLogItem) {
    val indicatorColor = when {
        !log.isSuccess -> UltronCrimson
        log.tag == "WAKE" -> UltronNeonCyan
        log.tag == "ACTION" -> UltronSuccessGreen
        log.tag == "VOICE" -> Color(0xFFFFD700)
        else -> UltronTitanium.copy(alpha = 0.7f)
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = log.timestamp,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = UltronTextMuted,
            modifier = Modifier.width(55.dp)
        )

        Text(
            text = "[${log.tag}]",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = indicatorColor,
            modifier = Modifier.width(58.dp)
        )

        Text(
            text = log.message,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = UltronTitanium,
            lineHeight = 13.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
