package com.example.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "ULTRON"
    val message: String,
    val timestamp: String,
    val actionTag: String? = null,
    val requiresConfirmation: Boolean = false
)
