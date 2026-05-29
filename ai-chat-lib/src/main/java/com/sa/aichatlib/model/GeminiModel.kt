package com.sa.aichatlib.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class GeminiModel(
    val name: String,
    val temperature: Float,
    val candidateCount: Int,
    val maxOutputTokens: Int,
    val topK: Int,
    val topP: Float,
) : Parcelable