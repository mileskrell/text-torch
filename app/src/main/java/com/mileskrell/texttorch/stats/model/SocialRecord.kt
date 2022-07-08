/*
 * Copyright (C) 2020 Miles Krell and the Text Torch contributors
 *
 * This file is part of Text Torch.
 *
 * Text Torch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Text Torch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Text Torch.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mileskrell.texttorch.stats.model

import kotlin.math.roundToInt

/**
 * Holds data about the user's texting history with someone.
 *
 * This data gets displayed to the user in [com.mileskrell.texttorch.stats.StatsFragment].
 */
data class SocialRecord(
    val correspondentName: String?,
    val correspondentAddress: String,
    // For sorting by most recent
    val mostRecentMessageDate: Long,

    /**
     * If true, this record should also display the correspondent's address for clarity.
     * See [MessageThread.nonUniqueName]
     */
    val nonUniqueName: Boolean,

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
    private val combinedAverageChars = ownAvgChars + correspondentAvgChars
    val correspondentAvgCharsPercent = if (combinedAverageChars == 0) {
        // This means that no messages with a body have ever been sent by either person.
        50
    } else {
        (100.0 * correspondentAvgChars / combinedAverageChars).roundToInt()
    }
}
