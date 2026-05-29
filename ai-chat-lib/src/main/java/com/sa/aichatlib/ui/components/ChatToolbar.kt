package com.sa.aichatlib.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatToolbar(
	title: String,
	modifier: Modifier = Modifier,
	onClearHistory: (() -> Unit)? = null
) {
	TopAppBar(
		title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
		modifier = modifier,
		colors = TopAppBarDefaults.mediumTopAppBarColors(),
		actions = {
			if (onClearHistory != null) {
				TextButton(onClick = onClearHistory) {
					Text(text = "Clear", style = MaterialTheme.typography.labelLarge)
				}
			}
		}
	)
}
