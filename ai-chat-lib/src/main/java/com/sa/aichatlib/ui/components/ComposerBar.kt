package com.sa.aichatlib.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sa.aichatlib.ui.theme.ChatTokens

@Composable
fun ComposerBar(
	value: String,
	onValueChange: (String) -> Unit,
	onSend: () -> Unit,
	modifier: Modifier = Modifier,
	sendEnabled: Boolean = true,
	placeholder: String = "Type your message..."
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(ChatTokens.spacingSmall)
	) {
		TextField(
			value = value,
			onValueChange = onValueChange,
			modifier = Modifier
				.weight(1f)
				.padding(end = ChatTokens.spacingSmall),
			placeholder = { Text(text = placeholder) },
			singleLine = true,
			shape = RoundedCornerShape(ChatTokens.spacingSmall)
		)
		Button(
			onClick = onSend,
			enabled = sendEnabled,
			shape = RoundedCornerShape(ChatTokens.spacingSmall)
		) {
			Text("Send")
		}
	}
}
