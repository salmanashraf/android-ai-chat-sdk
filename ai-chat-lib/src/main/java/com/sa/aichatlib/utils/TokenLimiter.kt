package com.sa.aichatlib.utils

object TokenLimiter {
    fun limit(input: String, maxTokens: Int): String {
        val words = input.split(" ")
        return if (words.size > maxTokens) words.take(maxTokens).joinToString(" ") else input
    }
}