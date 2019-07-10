package com.mileskrell.texttorch.stats.model

import kotlin.math.roundToInt

/**
 * Holds data about the user's texting history with someone.
 *
 * This data gets displayed to the user in [com.mileskrell.texttorch.ui.MainFragment].
 *
 *  TODO Add other stats, e.g.
 *    - # of messages sent by each person
 *    - average # of characters in each person's messages
 *   These will probably be displayed on a second page, to keep the focus on the "who texts first" part.
 */
data class SocialRecord(
    val correspondentName: String,
    val ownInits: Int,
    val correspondentInits: Int,
    val mostRecentMessageDate: Long // Used to sort by most recent again after sorting by something else
) {
    val numConversations = ownInits + correspondentInits
    val correspondentInitPercent = (100.0 * correspondentInits / (correspondentInits + ownInits)).roundToInt()
}
