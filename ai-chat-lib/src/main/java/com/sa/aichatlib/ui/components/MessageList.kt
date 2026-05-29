package com.sa.aichatlib.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sa.aichatlib.model.Message
import com.sa.aichatlib.ui.MessageItem
import com.sa.aichatlib.ui.theme.ChatTokens

@Composable
fun MessageList(
	messages: List<Message>,
	isTyping: Boolean,
	typingLabel: String,
	modifier: Modifier = Modifier
) {
	LazyColumn(
		modifier = modifier,
		reverseLayout = true,
		verticalArrangement = Arrangement.Bottom
	) {
		items(messages.reversed(), key = { it.id }) { message ->
			MessageItem(message)
		}

		if (isTyping) {
			item { TypingIndicatorRow(label = typingLabel) }
		}

		item { Spacer(modifier = Modifier.height(ChatTokens.spacingMedium)) }
	}
}

@Composable
private fun TypingIndicatorRow(label: String) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(ChatTokens.spacingSmall),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		CircularProgressIndicator(
			modifier = Modifier.size(24.dp),
			color = MaterialTheme.colorScheme.primary,
			strokeWidth = 2.dp
		)
		Spacer(modifier = Modifier.width(ChatTokens.spacingSmall))
		Text(text = label, style = MaterialTheme.typography.bodyMedium)
	}
}
