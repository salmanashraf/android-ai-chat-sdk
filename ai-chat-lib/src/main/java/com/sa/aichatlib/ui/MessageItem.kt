package com.sa.aichatlib.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sa.aichatlib.model.Message

@Composable
fun MessageItem(message: Message) {
    val isBot = message.isBot
    val alignment = if (isBot) Alignment.Start else Alignment.End
    val bubbleColor = if (isBot) Color(0xFFE0E0E0) else MaterialTheme.colorScheme.primary
    val textColor = if (isBot) Color.Black else Color.White
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isBot) 0.dp else 16.dp,
        bottomEnd = if (isBot) 16.dp else 0.dp
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
    ) {
        Column(
            horizontalAlignment = alignment
        ) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .padding(12.dp)
            ) {
                Text(
                    text = message.message,
                    color = textColor,
                    fontSize = 16.sp
                )
            }
        }
    }
}
