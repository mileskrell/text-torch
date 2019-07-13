package com.mileskrell.texttorch.stats.model

/**
 * Represents a single SMS or MMS. Used during computations in [com.mileskrell.texttorch.stats.repo.Repository].
 *
 * If the [senderAddress] and [senderName] are `null`, it means the message was sent by the user.
 *
 * If the [recipientAddress] and [recipientName] are `null`, it means the message was received by the user.
 */
data class Message(
    val threadId: Int,
    val date: Long,
    val senderName: String?,
    val senderAddress: String?,
    val recipientName: String?,
    val recipientAddress: String?,
    val body: String
)
