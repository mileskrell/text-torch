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

package com.mileskrell.texttorch.stats.repo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.mileskrell.texttorch.stats.model.MessageThread
import com.mileskrell.texttorch.stats.model.SocialRecord
import com.mileskrell.texttorch.stats.model.SocialRecordsViewModel
import java.util.Locale
import kotlin.math.roundToInt

/**
 * The core functionality of the app, really.
 *
 * First, it uses [ThreadGetter] to fetch the user's texting history.
 *
 * Then, it computes a list of [SocialRecord] to be displayed to the user.
 */
class Repository(val context: Context) {

    companion object {
        const val TAG = "Repository"
    }

    // Used to sort by name, followed by address (phone number/email address)
    private val getNameAddressString = { socialRecord: SocialRecord ->
        ((socialRecord.correspondentName ?: "") + socialRecord.correspondentAddress)
            .toLowerCase(Locale.getDefault())
    }

    lateinit var threads: List<MessageThread>

    /**
     * Returns a list of [SocialRecord], based on the provided [period]
     *
     * @param period: Number of milliseconds of silence required until the next conversation has officially started
     */
    fun initializeSocialRecords(
        period: Int,
        threadsTotal: MutableLiveData<Int>,
        threadsCompleted: MutableLiveData<Int>,
        messagesTotal: MutableLiveData<Int>,
        messagesCompleted: MutableLiveData<Int>
    ): List<SocialRecord> {
        if (!::threads.isInitialized) {
            threads = ThreadGetter(context).getThreads(
                threadsTotal, threadsCompleted, messagesTotal, messagesCompleted
            )
        }

        return getSocialRecordsFromPeriod(period)
    }

    fun getSocialRecordsFromPeriod(period: Int): List<SocialRecord> {
        val socialRecords = mutableListOf<SocialRecord>()

        threads.forEach { thread ->
            var ownInits = 0
            var theirInits = 0
            var ownTexts = 0
            var theirTexts = 0
            var ownTextsWithBody = 0
            var theirTextsWithBody = 0
            var ownTotalChars = 0
            var theirTotalChars = 0

            val firstMessageBody = thread.messages[0].body

            if (thread.messages[0].sentByUser) {
                ownInits++
                ownTexts++
                if (firstMessageBody != null) {
                    ownTextsWithBody++
                    ownTotalChars += firstMessageBody.length
                }
            } else {
                theirInits++
                theirTexts++
                if (firstMessageBody != null) {
                    theirTextsWithBody++
                    theirTotalChars += firstMessageBody.length
                }
            }
            var latestTime = thread.messages[0].date

            thread.messages.drop(1).forEach { message ->
                if (message.sentByUser) {
                    ownTexts++
                    if (message.body != null) {
                        ownTextsWithBody++
                        ownTotalChars += message.body.length
                    }
                    if (message.date - latestTime > period) {
                        ownInits++
                    }
                } else {
                    theirTexts++
                    if (message.body != null) {
                        theirTextsWithBody++
                        theirTotalChars += message.body.length
                    }
                    if (message.date - latestTime > period) {
                        theirInits++
                    }
                }
                latestTime = message.date
            }

            val ownAvgChars = if (ownTextsWithBody == 0) {
                0
            } else {
                (1.0 * ownTotalChars / ownTextsWithBody).roundToInt()
            }

            val theirAvgChars = if (theirTextsWithBody == 0) {
                0
            } else {
                (1.0 * theirTotalChars / theirTextsWithBody).roundToInt()
            }

            socialRecords.add(SocialRecord(
                thread.otherPartyName,
                thread.otherPartyAddress,
                thread.messages.last().date,
                thread.nonUniqueName,
                ownInits,
                theirInits,
                ownTexts,
                theirTexts,
                ownAvgChars,
                theirAvgChars
            ))
        }

        return socialRecords
    }

    /*
    Notes on sorting:
    1. "Reversed" only reverses the primary level of sorting; further levels remain unchanged.
        For example, when using alphabetical as a secondary sort, we always sort A-Z, never Z-A.
    2. In theory, after sorting by name (case-insensitive), we could sort by name (case-sensitive).
       But that would only matter if there were multiple people with the same name, which is rather
       unlikely, so I don't think we need to bother.
     */
    fun sortSocialRecords(socialRecords: List<SocialRecord>, sortType: SocialRecordsViewModel.SortType, reversed: Boolean = false): List<SocialRecord> {
        return when (sortType) {
            SocialRecordsViewModel.SortType.MOST_RECENT -> {
                // These dates are ms since epoch, so it's almost impossible that any two will be
                // equal. Because of that, we don't bother sorting further.
                if (reversed) {
                    socialRecords.sortedBy { it.mostRecentMessageDate }
                } else {
                    socialRecords.sortedByDescending { it.mostRecentMessageDate }
                }
            }
            SocialRecordsViewModel.SortType.ALPHABETICAL -> {
                if (reversed) {
                    socialRecords.sortedByDescending(getNameAddressString)
                } else {
                    socialRecords.sortedBy(getNameAddressString)
                }
            }
            SocialRecordsViewModel.SortType.NUMBER_OF_CONVERSATIONS -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareBy<SocialRecord> { it.numConversations }
                    } else {
                        compareByDescending { it.numConversations }
                    }.thenBy(getNameAddressString)
                )
            }
            SocialRecordsViewModel.SortType.NUMBER_OF_TOTAL_TEXTS -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareBy<SocialRecord> { it.numTexts }
                    } else {
                        compareByDescending { it.numTexts }
                    }.thenBy(getNameAddressString)
                )
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_TEXT_FIRST -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareByDescending<SocialRecord> { it.correspondentInitPercent }
                    } else {
                        compareBy { it.correspondentInitPercent }
                    }.thenBy(getNameAddressString)
                )
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_TEXT_MORE -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareByDescending<SocialRecord> { it.correspondentTextPercent }
                    } else {
                        compareBy { it.correspondentTextPercent }
                    }.thenBy(getNameAddressString)
                )
            }
            SocialRecordsViewModel.SortType.PEOPLE_YOU_SEND_LONGER_TEXTS -> {
                socialRecords.sortedWith(
                    if (reversed) {
                        compareByDescending<SocialRecord> { it.correspondentAvgCharsPercent }
                    } else {
                        compareBy { it.correspondentAvgCharsPercent }
                    }.thenBy(getNameAddressString)
                )
            }
        }
    }
}
