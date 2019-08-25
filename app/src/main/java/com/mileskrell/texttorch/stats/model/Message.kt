package com.mileskrell.texttorch.stats.model

/**
 * Represents a single SMS or MMS.
 *
 * Produced by [com.mileskrell.texttorch.stats.repo.ThreadGetter].
 *
 * Used during computations in [com.mileskrell.texttorch.stats.repo.Repository].
 */
data class Message(
    val type: String, // SMS or MMS
    val sentByUser: Boolean,
    val date: Long, // Milliseconds since epoch
    val body: String?
)
