package com.mileskrell.texttorch.stats.model

/**
 * Represent a thread of conversation between the user and one other person.
 */
data class MessageThread(
    val otherPartyAddress: String,
    val otherPartyName: String?,
    val messages: List<Message>
) {
    // If true, there exist multiple threads with people of this name
    var nonUniqueName = false
}
