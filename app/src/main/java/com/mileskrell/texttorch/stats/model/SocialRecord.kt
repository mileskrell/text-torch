package com.mileskrell.texttorch.stats.model

import kotlin.math.roundToInt

/**
 * Holds data about the user's texting history with someone.
 *
 * This data gets displayed to the user in [com.mileskrell.texttorch.stats.StatsFragment].
 */
data class SocialRecord(
    val correspondentName: String,
    // For sorting by most recent
    val mostRecentMessageDate: Long,

    // For who texts first
    val ownInits: Int,
    val correspondentInits: Int,

    // For total texts
    val ownTexts: Int,
    val correspondentTexts: Int,

    // For average length
    val ownAvgChars: Int,
    val correspondentAvgChars: Int
) {
    // These are all computed here based on the provided values,
    // to minimize the work done by the view holder.

    // For who texts first
    val numConversations = ownInits + correspondentInits
    val correspondentInitPercent = (100.0 * correspondentInits / numConversations).roundToInt()

    // For total texts
    val numTexts = ownTexts + correspondentTexts
    val correspondentTextPercent = (100.0 * correspondentTexts / numTexts).roundToInt()

    // For average length
    val combinedAverageChars = ownAvgChars + correspondentAvgChars
    val correspondentAvgCharsPercent = if (combinedAverageChars == 0) {
        // This means that no messages with a body have ever been sent by either person.
        50
    } else {
        (100.0 * correspondentAvgChars / combinedAverageChars).roundToInt()
    }
}
